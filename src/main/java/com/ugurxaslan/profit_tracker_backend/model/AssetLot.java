package com.ugurxaslan.profit_tracker_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "asset_lots")
public class AssetLot extends BaseEntity {

    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal remainingQuantity;

    @Builder.Default
    @Column(name = "is_closed", nullable = false)
    private boolean isClosed = false;

    // Relationships
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @OneToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "buy_transaction_id",nullable = false)
    private Transaction buyTransaction;
}