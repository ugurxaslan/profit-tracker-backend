package com.ugurxaslan.profit_tracker_backend.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wallets")
public class Wallet extends BaseEntity {

    @Builder.Default
    @Column(name = "wallet_name", nullable = false)
    private String walletName = "Default Wallet";

    @Transient
    @Builder.Default
    private BigDecimal cash = BigDecimal.ZERO;

    @Transient
    @Builder.Default
    private BigDecimal portfolioValue = BigDecimal.ZERO;

    @Transient
    @Builder.Default
    private BigDecimal totalValue = BigDecimal.ZERO;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;
}
