package com.ugurxaslan.profit_tracker_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequestDTO {

    @NotBlank(message = "Wallet name must not be blank")
    @Size(min = 2, max = 50, message = "Wallet name must be between 2 and 50 characters")
    private String name;
}