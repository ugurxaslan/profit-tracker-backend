package com.ugurxaslan.profit_tracker_backend.model;

import java.math.BigDecimal;

import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "closed_positions")
public class ClosedPosition extends BaseEntity {
    @NotNull(message = "Used quantity must not be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Used quantity must be greater than 0")
    @Column(name = "used_quantity", nullable = false, precision = 19, scale = 2)
    private BigDecimal usedQuantity;

    @NotNull(message = "Asset must not be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Buy unit price must be greater than 0")
    @Column(name = "buy_unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal buyUnitPrice;

    @NotNull(message = "Asset must not be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Sell unit price must be greater than 0")
    @Column(name = "sell_unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal sellUnitPrice;

    @NotNull(message = "Profit/loss must not be null")
    @Column(name = "profit_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal profitLoss;

    @NotNull(message = "Profit/loss percentage must not be null")
    @Column(name = "profit_loss_percentage", nullable = false, precision = 19, scale = 2)
    private BigDecimal profitLossPercentage;

    // Relationships

    @NotNull(message = "Buy transaction must not be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "buy_transaction_id", nullable = false)
    private Transaction buyTransaction;

    @NotNull(message = "Sell transaction must not be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "sell_transaction_id", nullable = false)
    private Transaction sellTransaction;

    @NotNull(message = "Wallet asset must not be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @AssertTrue(message = "Buy transaction must be a BUY transaction")
    public boolean isBuyTransactionValid() {
        return buyTransaction != null && buyTransaction.getTransactionType() == TransactionType.BUY;
    }

    @AssertTrue(message = "Sell transaction must be a SELL transaction")
    public boolean isSellTransactionValid() {
        return sellTransaction != null && sellTransaction.getTransactionType() == TransactionType.SELL;
    }

}
