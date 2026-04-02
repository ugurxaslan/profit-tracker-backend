package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;

@Repository
public interface WalletAssetRepository extends JpaRepository<WalletAsset, Long> {

    List<WalletAsset> findByWallet_Id(Long walletId);

    Optional<WalletAsset> findByAsset_Symbol(String assetSymbol);

    boolean existsByWallet_IdAndAsset_Id(Long walletId, Long assetId);

    Optional<WalletAsset> findByWallet_IdAndAsset_Symbol(Long wallet_Id, String asset_Symbol);

}
