package com.ugurxaslan.profit_tracker_backend.security;

import com.ugurxaslan.profit_tracker_backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("walletSecurity")
@RequiredArgsConstructor
public class WalletSecurity {

    private final WalletRepository walletRepository;

    public boolean isWalletOwner(Long walletId, Authentication authentication) {
        if (walletId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        if (username == null || "anonymousUser".equalsIgnoreCase(username)) {
            return false;
        }

        return walletRepository.existsByIdAndUser_Username(walletId, username);
    }
}