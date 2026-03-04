package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByWallet_IdOrderByTransactionDateDesc(Long walletId);

    List<Transaction> findByWallet_IdAndAsset_IdOrderByTransactionDateDesc(Long walletId, Long assetId);

    //buy or sell same transactions
    List<Transaction> findByWallet_IdAndAsset_IdAndTransactionTypeOrderByTransactionDateDesc(Long walletId, Long assetId, String transactionType);

}
