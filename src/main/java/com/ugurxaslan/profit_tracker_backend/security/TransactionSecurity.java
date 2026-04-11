package com.ugurxaslan.profit_tracker_backend.security;

import com.ugurxaslan.profit_tracker_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("transactionSecurity")
@RequiredArgsConstructor
public class TransactionSecurity {

    private final TransactionRepository transactionRepository;

    public boolean isTransactionOwner(Long walletId, Long transactionId) {
        if (walletId == null || transactionId == null) {
            return false;
        }

        return transactionRepository.existsByIdAndWallet_Id(transactionId, walletId);
    }
}