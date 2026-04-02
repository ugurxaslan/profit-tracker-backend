package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.model.Asset;
import com.ugurxaslan.profit_tracker_backend.model.AssetLot;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import com.ugurxaslan.profit_tracker_backend.repository.AssetLotRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetLotService {

    private final AssetLotRepository assetLotRepository;

    public AssetLot createAssetLot(@NonNull Asset asset, @NonNull Transaction transaction,
            @NonNull WalletAsset walletAsset, @NonNull BigDecimal remainingQuantity) {
        AssetLot assetLotToSave = AssetLot.builder()
                .asset(asset)
                .transaction(transaction)
                .walletAsset(walletAsset)
                .remainingQuantity(remainingQuantity)
                .isClosed(false)
                .build();

        return assetLotRepository.saveAndFlush(Objects.requireNonNull(assetLotToSave));
    }

    @Transactional(readOnly = true)
    public AssetLot getAssetLotById(@NonNull Long id) {
        return getAssetLotEntityById(id);
    }

    @Transactional(readOnly = true)
    public List<AssetLot> getOpenAssetLots(@NonNull Long walletAssetId) {
        return assetLotRepository
                .findByWalletAsset_IdAndIsClosedFalseOrderByTransaction_TransactionDateAsc(
                        walletAssetId);
    }

    public AssetLot updateAssetLot(@NonNull Long id, @NonNull BigDecimal newRemainingQuantity) {
        AssetLot existingAssetLot = getAssetLotEntityById(id);

        existingAssetLot.setRemainingQuantity(newRemainingQuantity);
        existingAssetLot.setClosed(newRemainingQuantity.compareTo(BigDecimal.ZERO) == 0);

        return assetLotRepository.saveAndFlush(existingAssetLot);
    }

    public void deleteAssetLot(@NonNull Long id) {
        AssetLot assetLot = getAssetLotEntityById(id);

        if (assetLot.getRemainingQuantity() == null
                || assetLot.getRemainingQuantity().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Asset lot can be deleted only when remaining quantity is 0");
        }

        assetLotRepository.delete(assetLot);
    }

    // iç servis
    @Transactional
    public AssetLot getAssetLotEntityById(@NonNull Long id) {
        return assetLotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset lot not found"));
    }

    @Transactional
    public void sellAssetLot(@NonNull Long walletAssetId, @NonNull BigDecimal quantity,
            Long optionalSellTransactionId) {
        consumeAssetLots(walletAssetId, quantity, "sell", optionalSellTransactionId);
    }

    @Transactional
    public void cashOutAssetLots(@NonNull Long walletAssetId, @NonNull BigDecimal amount) {
        consumeAssetLots(walletAssetId, amount, "cash out", null);
    }

    @Transactional
    private void consumeAssetLots(@NonNull Long walletAssetId, @NonNull BigDecimal amount,
            @NonNull String operationName, Long optionalSellTransactionId) {
        List<AssetLot> assetLots = getOpenAssetLots(walletAssetId);

        if (optionalSellTransactionId == null) {
            assetLots = getOpenAssetLots(walletAssetId);
        } else {
            assetLots.add(assetLotRepository.findByTransaction_Id(optionalSellTransactionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset lot not found")));
        }

        BigDecimal totalAvailable = assetLots.stream()
                .map(AssetLot::getRemainingQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient quantity in asset lots for " + operationName);
        }

        BigDecimal remainingAmount = amount;
        for (AssetLot lot : assetLots) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0)
                break;

            BigDecimal lotQty = lot.getRemainingQuantity();
            if (lotQty.compareTo(remainingAmount) <= 0) {
                lot.setRemainingQuantity(BigDecimal.ZERO);
                lot.setClosed(true);
                remainingAmount = remainingAmount.subtract(lotQty);
            } else {
                lot.setRemainingQuantity(lotQty.subtract(remainingAmount));
                remainingAmount = BigDecimal.ZERO;
            }
            assetLotRepository.saveAndFlush(lot);
        }
    }

    public Boolean transactionIsSellable(@NonNull Long sellTransactionId, @NonNull BigDecimal quantity) {
        AssetLot assetLot = assetLotRepository.findByTransaction_Id(sellTransactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset lot not found"));
        return assetLot.getRemainingQuantity().compareTo(quantity) >= 0;
    }

}
