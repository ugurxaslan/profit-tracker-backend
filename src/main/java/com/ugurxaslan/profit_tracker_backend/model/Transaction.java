package com.ugurxaslan.profit_tracker_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "quantity", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal unitCost;

    @Column(name = "total_cost", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal totalCost;

    @Column(name = "fee", precision = 19, scale = 2)
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

    @OneToOne(mappedBy = "transaction", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OpenPosition openPosition;

    @Builder.Default
    @OneToMany(mappedBy = "buyTransaction", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClosedPosition> buyClosedPositions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "sellTransaction", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClosedPosition> sellClosedPositions = new ArrayList<>();
}
