package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.OpenPosition;

@Repository
public interface OpenPositionRepository extends JpaRepository<OpenPosition, Long> {

    List<OpenPosition> findByWalletAsset_Wallet_Id(Long walletId);

    Page<OpenPosition> findByWalletAsset_Wallet_Id(Long walletId, Pageable pageable);

    Page<OpenPosition> findByWalletAsset_Id(Long walletAssetId, Pageable pageable);

    List<OpenPosition> findByWalletAsset_IdOrderByTransaction_TransactionDateAsc(Long walletAssetId);

    Optional<OpenPosition> findByWalletAsset_IdAndTransaction_Id(Long walletAssetId, Long transactionId);

    Optional<OpenPosition> findByTransaction_Id(Long transactionId);

}
