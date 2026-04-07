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
                .isClosed(false)
                .build();

        return openPositionRepository.saveAndFlush(Objects.requireNonNull(openPositionToSave));
    }

    @Transactional(readOnly = true)
    public OpenPosition getOpenPositionById(@NonNull Long id) {
        return getOpenPositionEntityById(id);
    }

    @Transactional(readOnly = true)
    public List<OpenPosition> getOpenOpenPositions(@NonNull Long walletAssetId) {
        return openPositionRepository
                .findByWalletAsset_IdAndIsClosedFalseOrderByTransaction_TransactionDateAsc(
                        walletAssetId);
    }

    public OpenPosition updateOpenPosition(@NonNull Long id, @NonNull BigDecimal newRemainingQuantity) {
        OpenPosition existingOpenPosition = getOpenPositionEntityById(id);

        existingOpenPosition.setRemainingQuantity(newRemainingQuantity);
        existingOpenPosition.setClosed(newRemainingQuantity.compareTo(BigDecimal.ZERO) == 0);

        return openPositionRepository.saveAndFlush(existingOpenPosition);
    }

    public void deleteOpenPosition(@NonNull Long id) {
        OpenPosition openPosition = getOpenPositionEntityById(id);

        if (openPosition.getRemainingQuantity() == null
                || openPosition.getRemainingQuantity().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Open position can be deleted only when remaining quantity is 0");
        }

        openPositionRepository.delete(openPosition);
    }

    // iç servis
    @Transactional
    public OpenPosition getOpenPositionEntityById(@NonNull Long id) {
        return openPositionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open position not found"));
    }

    @Transactional
    public void sellOpenPosition(@NonNull Long walletAssetId, @NonNull BigDecimal quantity,
            Long optionalSellTransactionId) {
        consumeOpenPositions(walletAssetId, quantity, "sell", optionalSellTransactionId);
    }

    @Transactional
    public void cashOutOpenPositions(@NonNull Long walletAssetId, @NonNull BigDecimal amount) {
        consumeOpenPositions(walletAssetId, amount, "cash out", null);
    }

    @Transactional
    private void consumeOpenPositions(@NonNull Long walletAssetId, @NonNull BigDecimal amount,
            @NonNull String operationName, Long optionalSellTransactionId) {
        List<OpenPosition> openPositions = getOpenOpenPositions(walletAssetId);

        if (optionalSellTransactionId == null) {
            openPositions = getOpenOpenPositions(walletAssetId);
        } else {
            openPositions.add(openPositionRepository.findByTransaction_Id(optionalSellTransactionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open position not found")));
        }

        BigDecimal totalAvailable = openPositions.stream()
                .map(OpenPosition::getRemainingQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient quantity in open positions for " + operationName);
        }

        BigDecimal remainingAmount = amount;
        for (OpenPosition lot : openPositions) {
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
            openPositionRepository.saveAndFlush(lot);
        }
    }

    public Boolean transactionIsSellable(@NonNull Long sellTransactionId, @NonNull BigDecimal quantity) {
        OpenPosition openPosition = openPositionRepository.findByTransaction_Id(sellTransactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open position not found"));
        return openPosition.getRemainingQuantity().compareTo(quantity) >= 0;
    }

}
