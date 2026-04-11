package com.ugurxaslan.profit_tracker_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetResponseDTO {

    private String name;
    private String symbol;
    private BigDecimal currentPrice;
    private boolean isCash;
}
