package com.ugurxaslan.profit_tracker_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8,updatable = false)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale =2,updatable = false)
    private BigDecimal unitCost;

    @Column(name = "total_cost", nullable = false, precision = 19, scale = 2,updatable = false)
    private BigDecimal totalCost;

    @Column(name = "fee", precision = 19, scale = 2,updatable = false)
    private BigDecimal fee;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

}
