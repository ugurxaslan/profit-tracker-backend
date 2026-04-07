package com.ugurxaslan.profit_tracker_backend.service.entityService;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.ugurxaslan.profit_tracker_backend.model.ClosedPosition;
import com.ugurxaslan.profit_tracker_backend.repository.ClosedPositionRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class ClosedPositionService {

    private final ClosedPositionRepository closedPositionRepository;

    @Transactional(readOnly = true)
    public List<ClosedPosition> getClosedPositionsByWalletId(@NonNull Long walletId) {
        return closedPositionRepository.findAllByWallet_IdOrderBySellTransaction_TransactionDateDesc(walletId);
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