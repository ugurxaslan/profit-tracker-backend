package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.OpenPosition;

@Repository
public interface OpenPositionRepository extends JpaRepository<OpenPosition, Long> {

    List<OpenPosition> findByWalletAsset_Wallet_Id(Long walletId);

    List<OpenPosition> findByWalletAsset_IdAndIsClosedFalseOrderByTransaction_TransactionDateAsc(Long walletAssetId);

    Optional<OpenPosition> findByTransaction_Id(Long transactionId);

}
