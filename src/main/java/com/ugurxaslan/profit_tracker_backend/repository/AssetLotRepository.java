package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.AssetLot;

@Repository
public interface AssetLotRepository extends JpaRepository<AssetLot, Long> {

	List<AssetLot> findByWalletAsset_IdOrderByTransaction_TransactionDateAsc(Long walletAssetId);

	List<AssetLot> findByWalletAsset_IdAndIsClosedFalseOrderByTransaction_TransactionDateAsc(Long walletAssetId);

	List<AssetLot> findByWalletAsset_Wallet_IdAndAsset_IdAndIsClosedFalseOrderByTransaction_TransactionDateAsc(Long walletId, Long assetId);
}