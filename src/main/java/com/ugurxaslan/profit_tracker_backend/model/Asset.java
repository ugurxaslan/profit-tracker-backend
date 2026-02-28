package com.ugurxaslan.profit_tracker_backend.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assets")
public class Asset extends BaseEntity {

    @Column(name = "name",nullable = false, unique = true,updatable = false)
    private String name;

    @Column(name = "symbol",nullable = false, unique = true,updatable = false)
    private String symbol;

    @Column(name = "current_price", nullable = false, precision = 19, scale = 2) 
    private BigDecimal currentPrice;
    
}
