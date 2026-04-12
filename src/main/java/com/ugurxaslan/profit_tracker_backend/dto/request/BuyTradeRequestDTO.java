package com.ugurxaslan.profit_tracker_backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyTradeRequestDTO {

    @NotBlank(message = "Asset symbol must not be blank")
    private String assetSymbol;

    @NotNull(message = "Quantity must not be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Quantity must be greater than 0")
    @Digits(integer = 19, fraction = 2, message = "Quantity can have at most 2 decimal places")
    private BigDecimal quantity;

    @NotNull(message = "Unit price must not be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Unit price must be greater than 0")
    @Digits(integer = 19, fraction = 2, message = "Unit price can have at most 2 decimal places")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", inclusive = true, message = "Fee cannot be negative")
    @Digits(integer = 19, fraction = 2, message = "Fee can have at most 2 decimal places")
    private BigDecimal fee;

    private LocalDateTime transactionDate;

    private Boolean IsUseCash = true;
}
