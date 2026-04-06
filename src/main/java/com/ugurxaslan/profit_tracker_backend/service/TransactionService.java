package com.ugurxaslan.profit_tracker_backend.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import com.ugurxaslan.profit_tracker_backend.repository.TransactionRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByWalletId(@NonNull Long walletId) {
        return transactionRepository.findByWallet_IdOrderByTransactionDateDesc(walletId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByWalletIdAndAssetId(@NonNull Long walletId, @NonNull Long assetId) {
        return transactionRepository.findByWallet_IdAndAsset_IdOrderByTransactionDateDesc(walletId, assetId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByWalletIdAndAssetIdAndType(
            @NonNull Long walletId,
            @NonNull Long assetId,
            @NonNull TransactionType transactionType) {
        return transactionRepository.findByWallet_IdAndAsset_IdAndTransactionTypeOrderByTransactionDateDesc(
                walletId,
                assetId,
                transactionType.name());
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionEntityById(@NonNull Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    @Transactional
    public Transaction createTransaction(@NonNull Transaction transaction) {

        return transactionRepository.save(Objects.requireNonNull(transaction));
    }
}
