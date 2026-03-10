package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.request.CreateUserRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.UpdateUserRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.UserResponseDTO;
import com.ugurxaslan.profit_tracker_backend.mapper.UserMapper;
import com.ugurxaslan.profit_tracker_backend.model.User;
import com.ugurxaslan.profit_tracker_backend.model.Wallet;
import com.ugurxaslan.profit_tracker_backend.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final BCryptPasswordEncoder passwordEncoder;

	@Transactional
	public UserResponseDTO createUser(CreateUserRequestDTO requestDTO) {
		if (userRepository.existsByUsername(requestDTO.getUsername())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
		}

		if (userRepository.existsByEmail(requestDTO.getEmail())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
		}

		User user = userMapper.toEntityForCreate(requestDTO);
		user.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));

		// default wallet
		Wallet defaultWallet = Wallet.builder().build();
		defaultWallet.setUser(user);

		user.getWalletList().add(defaultWallet);

		User savedUser = userRepository.save(user);
		return userMapper.toResponse(savedUser);
	}

	@Transactional(readOnly = true)
	public UserResponseDTO getUserById(@NonNull Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		return userMapper.toResponse(user);
	}

	@Transactional(readOnly = true)
	public List<UserResponseDTO> getAllUsers() {
		return userRepository.findAll()
				.stream()
				.map(userMapper::toResponse)
				.toList();
	}

	public UserResponseDTO updateUser(@NonNull Long id, UpdateUserRequestDTO requestDTO) {

		User existingUser = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (!existingUser.getUsername().equalsIgnoreCase(requestDTO.getUsername())
				&& userRepository.existsByUsername(requestDTO.getUsername())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
		}

		if (!existingUser.getEmail().equalsIgnoreCase(requestDTO.getEmail())
				&& userRepository.existsByEmail(requestDTO.getEmail())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
		}

		userMapper.toEntityForUpdate(requestDTO, existingUser);
		if (StringUtils.hasText(requestDTO.getPassword())) {
			existingUser.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));
		}

		User updatedUser = userRepository.save(existingUser);
		return userMapper.toResponse(updatedUser);
	}

	public void deleteUser(@NonNull Long id) {
		if (!userRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		userRepository.deleteById(id);
	}
}
