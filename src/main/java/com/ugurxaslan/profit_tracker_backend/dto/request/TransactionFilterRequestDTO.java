package com.ugurxaslan.profit_tracker_backend.dto.request;

import java.time.LocalDateTime;

import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterRequestDTO {

    private String assetSymbol;

    private TransactionType transactionType;

    private LocalDateTime fromDate;

    private LocalDateTime toDate;

    @AssertTrue(message = "fromDate must be before or equal to toDate")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }
}
