package com.ugurxaslan.profit_tracker_backend.controller;

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

import com.ugurxaslan.profit_tracker_backend.dto.request.UpdateUserRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.WalletRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.UserResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletResponseDTO;
import com.ugurxaslan.profit_tracker_backend.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/users/{id}")
    @PreAuthorize("@userSecurity.isUserOwner(#id, authentication) or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = profileService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(Authentication authentication) {
        UserResponseDTO user = profileService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("@userSecurity.isUserOwner(#id, authentication) or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO requestDTO) {
        UserResponseDTO updatedUser = profileService.updateUser(id, requestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("@userSecurity.isUserOwner(#id, authentication) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        profileService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wallets")
    public ResponseEntity<WalletResponseDTO> createWallet(
            Authentication authentication,
            @Valid @RequestBody WalletRequestDTO requestDTO) {
        WalletResponseDTO createdWallet = profileService.createWallet(authentication.getName(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWallet);
    }

    @PutMapping("/wallets/{walletId}")
    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
    public ResponseEntity<WalletResponseDTO> updateWalletName(
            @PathVariable Long walletId,
            @Valid @RequestBody WalletRequestDTO requestDTO) {
        WalletResponseDTO updatedWallet = profileService.updateWalletName(walletId, requestDTO);
        return ResponseEntity.ok(updatedWallet);
    }

    @DeleteMapping("/wallets/{walletId}")
    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long walletId) {
        profileService.deleteWallet(walletId);
        return ResponseEntity.noContent().build();
    }
}
