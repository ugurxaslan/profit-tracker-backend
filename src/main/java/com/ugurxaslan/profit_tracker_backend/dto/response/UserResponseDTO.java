package com.ugurxaslan.profit_tracker_backend.dto.response;

import com.ugurxaslan.profit_tracker_backend.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailVerified;
    private UserStatus status;
    private Set<String> roles;
}