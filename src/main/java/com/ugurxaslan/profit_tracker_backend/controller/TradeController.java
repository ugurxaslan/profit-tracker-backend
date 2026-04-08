package com.ugurxaslan.profit_tracker_backend.controller;

import com.ugurxaslan.profit_tracker_backend.dto.request.SellTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.BuyTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.CashTradeRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.TransactionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;
import com.ugurxaslan.profit_tracker_backend.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
    @PostMapping("/wallets/{walletId}/buy")
    public ResponseEntity<TransactionResponseDTO> buy(
            @PathVariable Long walletId,
            @Valid @RequestBody BuyTradeRequestDTO requestDTO) {
        TransactionResponseDTO response = tradeService.buy(walletId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication) and " +
            "(#requestDTO.optionalBuyTransactionIdForSell == null or " +
            "@transactionSecurity.isTransactionOwner(#requestDTO.optionalBuyTransactionIdForSell, authentication))")

    @PostMapping("/wallets/{walletId}/sell")
    public ResponseEntity<TransactionResponseDTO> sell(
            @PathVariable Long walletId,
            @Valid @RequestBody SellTradeRequestDTO requestDTO) {
        TransactionResponseDTO response = tradeService.sell(walletId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
    @PostMapping("/wallets/{walletId}/cash-in")
    public ResponseEntity<TransactionResponseDTO> cashIn(
            @PathVariable Long walletId,
            @Valid @RequestBody CashTradeRequestDTO requestDTO) {
        TransactionResponseDTO response = tradeService.cashIn(walletId, requestDTO, TransactionType.CASH_IN);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@walletSecurity.isWalletOwner(#walletId, authentication)")
    @PostMapping("/wallets/{walletId}/cash-out")
    public ResponseEntity<TransactionResponseDTO> cashOut(
            @PathVariable Long walletId,
            @Valid @RequestBody CashTradeRequestDTO requestDTO) {
        TransactionResponseDTO response = tradeService.cashOut(walletId, requestDTO, TransactionType.CASH_OUT);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
