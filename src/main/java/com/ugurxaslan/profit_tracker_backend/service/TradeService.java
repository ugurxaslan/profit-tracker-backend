package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.request.SellTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.BuyTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.CashTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.TradeResponseDTO;
import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;
import com.ugurxaslan.profit_tracker_backend.mapper.TradeMapper;
import com.ugurxaslan.profit_tracker_backend.model.Asset;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TradeService {

        private final WalletService walletService;
        private final WalletAssetService walletAssetService;
        private final OpenPositionService openPositionService;
        private final AssetService assetService;

        private final TransactionService transactionService;
        private final TradeMapper tradeMapper;

        @Transactional
        public TradeResponseDTO buy(Long walletId, BuyTradeRequestDTO requestDTO) {

                Wallet wallet = walletService.getWalletEntityById(walletId);
                boolean useCash = requestDTO.getIsUseCash() == null || requestDTO.getIsUseCash();

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, "TRY");

                if (useCash) {
                        this.cashControlForBuy(requestDTO, cashWalletAsset);
                }

                WalletAsset buyWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset buyAsset = buyWalletAsset.getAsset();

                BigDecimal unitPrice = resolveUnitPrice(requestDTO.getUnitPrice(), buyAsset);
                BigDecimal totalCost = requestDTO.getQuantity().multiply(unitPrice);

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

                return tradeMapper.toResponse(buyTransaction);
        }

        @Transactional
        public TradeResponseDTO sell(Long walletId, SellTradeRequestDTO requestDTO) {

                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset sellWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset sellAsset = sellWalletAsset.getAsset();

                // tekli ve çoklu satış durumlarını tek bir fonksiyonda kontrol et
                this.assetControlForSell(requestDTO, sellWalletAsset);

                BigDecimal unitPrice = resolveUnitPrice(requestDTO.getUnitPrice(), sellAsset);
                BigDecimal totalCost = requestDTO.getQuantity().multiply(unitPrice);

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

                openPositionService.sellOpenPosition(sellWalletAsset.getId(), requestDTO.getQuantity(),
                                requestDTO.getSellTransactionId());
                walletAssetService.updateWalletAsset(walletId, sellAsset.getSymbol());

                walletService.syncWallet(walletId);

                return tradeMapper.toResponse(sellTransaction);
        }

        @Transactional
        public TradeResponseDTO cashIn(Long walletId, CashTradeRequestDTO requestDTO, TransactionType transactionType) {
                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset cashAsset = cashWalletAsset.getAsset();

                Transaction cashInTransactionToSave = Transaction.builder()
                                .wallet(wallet)
                                .asset(cashAsset)
                                .transactionType(transactionType)
                                .quantity(requestDTO.getAmount())
                                .unitCost(cashAsset.getCurrentPrice())
                                .totalCost(requestDTO.getAmount().multiply(cashAsset.getCurrentPrice()))
                                .fee(BigDecimal.ZERO)
                                .transactionDate(resolveTransactionDate(null))
                                .build();
                Transaction cashInTransaction = transactionService.createTransaction(cashInTransactionToSave);

                openPositionService.createOpenPosition(cashAsset, cashInTransaction, cashWalletAsset,
                                requestDTO.getAmount());
                walletAssetService.updateWalletAsset(walletId, cashAsset.getSymbol());

                walletService.syncWallet(walletId);

                return tradeMapper.toResponse(cashInTransaction);
        }

        @Transactional
        public TradeResponseDTO cashOut(Long walletId, CashTradeRequestDTO requestDTO,
                        TransactionType transactionType) {

                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset cashAsset = cashWalletAsset.getAsset();

                this.cashControlForCashOut(requestDTO, cashWalletAsset);
                BigDecimal totalCost = requestDTO.getAmount().multiply(cashAsset.getCurrentPrice());

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

                openPositionService.cashOutOpenPositions(cashWalletAsset.getId(), requestDTO.getAmount());
                walletAssetService.updateWalletAsset(walletId, cashAsset.getSymbol());

                walletService.syncWallet(walletId);

                return tradeMapper.toResponse(transaction);
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

        private void cashControlForBuy(BuyTradeRequestDTO requestDTO, WalletAsset cashWalletAsset) {
                BigDecimal requiredCash = requestDTO.getQuantity().multiply(
                                resolveUnitPrice(requestDTO.getUnitPrice(), cashWalletAsset.getAsset()))
                                .add(resolveFee(requestDTO.getFee()));

                if (cashWalletAsset.getQuantity().multiply(cashWalletAsset.getAsset().getCurrentPrice())
                                .compareTo(requiredCash) < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient TRY balance in wallet");
                }
        }

        private void assetControlForSell(SellTradeRequestDTO requestDTO, WalletAsset sellWalletAsset) {
                if (requestDTO.getSellTransactionId() != null) {// null tüm varlıklardan satılabilir demek
                        if (!openPositionService.transactionIsSellable(requestDTO.getSellTransactionId(),
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
}
