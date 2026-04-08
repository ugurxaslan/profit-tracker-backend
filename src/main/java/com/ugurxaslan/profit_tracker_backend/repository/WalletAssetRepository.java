package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;

@Repository
public interface WalletAssetRepository extends JpaRepository<WalletAsset, Long> {

    List<WalletAsset> findByWallet_Id(Long walletId);

    Page<WalletAsset> findByWallet_Id(Long walletId, Pageable pageable);

    Optional<WalletAsset> findByAsset_Symbol(String assetSymbol);

    boolean existsByWallet_IdAndAsset_Id(Long walletId, Long assetId);

    boolean existsByIdAndWallet_Id(Long walletAssetId, Long walletId);

    Optional<WalletAsset> findByWallet_IdAndAsset_Symbol(Long wallet_Id, String asset_Symbol);

}
