package com.ugurxaslan.profit_tracker_backend.service.entityService;

import com.ugurxaslan.profit_tracker_backend.dto.response.OpenPositionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.mapper.OpenPositionMapper;
import com.ugurxaslan.profit_tracker_backend.model.Asset;
import com.ugurxaslan.profit_tracker_backend.model.OpenPosition;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import com.ugurxaslan.profit_tracker_backend.repository.OpenPositionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class OpenPositionService {

    private final OpenPositionRepository openPositionRepository;
    private final OpenPositionMapper openPositionMapper;
    private final AssetService assetService;

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
    public List<OpenPosition> getOpenPositions(@NonNull Long walletAssetId) {
        return openPositionRepository
                .findByWalletAsset_IdOrderByTransaction_TransactionDateAsc(
                        walletAssetId);
    }

    @Transactional(readOnly = true)
    public Page<OpenPositionResponseDTO> getOpenPositionsByWalletId(@NonNull Long walletId,
            @NonNull Pageable pageable) {
        return openPositionRepository.findByWalletAsset_Wallet_IdAndAsset_IsCashFalse(walletId, pageable)
                .map(this::calculateAndMapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<OpenPositionResponseDTO> getOpenPositionsByWalletAssetId(
            @NonNull Long walletAssetId,
            @NonNull Pageable pageable) {
        return openPositionRepository.findByWalletAsset_IdAndAsset_IsCashFalse(walletAssetId, pageable)
                .map(this::calculateAndMapToResponse);
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

    private OpenPositionResponseDTO calculateAndMapToResponse(@NonNull OpenPosition openPosition) {
        OpenPositionResponseDTO dto = openPositionMapper.toResponse(openPosition);
        Asset asset = assetService.getAssetEntityBySymbol(openPosition.getAsset().getSymbol());

        BigDecimal remainingQuantity = openPosition.getRemainingQuantity();
        BigDecimal unitCost = openPosition.getTransaction().getUnitCost();
        BigDecimal totalCost = remainingQuantity.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentPrice = asset.getCurrentPrice();
        BigDecimal currentTotalValue = remainingQuantity.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedPL = currentTotalValue.subtract(totalCost);

        BigDecimal unrealizedPLP = BigDecimal.ZERO;
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            unrealizedPLP = unrealizedPL.divide(totalCost, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        dto.setUnitCost(unitCost);
        dto.setTotalCost(totalCost);
        dto.setCurrentUnitPrice(currentPrice);
        dto.setCurrentTotalValue(currentTotalValue);
        dto.setUnrealizedPL(unrealizedPL);
        dto.setUnrealizedPLP(unrealizedPLP);

        return dto;
    }

}
