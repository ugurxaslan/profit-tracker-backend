package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByWalletIdOrderByTransactionDateDesc(Long walletId);

    List<Transaction> findByWalletIdAndAssetIdOrderByTransactionDateDesc(Long walletId, Long assetId);
}
