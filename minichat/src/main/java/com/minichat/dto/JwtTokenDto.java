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
public class JwtTokenDto {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    private LocalDateTime issuedAt;

    private LocalDateTime expiresAt;
}
