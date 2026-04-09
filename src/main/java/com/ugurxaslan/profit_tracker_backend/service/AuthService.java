package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.request.LoginRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.CreateUserRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.RefreshTokenRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.LoginResponseDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.UserResponseDTO;
import com.ugurxaslan.profit_tracker_backend.mapper.UserMapper;
import com.ugurxaslan.profit_tracker_backend.model.User;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponseDTO signup(CreateUserRequestDTO requestDTO) {
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
        }

        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        Wallet defaultWallet = Wallet.builder()
                .walletName("Default Wallet")
                .build();

        User userToCreate = userMapper.toEntityForCreate(requestDTO);
        defaultWallet.setUser(userToCreate);
        userToCreate.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));
        userToCreate.getWalletList().add(defaultWallet);

        User createdUser = userRepository.save(userToCreate);
        return userMapper.toResponse(createdUser);
    }

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

        return buildLoginResponse(user);
    }

    public LoginResponseDTO refresh(RefreshTokenRequestDTO requestDTO) {
        if (!jwtService.isRefreshTokenValid(requestDTO.getRefreshToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String userName = jwtService.extractSubject(requestDTO.getRefreshToken());
        User user;

        try {
            user = userService.getUserEntityByUsername(userName);
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        return buildLoginResponse(user);
    }

    private LoginResponseDTO buildLoginResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessExpirationMs())
                .refreshExpiresIn(jwtService.getRefreshExpirationMs())
                .build();
    }
}
