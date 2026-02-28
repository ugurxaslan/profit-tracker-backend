package com.ugurxaslan.profit_tracker_backend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WalletAsset> assetList = new ArrayList<>();


}
