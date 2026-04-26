package com.minichat.controller;

import com.minichat.entity.User;
import com.minichat.repository.UserRepository;
import com.minichat.dto.RegisterRequest;
import com.minichat.dto.RegisterResponse;
import com.minichat.dto.LoginRequest;
import com.minichat.dto.LoginResponse;
import com.minichat.dto.UserProfileDto;
import com.minichat.dto.UpdateProfileRequest;
import com.minichat.service.AuthService;
import com.minichat.exception.UnauthorizedException;
import com.minichat.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUserProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest updateRequest) {
        
        String currentUsername = authentication.getName();
        
        // Explicit authorization check: user can only update their own profile
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        // If trying to change username, ensure they're not changing to someone else's username
        if (!currentUser.getUsername().equals(updateRequest.getUsername())) {
            userRepository.findByUsername(updateRequest.getUsername())
                    .ifPresent(u -> {
                        throw new UserAlreadyExistsException("Username already in use");
                    });
        }
        
        // If trying to change email, ensure they're not changing to someone else's email
        if (!currentUser.getEmail().equals(updateRequest.getEmail())) {
            userRepository.findByEmail(updateRequest.getEmail())
                    .ifPresent(u -> {
                        throw new UserAlreadyExistsException("Email already in use");
                    });
        }
        
        return ResponseEntity.ok(authService.updateUserProfile(currentUsername, updateRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
