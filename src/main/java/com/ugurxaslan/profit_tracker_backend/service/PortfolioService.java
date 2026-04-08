package com.ugurxaslan.profit_tracker_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ugurxaslan.profit_tracker_backend.dto.request.TransactionFilterRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.ClosedPositionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.OpenPositionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.TransactionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletAssetResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletResponseDTO;

import com.ugurxaslan.profit_tracker_backend.service.entityService.ClosedPositionService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.OpenPositionService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.TransactionService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.WalletAssetService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    // kollanıcının yapabileceği aksiyonlar
    // toplam portföy değeri tüm walletelrin toplamı şeklinde hesaplanır
    // walletsekmesinden wallet listesi ve değerleriyle birlikte
    // bir wallete tıkalr ve o wallet içinde bulunan wallet assetleri görür
    // değerleriyle birlikte kar zarar durumlarını görür ortalama
    // bir wallet assetet tıklar ve o waleltt assete ait openpositionları kar zarar
    // durumlaryıola birlikte görür
    // transaction sekmesinden geçmiş transactionları filtreli bir şekilde sorgusunu
    // yapaiblri
    // bir transactiona tıklar ve o transactiona ait detayları görür
    // closed posixyon sekmesinden geçmiş kar zarar analizi yapar
    // transaction sekmesi ve closed sekmesi wallet sekmesinin altındadır ynai
    // wallete göre görebilir bunları

    private final WalletService walletService;
    private final WalletAssetService walletAssetService;
    private final OpenPositionService openPositionService;
    private final ClosedPositionService closedPositionService;
    private final TransactionService transactionService;

    public Page<WalletResponseDTO> getWallets(String currentUsername, Pageable pageable) {
        return walletService.getAllWallets(currentUsername, pageable);
    }

    public Page<WalletAssetResponseDTO> getWalletAssets(Long walletId, Pageable pageable) {
        return walletAssetService.getWalletAssetsByWalletId(walletId, pageable);
    }

    public Page<OpenPositionResponseDTO> getOpenPositions(Long walletId, Long walletAssetId, Pageable pageable) {
        if (walletAssetId == null) {
            return openPositionService.getOpenPositionsByWalletId(walletId, pageable);
        }

        return openPositionService.getOpenPositionsByWalletAssetId(walletAssetId, pageable);
    }

    public Page<ClosedPositionResponseDTO> getClosedPositions(Long walletId, Long walletAssetId, Pageable pageable) {
        if (walletAssetId == null) {
            return closedPositionService.getClosedPositionsByWalletId(walletId, pageable);
        }

        return closedPositionService.getClosedPositionsByWalletAssetId(walletId, walletAssetId, pageable);
    }

    public Page<TransactionResponseDTO> getTransactions(Long walletId, TransactionFilterRequestDTO filter,
            Pageable pageable) {
        return transactionService.getTransactionsByFilter(walletId, filter, pageable);
    }
}
