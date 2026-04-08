package com.ugurxaslan.profit_tracker_backend.dto.response;

import java.math.BigDecimal;

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
public class WalletAssetResponseDTO {

    private Long id;
    private String assetSymbol;
    private BigDecimal totalCost;
    private BigDecimal averageCost;
    private BigDecimal totalValue;
    private BigDecimal quantity;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercentage;
    private Long walletId;
    private Long assetId;
}
