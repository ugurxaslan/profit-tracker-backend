package com.ugurxaslan.profit_tracker_backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClosedPositionResponseDTO {

    private Long id;
    private Long walletId;
    private Long buyTransactionId;
    private Long sellTransactionId;
    private String assetSymbol;
    private BigDecimal usedQuantity;
    private BigDecimal buyUnitPrice;
    private BigDecimal sellUnitPrice;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercentage;
    private LocalDateTime sellTransactionDate;
}
