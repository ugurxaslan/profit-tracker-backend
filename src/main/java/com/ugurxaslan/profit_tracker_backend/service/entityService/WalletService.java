package com.ugurxaslan.profit_tracker_backend.service.entityService;

import com.ugurxaslan.profit_tracker_backend.dto.request.WalletRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletResponseDTO;
import com.ugurxaslan.profit_tracker_backend.mapper.WalletMapper;
import com.ugurxaslan.profit_tracker_backend.model.User;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import com.ugurxaslan.profit_tracker_backend.repository.WalletRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final WalletAssetService walletAssetService;
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

        wallet.setCash(BigDecimal.ZERO);
        wallet.setTotalValue(BigDecimal.ZERO);
        wallet.setPortfolioValue(BigDecimal.ZERO);

        Wallet savedWallet = walletRepository.saveAndFlush(Objects.requireNonNull(wallet));
        return walletMapper.toResponse(savedWallet);
    }

    @Transactional
    public WalletResponseDTO getWalletById(@NonNull Long walletId) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        recalculateWalletTotals(wallet);
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public List<WalletResponseDTO> getAllWallets(@NonNull String currentUsername) {
        return walletRepository.findAllByUser_Username(currentUsername)
                .stream()
                .peek(this::recalculateWalletTotals)
                .map(walletMapper::toResponse)
                .toList();
    }

    @Transactional
    public Page<WalletResponseDTO> getAllWallets(@NonNull String currentUsername, @NonNull Pageable pageable) {
        return walletRepository.findAllByUser_Username(currentUsername, pageable)
                .map(wallet -> {
                    recalculateWalletTotals(wallet);
                    return walletMapper.toResponse(wallet);
                });
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

    @Transactional
    public Wallet syncWallet(@NonNull Long walletId) {
        Wallet wallet = getWalletEntityById(walletId);
        recalculateWalletTotals(wallet);
        return walletRepository.saveAndFlush(Objects.requireNonNull(wallet));
    }

    @Transactional
    private void recalculateWalletTotals(@NonNull Wallet wallet) {
        List<WalletAsset> walletAssets = walletAssetService.getWalletAssetsByWalletId(wallet.getId());

        BigDecimal newCashBalance = walletAssets.stream()
                .filter(wa -> wa.getAsset().getSymbol().equalsIgnoreCase("TRY"))
                .map(wa -> wa.getTotalCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newPortfolioValue = walletAssets.stream()
                .filter(wa -> !wa.getAsset().getSymbol().equalsIgnoreCase("TRY"))
                .map(wa -> wa.getTotalValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Recalculating wallet totals for walletId={}, newCashBalance={}, newPortfolioValue={}",
                wallet.getId(), newCashBalance, newPortfolioValue);
        wallet.setCash(newCashBalance);
        wallet.setPortfolioValue(newPortfolioValue);
        wallet.setTotalValue(newCashBalance.add(newPortfolioValue));
    }

}