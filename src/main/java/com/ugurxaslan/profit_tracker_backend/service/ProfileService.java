package com.ugurxaslan.profit_tracker_backend.service;

import org.springframework.stereotype.Service;

import com.ugurxaslan.profit_tracker_backend.dto.request.UpdateUserRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.WalletRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.UserResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletResponseDTO;
import com.ugurxaslan.profit_tracker_backend.service.entityService.UserService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserService userService;
    private final WalletService walletService;

    // user
    public UserResponseDTO getUserById(Long id) {
        return userService.getUserById(id);
    }

    public UserResponseDTO getUserByUsername(String username) {
        return userService.getUserByUsername(username);
    }

    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO requestDTO) {
        return userService.updateUser(id, requestDTO);
    }

    public void deleteUser(Long id) {
        userService.deleteUser(id);
    }

    // wallet
    public WalletResponseDTO createWallet(String currentUsername, WalletRequestDTO requestDTO) {
        return walletService.createWallet(currentUsername, requestDTO);
    }

    public WalletResponseDTO updateWalletName(Long walletId, WalletRequestDTO requestDTO) {
        return walletService.updateWalletName(walletId, requestDTO);
    }

    public void deleteWallet(Long walletId) {
        walletService.deleteWallet(walletId);
    }
}
