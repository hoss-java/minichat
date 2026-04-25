package com.minichat.controller;

import com.minichat.dto.RegisterRequest;
import com.minichat.dto.LoginRequest;
import com.minichat.dto.LoginResponse;
import com.minichat.dto.UserProfileDto;
import com.minichat.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
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
            @Valid @RequestBody UserProfileDto profileDto) {
        return ResponseEntity.ok(authService.updateUserProfile(authentication.getName(), profileDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
