package com.ugurxaslan.profit_tracker_backend.service.entityService;

import com.ugurxaslan.profit_tracker_backend.model.Asset;
import com.ugurxaslan.profit_tracker_backend.model.OpenPosition;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import com.ugurxaslan.profit_tracker_backend.repository.OpenPositionRepository;
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
public class OpenPositionService {

    private final OpenPositionRepository openPositionRepository;

    public OpenPosition createOpenPosition(@NonNull Asset asset, @NonNull Transaction transaction,
            @NonNull WalletAsset walletAsset, @NonNull BigDecimal remainingQuantity) {
        OpenPosition openPositionToSave = OpenPosition.builder()
                .asset(asset)
                .transaction(transaction)
                .walletAsset(walletAsset)
                .remainingQuantity(remainingQuantity)
                .build();

        return openPositionRepository.saveAndFlush(Objects.requireNonNull(openPositionToSave));
    }

    @Transactional(readOnly = true)
    public OpenPosition getOpenPositionById(@NonNull Long id) {
        return openPositionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open position not found"));
    }

    @Transactional(readOnly = true)
    public List<OpenPosition> getOpenOpenPositions(@NonNull Long walletAssetId) {
        return openPositionRepository
                .findByWalletAsset_IdOrderByTransaction_TransactionDateAsc(
                        walletAssetId);
    }

    @Transactional(readOnly = true)
    public OpenPosition getOpenPositionByTransactionId(@NonNull Long transactionId) {
        return openPositionRepository.findByTransaction_Id(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open position not found"));
    }

    @Transactional(readOnly = true)
    public OpenPosition getOpenPositionByWalletAssetAndTransactionId(@NonNull Long walletAssetId,
            @NonNull Long transactionId) {
        return openPositionRepository
                .findByWalletAsset_IdAndTransaction_Id(walletAssetId, transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open position not found"));
    }

    public OpenPosition updateOpenPosition(@NonNull OpenPosition openPosition) {

        return openPositionRepository.saveAndFlush(openPosition);
    }

    public void deleteOpenPosition(@NonNull OpenPosition openPosition) {

        if (openPosition.getRemainingQuantity() == null
                || openPosition.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Open position can be deleted only when remaining quantity is 0");
        }

        openPositionRepository.delete(openPosition);
    }

    // iç servis

    public Boolean openPositionIsSellable(@NonNull Long walletAssetId, @NonNull Long sellTransactionId,
            @NonNull BigDecimal quantity) {
        OpenPosition openPosition = getOpenPositionByWalletAssetAndTransactionId(walletAssetId, sellTransactionId);
        return openPosition.getRemainingQuantity().compareTo(quantity) >= 0;
    }

}
