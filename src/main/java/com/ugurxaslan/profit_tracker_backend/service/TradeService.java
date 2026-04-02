package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.request.TradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.CashTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.TradeResponseDTO;
import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;
import com.ugurxaslan.profit_tracker_backend.mapper.TradeMapper;
import com.ugurxaslan.profit_tracker_backend.model.Asset;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import com.ugurxaslan.profit_tracker_backend.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TradeService {

        private final WalletService walletService;
        private final AssetService assetService;
        private final AssetLotService assetLotService;
        private final WalletAssetService walletAssetService;

        private final TransactionRepository transactionRepository;
        private final TradeMapper tradeMapper;

        // TODO: sell için : belirtilen transsactiounu satabilme
        // TODO: buy için : alış için bakiye kullan veya direkt ekle
        @Transactional
        public TradeResponseDTO buy(Long walletId, TradeRequestDTO requestDTO) {

                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, "TRY");

                this.cashControlForBuy(requestDTO, cashWalletAsset);

                WalletAsset buyWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset buyAsset = buyWalletAsset.getAsset();

                BigDecimal unitPrice = resolveUnitPrice(requestDTO, buyAsset);
                BigDecimal totalCost = requestDTO.getQuantity().multiply(unitPrice);

                Transaction buyTransactionToSave = Transaction.builder()
                                .wallet(wallet)
                                .asset(buyAsset)
                                .transactionType(TransactionType.BUY)
                                .quantity(requestDTO.getQuantity())
                                .unitCost(unitPrice)
                                .totalCost(requestDTO.getQuantity().multiply(unitPrice))
                                .fee(resolveFee(requestDTO))
                                .transactionDate(resolveTransactionDate(requestDTO.getTransactionDate()))
                                .build();
                Transaction buyTransaction = transactionRepository.save(Objects.requireNonNull(buyTransactionToSave));

                assetLotService.createAssetLot(buyAsset, buyTransaction, buyWalletAsset, requestDTO.getQuantity());
                walletAssetService.updateWalletAsset(walletId, buyAsset.getSymbol());

                CashTradeRequestDTO cashOutRequestDTO = CashTradeRequestDTO.builder()
                                .amount(totalCost)
                                .build();
                this.cashOut(walletId, cashOutRequestDTO, TransactionType.TRADE_CASH_OUT);

                walletService.syncWallet(walletId);

                return tradeMapper.toResponse(buyTransaction);
        }

        @Transactional
        public TradeResponseDTO sell(Long walletId, TradeRequestDTO requestDTO) {

                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset sellWalletAsset = this.getOrCreateWalletAsset(wallet, requestDTO.getAssetSymbol());
                Asset sellAsset = sellWalletAsset.getAsset();

                this.assetControlForSell(requestDTO, sellWalletAsset);

                BigDecimal unitPrice = resolveUnitPrice(requestDTO, sellAsset);
                BigDecimal totalCost = requestDTO.getQuantity().multiply(unitPrice);

                Transaction sellTransactionToSave = Transaction.builder()
                                .wallet(wallet)
                                .asset(sellAsset)
                                .transactionType(TransactionType.SELL)
                                .quantity(requestDTO.getQuantity())
                                .unitCost(unitPrice)
                                .totalCost(totalCost)
                                .fee(resolveFee(requestDTO))
                                .transactionDate(resolveTransactionDate(requestDTO.getTransactionDate()))
                                .build();

                Transaction sellTransaction = transactionRepository.save(Objects.requireNonNull(sellTransactionToSave));

                CashTradeRequestDTO cashOutRequestDTO = CashTradeRequestDTO.builder()
                                .amount(totalCost)
                                .build();
                this.cashIn(walletId, cashOutRequestDTO, TransactionType.TRADE_CASH_IN);

                assetLotService.sellAssetLot(sellWalletAsset.getId(), requestDTO.getQuantity());
                walletAssetService.updateWalletAsset(walletId, sellAsset.getSymbol());

                walletService.syncWallet(walletId);

                return tradeMapper.toResponse(sellTransaction);
        }

        public TradeResponseDTO cashIn(Long walletId, CashTradeRequestDTO requestDTO, TransactionType transactionType) {
                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, "TRY");
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
                Transaction cashInTransaction = transactionRepository
                                .save(Objects.requireNonNull(cashInTransactionToSave));

                assetLotService.createAssetLot(cashAsset, cashInTransaction, cashWalletAsset, requestDTO.getAmount());
                walletAssetService.updateWalletAsset(walletId, cashAsset.getSymbol());

                walletService.syncWallet(walletId);

                return tradeMapper.toResponse(cashInTransaction);
        }

        public TradeResponseDTO cashOut(Long walletId, CashTradeRequestDTO requestDTO,
                        TransactionType transactionType) {

                Wallet wallet = walletService.getWalletEntityById(walletId);

                WalletAsset cashWalletAsset = this.getOrCreateWalletAsset(wallet, "TRY");
                Asset cashAsset = cashWalletAsset.getAsset();

                this.cashControlForCashOut(requestDTO, cashWalletAsset);

                Transaction transactionToSave = Transaction.builder()
                                .wallet(wallet)
                                .asset(cashAsset)
                                .transactionType(transactionType)
                                .quantity(requestDTO.getAmount())
                                .unitCost(cashAsset.getCurrentPrice())
                                .totalCost(requestDTO.getAmount().multiply(cashAsset.getCurrentPrice()))
                                .fee(BigDecimal.ZERO)
                                .transactionDate(resolveTransactionDate(null))
                                .build();
                Transaction transaction = transactionRepository.save(Objects.requireNonNull(transactionToSave));

                assetLotService.cashOutAssetLots(cashWalletAsset.getId(), requestDTO.getAmount());
                walletAssetService.updateWalletAsset(walletId, cashAsset.getSymbol());

                walletService.syncWallet(walletId);

                return tradeMapper.toResponse(transaction);
        }

        // yardımcı fonksiyonlar
        private LocalDateTime resolveTransactionDate(LocalDateTime requestDate) {
                return requestDate != null ? requestDate : LocalDateTime.now();
        }

        private BigDecimal resolveUnitPrice(TradeRequestDTO requestDTO, Asset asset) {
                if (requestDTO.getUnitPrice() != null) {
                        return requestDTO.getUnitPrice();
                }
                return asset.getCurrentPrice();
        }

        private BigDecimal resolveFee(TradeRequestDTO requestDTO) {
                return requestDTO.getFee() != null ? requestDTO.getFee() : BigDecimal.ZERO;
        }

        private WalletAsset getOrCreateWalletAsset(Wallet wallet, String assetSymbol) {
                return wallet.getWalletAssets().stream()
                                .filter(wa -> wa.getAsset().getSymbol().equalsIgnoreCase(assetSymbol))
                                .findFirst()
                                .orElseGet(() -> walletAssetService.createWalletAsset(wallet,
                                                assetService.getAssetEntityBySymbol(assetSymbol)));
        }

        private void cashControlForBuy(TradeRequestDTO requestDTO, WalletAsset cashWalletAsset) {
                BigDecimal requiredCash = requestDTO.getQuantity().multiply(
                                resolveUnitPrice(requestDTO, cashWalletAsset.getAsset())).add(resolveFee(requestDTO));

                if (cashWalletAsset.getQuantity().multiply(cashWalletAsset.getAsset().getCurrentPrice())
                                .compareTo(requiredCash) < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient TRY balance in wallet");
                }
        }

        private void assetControlForSell(TradeRequestDTO requestDTO, WalletAsset sellWalletAsset) {
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
