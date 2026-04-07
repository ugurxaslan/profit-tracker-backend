package com.ugurxaslan.profit_tracker_backend.security;

import com.ugurxaslan.profit_tracker_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("transactionSecurity")
@RequiredArgsConstructor
public class TransactionSecurity {

    private final TransactionRepository transactionRepository;

    public boolean isTransactionOwner(Long transactionId, Authentication authentication) {
        if (transactionId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        if (username == null || "anonymousUser".equalsIgnoreCase(username)) {
            return false;
        }

        return transactionRepository.existsByIdAndWallet_User_Username(transactionId, username);
    }
}