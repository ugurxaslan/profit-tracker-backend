package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.request.SellTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.BuyTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.CashTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.TransactionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;
import com.ugurxaslan.profit_tracker_backend.mapper.TransactionMapper;
import com.ugurxaslan.profit_tracker_backend.model.Asset;
import com.ugurxaslan.profit_tracker_backend.model.ClosedPosition;
import com.ugurxaslan.profit_tracker_backend.model.OpenPosition;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import com.ugurxaslan.profit_tracker_backend.service.entityService.AssetService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.ClosedPositionService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.OpenPositionService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.TransactionService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.WalletAssetService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.WalletService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TradeService {

        private final AssetService assetService;
        private final WalletService walletService;
        private final WalletAssetService walletAssetService;
        private final OpenPositionService openPositionService;
        private final ClosedPositionService closedPositionService;
        private final TransactionService transactionService;

        private final TransactionMapper transactionMapper;

        @Transactional
        public TransactionResponseDTO buy(Long walletId, BuyTradeRequestDTO requestDTO) {

                Wallet wallet = walletService.getWalletEntityById(walletId);
                boolean useCash = requestDTO.getIsUseCash() == null || requestDTO.getIsUseCash();

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, "TRY");
                WalletAsset buyWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset buyAsset = buyWalletAsset.getAsset();
                assetControlIsCashForTrade(buyAsset.isCash());// trysatın alamazsın
                if (useCash) {
                        this.cashControlForBuy(requestDTO, cashWalletAsset, buyAsset);
                }

                BigDecimal unitPrice = resolveUnitPrice(requestDTO.getUnitPrice(), buyAsset);
                BigDecimal totalCost = roundMoney(requestDTO.getQuantity().multiply(unitPrice));

                Transaction buyTransactionToSave = Transaction.builder()
                                .wallet(wallet)
                                .asset(buyAsset)
                                .transactionType(TransactionType.BUY)
                                .quantity(requestDTO.getQuantity())
                                .unitCost(unitPrice)
                                .totalCost(totalCost)
                                .fee(resolveFee(requestDTO.getFee()))
                                .transactionDate(resolveTransactionDate(requestDTO.getTransactionDate()))
                                .build();
                Transaction buyTransaction = transactionService.createTransaction(buyTransactionToSave);

                openPositionService.createOpenPosition(buyAsset, buyTransaction, buyWalletAsset,
                                requestDTO.getQuantity());
                walletAssetService.updateWalletAsset(walletId, buyAsset.getSymbol());

                if (useCash) {
                        CashTradeRequestDTO cashOutRequestDTO = CashTradeRequestDTO.builder()
                                        .assetSymbol("TRY")
                                        .amount(totalCost)
                                        .build();
                        this.cashOut(walletId, cashOutRequestDTO, TransactionType.TRADE_CASH_OUT);
                }

                walletService.syncWallet(walletId);

                return transactionMapper.toResponse(buyTransaction);
        }

        @Transactional
        public TransactionResponseDTO sell(Long walletId, SellTradeRequestDTO requestDTO) {
                Wallet wallet = walletService.getWalletEntityById(walletId);
                WalletAsset sellWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset sellAsset = sellWalletAsset.getAsset();
                assetControlIsCashForTrade(sellAsset.isCash());
                this.assetControlForSell(requestDTO, sellWalletAsset);

                BigDecimal unitPrice = resolveUnitPrice(requestDTO.getUnitPrice(), sellAsset);
                BigDecimal totalCost = roundMoney(requestDTO.getQuantity().multiply(unitPrice));

                Transaction sellTransactionToSave = Transaction.builder()
                                .wallet(wallet)
                                .asset(sellAsset)
                                .transactionType(TransactionType.SELL)
                                .quantity(requestDTO.getQuantity())
                                .unitCost(unitPrice)
                                .totalCost(totalCost)
                                .fee(resolveFee(requestDTO.getFee()))
                                .transactionDate(resolveTransactionDate(requestDTO.getTransactionDate()))
                                .build();

                Transaction sellTransaction = transactionService.createTransaction(sellTransactionToSave);

                CashTradeRequestDTO cashOutRequestDTO = CashTradeRequestDTO.builder()
                                .assetSymbol("TRY")
                                .amount(totalCost)
                                .build();
                this.cashIn(walletId, cashOutRequestDTO, TransactionType.TRADE_CASH_IN);

                this.sellOpenPosition(sellWalletAsset.getId(), requestDTO.getQuantity(),
                                requestDTO.getOptionalBuyTransactionIdForSell(), sellTransaction);
                walletAssetService.updateWalletAsset(walletId, sellAsset.getSymbol());

                walletService.syncWallet(walletId);

                return transactionMapper.toResponse(sellTransaction);
        }

        @Transactional
        public TransactionResponseDTO cashIn(Long walletId, CashTradeRequestDTO requestDTO,
                        TransactionType transactionType) {
                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset cashAsset = cashWalletAsset.getAsset();

                Transaction cashInTransactionToSave = Transaction.builder()
                                .wallet(wallet)
                                .asset(cashAsset)
                                .transactionType(transactionType)
                                .quantity(requestDTO.getAmount())
                                .unitCost(cashAsset.getCurrentPrice())
                                .totalCost(roundMoney(requestDTO.getAmount().multiply(cashAsset.getCurrentPrice())))
                                .fee(BigDecimal.ZERO)
                                .transactionDate(resolveTransactionDate(null))
                                .build();
                Transaction cashInTransaction = transactionService.createTransaction(cashInTransactionToSave);

                openPositionService.createOpenPosition(cashAsset, cashInTransaction, cashWalletAsset,
                                requestDTO.getAmount());
                walletAssetService.updateWalletAsset(walletId, cashAsset.getSymbol());

                walletService.syncWallet(walletId);

                return transactionMapper.toResponse(cashInTransaction);
        }

        @Transactional
        public TransactionResponseDTO cashOut(Long walletId, CashTradeRequestDTO requestDTO,
                        TransactionType transactionType) {

                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset cashAsset = cashWalletAsset.getAsset();

                this.cashControlForCashOut(requestDTO, cashWalletAsset);
                BigDecimal totalCost = roundMoney(requestDTO.getAmount().multiply(cashAsset.getCurrentPrice()));

                Transaction transactionToSave = Transaction.builder()
                                .wallet(wallet)
                                .asset(cashAsset)
                                .transactionType(transactionType)
                                .quantity(requestDTO.getAmount())
                                .unitCost(cashAsset.getCurrentPrice())
                                .totalCost(totalCost)
                                .fee(BigDecimal.ZERO)
                                .transactionDate(resolveTransactionDate(null))
                                .build();
                Transaction transaction = transactionService.createTransaction(transactionToSave);

                this.cashOutOpenPositions(cashWalletAsset.getId(), requestDTO.getAmount());
                walletAssetService.updateWalletAsset(walletId, cashAsset.getSymbol());

                walletService.syncWallet(walletId);

                return transactionMapper.toResponse(transaction);
        }

        // yardımcı fonksiyonlar
        private LocalDateTime resolveTransactionDate(LocalDateTime requestDate) {
                return requestDate != null ? requestDate : LocalDateTime.now();
        }

        private BigDecimal resolveUnitPrice(BigDecimal unitPrice, Asset asset) {
                if (unitPrice != null) {
                        return unitPrice;
                }
                return asset.getCurrentPrice();
        }

        private BigDecimal resolveFee(BigDecimal fee) {
                return fee != null ? fee : BigDecimal.ZERO;
        }

        private BigDecimal roundMoney(BigDecimal value) {
                return value.setScale(2, RoundingMode.HALF_UP);
        }

        private WalletAsset getOrCreateWalletAsset(Wallet wallet, String assetSymbol) {
                return wallet.getWalletAssets().stream()
                                .filter(wa -> wa.getAsset().getSymbol().equalsIgnoreCase(assetSymbol))
                                .findFirst()
                                .orElseGet(() -> {
                                        WalletAsset createdWalletAsset = walletAssetService.createWalletAsset(wallet,
                                                        assetService.getAssetEntityBySymbol(assetSymbol));
                                        wallet.getWalletAssets().add(createdWalletAsset);
                                        return createdWalletAsset;
                                });
        }

        private void cashControlForBuy(BuyTradeRequestDTO requestDTO, WalletAsset cashWalletAsset, Asset buyAsset) {
                BigDecimal requiredCash = roundMoney(requestDTO.getQuantity().multiply(
                                resolveUnitPrice(requestDTO.getUnitPrice(), buyAsset)))
                                .add(resolveFee(requestDTO.getFee()));

                if (cashWalletAsset.getQuantity().multiply(cashWalletAsset.getAsset().getCurrentPrice())
                                .compareTo(requiredCash) < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient TRY balance in wallet");
                }
        }

        private void assetControlForSell(SellTradeRequestDTO requestDTO, WalletAsset sellWalletAsset) {
                if (requestDTO.getOptionalBuyTransactionIdForSell() != null) {// null tüm varlıklardan satılabilir demek
                        if (!openPositionService.openPositionIsSellable(sellWalletAsset.getId(),
                                        requestDTO.getOptionalBuyTransactionIdForSell(),
                                        requestDTO.getQuantity())) {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Transaction is not sellable");
                        }
                }
                if (sellWalletAsset.getQuantity().compareTo(requestDTO.getQuantity()) < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Insufficient asset quantity in wallet for sell");
                }
        }

        private void cashControlForCashOut(CashTradeRequestDTO requestDTO, WalletAsset cashWalletAsset) {
                if (cashWalletAsset.getTotalValue().compareTo(requestDTO.getAmount()) < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Insufficient cash balance in wallet");
                }
        }

        @Transactional
        public void sellOpenPosition(@NonNull Long walletAssetId, @NonNull BigDecimal consumeQuantity,
                        Long optionalBuyTransactionIdForSell, Transaction sellTransaction) {
                consumeOpenPositions(walletAssetId, consumeQuantity, "sell", optionalBuyTransactionIdForSell,
                                sellTransaction);
        }

        @Transactional
        public void cashOutOpenPositions(@NonNull Long walletAssetId, @NonNull BigDecimal consumeQuantity) {
                consumeOpenPositions(walletAssetId, consumeQuantity, "cash out", null, null);
        }

        @Transactional
        private void consumeOpenPositions(@NonNull Long walletAssetId, @NonNull BigDecimal consumeQuantity,
                        @NonNull String operationName, Long optionalBuyTransactionIdForSell,
                        Transaction sellTransaction) {
                List<OpenPosition> openPositions;

                if (optionalBuyTransactionIdForSell == null) {
                        openPositions = openPositionService.getOpenPositions(walletAssetId);
                } else {
                        openPositions = List.of(openPositionService
                                        .getOpenPositionByWalletAssetAndTransactionId(walletAssetId,
                                                        optionalBuyTransactionIdForSell));

                }

                BigDecimal totalAvailableQuantity = openPositions.stream()
                                .map(OpenPosition::getRemainingQuantity)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalAvailableQuantity.compareTo(consumeQuantity) < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Insufficient quantity in open positions for " + operationName);
                }

                BigDecimal remainingConsumeQuantity = consumeQuantity;
                for (OpenPosition openPosition : openPositions) {
                        if (remainingConsumeQuantity.compareTo(BigDecimal.ZERO) <= 0)// sayı pozitif se devam et
                                break;

                        BigDecimal positionQty = openPosition.getRemainingQuantity();
                        Boolean isDeleted = false;
                        if (positionQty.compareTo(remainingConsumeQuantity) <= 0) {

                                openPosition.setRemainingQuantity(BigDecimal.ZERO);
                                isDeleted = true;
                                remainingConsumeQuantity = remainingConsumeQuantity.subtract(positionQty);

                        } else {
                                openPosition.setRemainingQuantity(positionQty.subtract(remainingConsumeQuantity));
                                remainingConsumeQuantity = BigDecimal.ZERO;
                        }
                        if (sellTransaction != null) {
                                Transaction buyTransaction = openPosition.getTransaction();
                                BigDecimal profitLoss = calculateProfitLoss(buyTransaction, sellTransaction);
                                BigDecimal profitLossPercentage = calculateProfitLossPercentage(buyTransaction,
                                                sellTransaction);
                                ClosedPosition closedPosition = ClosedPosition.builder()
                                                .buyUnitPrice(buyTransaction.getUnitCost())
                                                .sellUnitPrice(sellTransaction.getUnitCost())
                                                .profitLoss(profitLoss)
                                                .profitLossPercentage(profitLossPercentage)
                                                .buyTransaction(buyTransaction)
                                                .sellTransaction(sellTransaction)
                                                .wallet(sellTransaction.getWallet())
                                                .usedQuantity(positionQty.subtract(openPosition.getRemainingQuantity()))
                                                .build();
                                closedPositionService.createClosedPosition(closedPosition);
                        }
                        if (isDeleted) {
                                openPosition.getWalletAsset().getOpenPositions().remove(openPosition);
                                openPosition.getTransaction().setOpenPosition(null);
                                openPositionService.deleteOpenPosition(openPosition);
                        } else
                                openPositionService.updateOpenPosition(openPosition);
                        isDeleted = false;
                }

        }

        private BigDecimal calculateProfitLoss(Transaction buyTransaction, Transaction sellTransaction) {
                return sellTransaction.getUnitCost().subtract(buyTransaction.getUnitCost())
                                .multiply(sellTransaction.getQuantity());
        }

        private BigDecimal calculateProfitLossPercentage(Transaction buyTransaction, Transaction sellTransaction) {
                if (buyTransaction.getUnitCost().compareTo(BigDecimal.ZERO) == 0) {
                        return BigDecimal.ZERO;
                }
                return sellTransaction.getUnitCost().subtract(buyTransaction.getUnitCost())
                                .divide(buyTransaction.getUnitCost(), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
        }

        private void assetControlIsCashForTrade(boolean isCash) {
                if (isCash) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Cash asset cannot be traded. Only used for cash in/out.");
                }
        }
}
