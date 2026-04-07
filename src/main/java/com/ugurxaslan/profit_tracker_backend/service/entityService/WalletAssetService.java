package com.ugurxaslan.profit_tracker_backend.service.entityService;

import com.ugurxaslan.profit_tracker_backend.model.OpenPosition;
import com.ugurxaslan.profit_tracker_backend.model.Asset;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import com.ugurxaslan.profit_tracker_backend.repository.WalletAssetRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalletAssetService {

        private static final BigDecimal ZERO = BigDecimal.ZERO;

        private final WalletAssetRepository walletAssetRepository;
        private final OpenPositionService openPositionService;
        private final AssetService assetService;

        @Transactional
        public Wallet recalculateWalletTotals(@NonNull Wallet wallet) {
                List<WalletAsset> walletAssets = walletAssetRepository.findByWallet_Id(wallet.getId());

                BigDecimal newCashBalance = walletAssets.stream()
                                .filter(wa -> wa.getAsset().getSymbol().equalsIgnoreCase("TRY"))
                                .map(wa -> wa.getTotalCost())
                                .reduce(ZERO, BigDecimal::add);
                BigDecimal newPortfolioValue = walletAssets.stream()
                                .filter(wa -> !wa.getAsset().getSymbol().equalsIgnoreCase("TRY"))
                                .map(wa -> wa.getTotalValue())
                                .reduce(ZERO, BigDecimal::add);

                log.info("Recalculating wallet totals for walletId={}, newCashBalance={}, newPortfolioValue={}",
                                wallet.getId(), newCashBalance, newPortfolioValue);
                wallet.setCash(newCashBalance);
                wallet.setPortfolioValue(newPortfolioValue);
                wallet.setTotalValue(newCashBalance.add(newPortfolioValue));
                return wallet;
        }

        public WalletAsset createWalletAsset(@NonNull Wallet wallet, @NonNull Asset asset) {
                if (walletAssetRepository.existsByWallet_IdAndAsset_Id(wallet.getId(), asset.getId())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Wallet asset already exists");
                }

                WalletAsset walletAssetToSave = WalletAsset.builder()
                                .wallet(wallet)
                                .asset(asset)
                                .build();

                return walletAssetRepository.saveAndFlush(Objects.requireNonNull(walletAssetToSave));
        }

        @Transactional(readOnly = true)
        public WalletAsset getWalletAssetByWalletIdAndSymbol(@NonNull Long walletId, @NonNull String assetSymbol) {
                WalletAsset walletAsset = walletAssetRepository
                                .findByWallet_IdAndAsset_Symbol(walletId, assetSymbol.trim().toUpperCase())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Wallet asset not found"));
                recalculateWalletAssetFields(walletAsset);
                return walletAsset;
        }

        @Transactional(readOnly = true)
        public List<WalletAsset> getWalletAssetsByWalletId(@NonNull Long walletId) {
                List<WalletAsset> walletAssets = walletAssetRepository.findByWallet_Id(walletId);
                walletAssets.forEach(this::recalculateWalletAssetFields);
                return walletAssets;
        }

        @Transactional
        public WalletAsset updateWalletAsset(@NonNull Long walletId, @NonNull String assetSymbol) {
                WalletAsset walletAsset = walletAssetRepository
                                .findByWallet_IdAndAsset_Symbol(walletId, assetSymbol.trim().toUpperCase())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Wallet asset not found"));

                recalculateWalletAssetFields(walletAsset);
                return walletAssetRepository.saveAndFlush(Objects.requireNonNull(walletAsset));
        }

        @Transactional
        public void deleteWalletAsset(@NonNull Long walletId, @NonNull String assetSymbol) {
                WalletAsset walletAsset = getWalletAssetByWalletIdAndSymbol(walletId, assetSymbol);
                walletAssetRepository.delete(Objects.requireNonNull(walletAsset));
        }

        // diğer servisler için entity döndüren metot
        @Transactional(readOnly = true)
        public WalletAsset getWalletAssetEntity(@NonNull Wallet wallet, @NonNull Asset asset) {
                WalletAsset walletAsset = walletAssetRepository
                                .findByWallet_IdAndAsset_Symbol(wallet.getId(), asset.getSymbol())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Wallet asset not found"));
                recalculateWalletAssetFields(walletAsset);
                return walletAsset;
        }

        @Transactional
        private void recalculateWalletAssetFields(@NonNull WalletAsset walletAsset) {
                List<OpenPosition> openPositions = openPositionService.getOpenOpenPositions(walletAsset.getId());

                BigDecimal quantity = openPositions.stream()
                                .map(OpenPosition::getRemainingQuantity)
                                .reduce(ZERO, BigDecimal::add);

                BigDecimal totalCost = openPositions.stream()
                                .map(openPosition -> openPosition.getRemainingQuantity()
                                                .multiply(openPosition.getTransaction().getUnitCost()))
                                .reduce(ZERO, BigDecimal::add);

                BigDecimal currentPrice = assetService.getAssetEntityBySymbol(walletAsset.getAsset().getSymbol())
                                .getCurrentPrice();

                BigDecimal totalValue = quantity.multiply(currentPrice);
                BigDecimal averageCost = quantity.signum() > 0
                                ? totalCost.divide(quantity, 8, RoundingMode.HALF_UP)
                                : ZERO;
                BigDecimal profitLoss = totalValue.subtract(totalCost);
                BigDecimal profitLossPercentage = totalCost.signum() > 0
                                ? profitLoss.multiply(BigDecimal.valueOf(100)).divide(totalCost, 8,
                                                RoundingMode.HALF_UP)
                                : ZERO;

                walletAsset.setQuantity(quantity.setScale(8, RoundingMode.HALF_UP));
                walletAsset.setTotalCost(totalCost.setScale(2, RoundingMode.HALF_UP));
                walletAsset.setAverageCost(averageCost.setScale(2, RoundingMode.HALF_UP));
                walletAsset.setTotalValue(totalValue.setScale(2, RoundingMode.HALF_UP));
                walletAsset.setProfitLoss(profitLoss.setScale(8, RoundingMode.HALF_UP));
                walletAsset.setProfitLossPercentage(profitLossPercentage.setScale(8, RoundingMode.HALF_UP));
        }

}
