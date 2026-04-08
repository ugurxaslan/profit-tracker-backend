package com.ugurxaslan.profit_tracker_backend.security;

import com.ugurxaslan.profit_tracker_backend.repository.WalletAssetRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("walletAssetSecurity")
@RequiredArgsConstructor
public class WalletAssetSecurity {

    private final WalletAssetRepository walletAssetRepository;

    public boolean isWalletAssetOwner(Long walletAssetId, Long walletId,
            Authentication authentication) {
        if (walletAssetId == null || walletId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        if (username == null || "anonymousUser".equalsIgnoreCase(username)) {
            return false;
        }

        return walletAssetRepository.existsByIdAndWallet_Id(walletAssetId, walletId);
    }
}