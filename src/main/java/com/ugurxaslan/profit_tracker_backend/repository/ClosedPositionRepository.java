package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.ClosedPosition;

@Repository
public interface ClosedPositionRepository extends JpaRepository<ClosedPosition, Long> {

    List<ClosedPosition> findAllByWallet_IdOrderBySellTransaction_TransactionDateDesc(Long walletId);

    Page<ClosedPosition> findByWallet_Id(Long walletId, Pageable pageable);

    Page<ClosedPosition> findByWallet_IdAndSellTransaction_Asset_Symbol(Long walletId, String assetSymbol,
            Pageable pageable);
}
