package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.request.WalletRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletResponseDTO;
import com.ugurxaslan.profit_tracker_backend.mapper.WalletMapper;
import com.ugurxaslan.profit_tracker_backend.model.User;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.repository.WalletRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletResponseDTO createWallet(@NonNull String currentUsername, @NonNull WalletRequestDTO requestDTO) {
        User user = userService.getUserEntityByUsername(currentUsername);

        String requestedName = requestDTO.getName().trim();
        if (walletRepository.existsByUser_UsernameAndWalletName(currentUsername, requestedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wallet name already exists");
        }

        Wallet wallet = Wallet.builder()
                .walletName(requestedName)
                .user(user)
                .build();
        Wallet savedWallet = recalculateWalletTotalsAndSave(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    @Transactional
    public WalletResponseDTO getWalletById(@NonNull Long walletId) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        recalculateWalletTotalsAndSave(wallet);
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public List<WalletResponseDTO> getAllWallets(@NonNull String currentUsername) {
        return walletRepository.findAllByUser_Username(currentUsername)
                .stream()
                .peek(this::recalculateWalletTotalsAndSave)
                .map(walletMapper::toResponse)
                .toList();
    }

    @Transactional
    public WalletResponseDTO updateWalletName(@NonNull Long walletId, @NonNull WalletRequestDTO requestDTO) {
        Wallet wallet = getWalletEntityById(walletId);

        if (walletRepository.existsByUser_UsernameAndWalletName(wallet.getUser().getUsername(),
                requestDTO.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wallet name already exists");
        }

        wallet.setWalletName(requestDTO.getName().trim());
        Wallet savedWallet = Objects.requireNonNull(walletRepository.save(wallet));
        return walletMapper.toResponse(savedWallet);
    }

    @Transactional
    public void deleteWallet(@NonNull Long walletId) {

        Wallet wallet = getWalletEntityById(walletId);

        walletRepository.delete(Objects.requireNonNull(wallet));
    }

    // servisler arası entity aktarımı için
    @Transactional
    public Wallet getWalletEntityById(@NonNull Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    public Wallet syncWallet(@NonNull Long walletId) {
        Wallet wallet = getWalletEntityById(walletId);
        Wallet updatedWallet = this.recalculateWalletTotalsAndSave(wallet);
        return updatedWallet;
    }

    @Transactional
    private Wallet recalculateWalletTotalsAndSave(@NonNull Wallet wallet) {
        BigDecimal newCashBalance = wallet.getWalletAssets().stream()
                .filter(wa -> wa.getAsset().getSymbol().equalsIgnoreCase("TRY"))
                .map(wa -> wa.getTotalCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newPortfolioValue = wallet.getWalletAssets().stream()
                .filter(wa -> !wa.getAsset().getSymbol().equalsIgnoreCase("TRY"))
                .map(wa -> wa.getTotalValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Recalculating wallet totals for walletId={}, newCashBalance={}, newPortfolioValue={}",
                wallet.getId(), newCashBalance, newPortfolioValue);
        wallet.setCash(newCashBalance);
        wallet.setPortfolioValue(newPortfolioValue);
        wallet.setTotalValue(newCashBalance.add(newPortfolioValue));
        Wallet savedWallet = Objects.requireNonNull(walletRepository.save(wallet));
        return savedWallet;
    }

}