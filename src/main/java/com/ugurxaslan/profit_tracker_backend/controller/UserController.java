package com.ugurxaslan.profit_tracker_backend.controller;

import com.ugurxaslan.profit_tracker_backend.dto.request.CreateUserRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.request.UpdateUserRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.UserResponseDTO;
import com.ugurxaslan.profit_tracker_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO requestDTO) {
		UserResponseDTO createdUser = userService.createUser(requestDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@userSecurity.isUserOwner(#id, authentication) or hasRole('ADMIN')")
	public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
		UserResponseDTO user = userService.getUserById(id);
		return ResponseEntity.ok(user);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
		List<UserResponseDTO> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@userSecurity.isUserOwner(#id, authentication) or hasRole('ADMIN')")
	public ResponseEntity<UserResponseDTO> updateUser(
			@PathVariable Long id,
			@Valid @RequestBody UpdateUserRequestDTO requestDTO) {
		UserResponseDTO updatedUser = userService.updateUser(id, requestDTO);
		return ResponseEntity.ok(updatedUser);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("@userSecurity.isUserOwner(#id, authentication) or hasRole('ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

}
