package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.request.LoginRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.LoginResponseDTO;
import com.ugurxaslan.profit_tracker_backend.model.User;
import com.ugurxaslan.profit_tracker_backend.security.JwtService;
import com.ugurxaslan.profit_tracker_backend.service.entityService.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        User user;
        try {
            user = userService.getUserEntityByUsername(requestDTO.getUsername());
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getUsername());

        return LoginResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }
}
