package com.ugurxaslan.profit_tracker_backend.service.entityService;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.ugurxaslan.profit_tracker_backend.dto.response.ClosedPositionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.mapper.ClosedPositionMapper;
import com.ugurxaslan.profit_tracker_backend.model.ClosedPosition;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import com.ugurxaslan.profit_tracker_backend.repository.ClosedPositionRepository;
import com.ugurxaslan.profit_tracker_backend.repository.WalletAssetRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class ClosedPositionService {

    private final ClosedPositionRepository closedPositionRepository;
    private final ClosedPositionMapper closedPositionMapper;
    private final WalletAssetRepository walletAssetRepository;

    @Transactional(readOnly = true)
    public Page<ClosedPositionResponseDTO> getClosedPositionsByWalletId(@NonNull Long walletId,
            @NonNull Pageable pageable) {
        return closedPositionRepository.findByWallet_Id(walletId, pageable)
                .map(closedPositionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClosedPositionResponseDTO> getClosedPositionsByWalletAssetId(@NonNull Long walletId,
            @NonNull Long walletAssetId,
            @NonNull Pageable pageable) {
        WalletAsset walletAsset = walletAssetRepository.findById(walletAssetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet asset not found"));

        return closedPositionRepository
                .findByWallet_IdAndBuyTransaction_Asset_Symbol(walletId, walletAsset.getAsset().getSymbol(),
                        pageable)
                .map(closedPositionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClosedPosition getClosedPositionEntityById(@NonNull Long closedPositionId) {
        return closedPositionRepository.findById(closedPositionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Closed position not found"));
    }

    @Transactional
    public ClosedPosition createClosedPosition(@NonNull ClosedPosition closedPosition) {
        return closedPositionRepository.save(Objects.requireNonNull(closedPosition));
    }

    @Transactional
    public void deleteClosedPosition(@NonNull Long closedPositionId) {
        ClosedPosition closedPosition = getClosedPositionEntityById(closedPositionId);
        closedPositionRepository.delete(Objects.requireNonNull(closedPosition));
    }
}
