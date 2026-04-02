package com.ugurxaslan.profit_tracker_backend.dto.response;

import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeResponseDTO {

    private Long id;
    private Long walletId;
    private String assetSymbol;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private BigDecimal fee;
    private LocalDateTime transactionDate;
}
