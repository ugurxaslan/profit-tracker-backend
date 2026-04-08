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
public class OpenPositionResponseDTO {

    private Long id;
    private Long walletId;
    private Long walletAssetId;
    private Long transactionId;
    private String assetSymbol;
    private BigDecimal remainingQuantity;
    private LocalDateTime transactionDate;

    // sadece dto da var get yapılınca hesaplanır
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private BigDecimal currentUnitPrice;
    private BigDecimal currentTotalValue;
    private BigDecimal unrealizedPL;
    private BigDecimal unrealizedPLP;

}
