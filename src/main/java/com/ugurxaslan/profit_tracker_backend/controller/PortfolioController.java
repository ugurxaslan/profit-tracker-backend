package com.ugurxaslan.profit_tracker_backend.controller;

import com.ugurxaslan.profit_tracker_backend.dto.request.TransactionFilterRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.ClosedPositionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.OpenPositionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.TransactionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletAssetResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.WalletResponseDTO;
import com.ugurxaslan.profit_tracker_backend.service.PortfolioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

        private final PortfolioService portfolioService;

        @GetMapping("/wallets")
        public ResponseEntity<Page<WalletResponseDTO>> getWallets(
                        Authentication authentication,
                        Pageable pageable) {
                Page<WalletResponseDTO> wallets = portfolioService.getWallets(authentication.getName(), pageable);
                return ResponseEntity.ok(wallets);
        }

        @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
        @GetMapping("/wallets/{walletId}")
        public ResponseEntity<WalletResponseDTO> getWallet(
                        @PathVariable Long walletId) {
                WalletResponseDTO wallet = portfolioService.getWallet(walletId);
                return ResponseEntity.ok(wallet);
        }

        @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
        @GetMapping("/wallets/{walletId}/assets")
        public ResponseEntity<Page<WalletAssetResponseDTO>> getWalletAssets(
                        @PathVariable Long walletId,
                        Pageable pageable) {
                Page<WalletAssetResponseDTO> walletAssets = portfolioService.getWalletAssets(walletId, pageable);
                return ResponseEntity.ok(walletAssets);
        }

        @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication) and"
                        + "(#walletAssetId == null or @walletAssetSecurity.isWalletAssetOwner(#walletAssetId, #walletId, authentication))")
        @GetMapping("/wallets/{walletId}/open-positions")
        public ResponseEntity<Page<OpenPositionResponseDTO>> getOpenPositions(
                        @PathVariable Long walletId,
                        @RequestParam(required = false) Long walletAssetId,
                        Pageable pageable) {
                Page<OpenPositionResponseDTO> openPositions = portfolioService.getOpenPositions(walletId, walletAssetId,
                                pageable);
                return ResponseEntity.ok(openPositions);
        }

        @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication) and"
                        + "(#walletAssetId == null or @walletAssetSecurity.isWalletAssetOwner(#walletAssetId, #walletId, authentication))")
        @GetMapping("/wallets/{walletId}/closed-positions")
        public ResponseEntity<Page<ClosedPositionResponseDTO>> getClosedPositions(
                        @PathVariable Long walletId,
                        @RequestParam(required = false) Long walletAssetId,
                        Pageable pageable) {
                Page<ClosedPositionResponseDTO> closedPositions = portfolioService.getClosedPositions(walletId,
                                walletAssetId,
                                pageable);
                return ResponseEntity.ok(closedPositions);
        }

        @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
        @PostMapping("/wallets/{walletId}/transactions/search")
        public ResponseEntity<Page<TransactionResponseDTO>> getTransactions(
                        @PathVariable Long walletId,
                        @Valid @RequestBody TransactionFilterRequestDTO filter,
                        Pageable pageable) {
                Page<TransactionResponseDTO> transactions = portfolioService.getTransactions(walletId, filter,
                                pageable);
                return ResponseEntity.ok(transactions);
        }

        @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication) and " +
                        "@transactionSecurity.isTransactionOwner(#walletId, #transactionId)")
        @GetMapping("/wallets/{walletId}/transactions/{transactionId}")
        public ResponseEntity<TransactionResponseDTO> getTransaction(
                        @PathVariable Long walletId,
                        @PathVariable Long transactionId) {
                TransactionResponseDTO transaction = portfolioService.getTransaction(transactionId);
                return ResponseEntity.ok(transaction);
        }
}
