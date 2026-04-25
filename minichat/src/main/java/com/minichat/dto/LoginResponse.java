package com.minichat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private Long userId;

    private String email;

    private String username;

    private LocalDateTime lastLogin;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;
}
