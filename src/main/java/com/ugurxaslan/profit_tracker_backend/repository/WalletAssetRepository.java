package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;

@Repository
public interface WalletAssetRepository extends JpaRepository<WalletAsset, Long> {

    List<WalletAsset> findByWalletId(Long walletId);

    Optional<WalletAsset> findByWalletIdAndAssetId(Long walletId, Long assetId);
    
}
