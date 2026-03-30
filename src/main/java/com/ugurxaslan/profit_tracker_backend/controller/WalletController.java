package com.ugurxaslan.profit_tracker_backend.controller;

import com.ugurxaslan.profit_tracker_backend.dto.request.WalletRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletResponseDTO;
import com.ugurxaslan.profit_tracker_backend.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponseDTO> createWallet(
            Authentication authentication,
            @Valid @RequestBody WalletRequestDTO requestDTO) {
        WalletResponseDTO createdWallet = walletService.createWallet(authentication.getName(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWallet);
    }

    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponseDTO> getWalletById(@PathVariable Long walletId) {
        WalletResponseDTO wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping
    public ResponseEntity<List<WalletResponseDTO>> getAllWallets(Authentication authentication) {
        List<WalletResponseDTO> wallets = walletService.getAllWallets(authentication.getName());
        return ResponseEntity.ok(wallets);
    }

    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
    @PutMapping("/{walletId}")
    public ResponseEntity<WalletResponseDTO> updateWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody WalletRequestDTO requestDTO) {
        WalletResponseDTO updatedWallet = walletService.updateWallet(walletId, requestDTO);
        return ResponseEntity.ok(updatedWallet);
    }

    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long walletId) {
        walletService.deleteWallet(walletId);
        return ResponseEntity.noContent().build();
    }
}
