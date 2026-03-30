package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.request.WalletRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletResponseDTO;
import com.ugurxaslan.profit_tracker_backend.mapper.WalletMapper;
import com.ugurxaslan.profit_tracker_backend.model.User;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.repository.UserRepository;
import com.ugurxaslan.profit_tracker_backend.repository.WalletRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletResponseDTO createWallet(@NonNull String currentUsername, @NonNull WalletRequestDTO requestDTO) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String requestedName = requestDTO.getName().trim();
        if (walletRepository.existsByUser_UsernameAndWalletName(currentUsername, requestedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wallet name already exists");
        }

        Wallet wallet = Wallet.builder()
                .walletName(requestedName)
                .user(user)
                .build();

        // TODO: Recalculate wallet balances before save.

        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletById(@NonNull Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        // TODO: Recalculate wallet balances before response.
        return walletMapper.toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponseDTO> getAllWallets(@NonNull String currentUsername) {
        return walletRepository.findAllByUser_Username(currentUsername)
                .stream()
                // TODO: Recalculate wallet balances for each wallet before response.
                .map(walletMapper::toResponse)
                .toList();
    }

    public WalletResponseDTO updateWallet(@NonNull Long walletId, @NonNull WalletRequestDTO requestDTO) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        String currentUsername = wallet.getUser().getUsername();

        String requestedName = requestDTO.getName().trim();

        if (wallet.getWalletName().equalsIgnoreCase(requestedName)) {
            return walletMapper.toResponse(wallet);
        }

        if (walletRepository.existsByUser_UsernameAndWalletName(currentUsername, requestedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wallet name already exists");
        }

        wallet.setWalletName(requestedName);
        Wallet updatedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(updatedWallet);
    }

    public void deleteWallet(@NonNull Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        walletRepository.delete(wallet);
    }
}
