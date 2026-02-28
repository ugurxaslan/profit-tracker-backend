package com.ugurxaslan.profit_tracker_backend.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "wallet_assets")
public class WalletAsset extends BaseEntity {

    @Builder.Default
    @Column(name = "total_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "average_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal averageCost = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "profit_loss", nullable = false, precision = 19, scale = 8)
    private BigDecimal profitLoss = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "p_l_percentage", nullable = false, precision = 19, scale = 8)
    private BigDecimal profitLossPercentage = BigDecimal.ZERO;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false, updatable = false)
    private Asset asset;

}
