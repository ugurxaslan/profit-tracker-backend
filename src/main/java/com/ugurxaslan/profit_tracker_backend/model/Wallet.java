package com.ugurxaslan.profit_tracker_backend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    @Column(name = "cash", nullable = false, precision = 19, scale = 2)
    private BigDecimal cash = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "portfolio_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal portfolioValue = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "wallet", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "wallet", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WalletAsset> walletAssets = new ArrayList<>();
}
