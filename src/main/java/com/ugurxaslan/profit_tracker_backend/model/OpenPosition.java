package com.ugurxaslan.profit_tracker_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "open_positions")
public class OpenPosition extends BaseEntity {

    @NotNull(message = "Remaining quantity must not be null")
    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingQuantity;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_asset_id", nullable = false)
    private WalletAsset walletAsset;

    @AssertFalse
    private boolean isTransactionTypeInvalid() {
        return transaction != null && transaction.getTransactionType() == TransactionType.SELL
                || transaction.getTransactionType() == TransactionType.CASH_OUT;
    }
}