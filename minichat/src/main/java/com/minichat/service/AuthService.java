package com.minichat.service;

import com.minichat.dto.RegisterRequest;
import com.minichat.dto.LoginRequest;
import com.minichat.dto.LoginResponse;
import com.minichat.dto.UserProfileDto;
import com.minichat.entity.User;
import com.minichat.entity.RoleType;
import com.minichat.entity.Role;
import com.minichat.entity.Session;
import com.minichat.repository.UserRepository;
import com.minichat.repository.SessionRepository;
import com.minichat.exception.UserAlreadyExistsException;
import com.minichat.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        
        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setName(RoleType.USER);
        user.setRoles(new HashSet<>(Set.of(userRole)));

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        saveSession(savedUser, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveSession(user, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Session not found"));

        if (!session.getActive() || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Session expired");
        }

        User user = session.getUser();
        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        session.setActive(false);
        sessionRepository.save(session);

        saveSession(user, newRefreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    public UserProfileDto getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fingerprint(user.getFingerprint())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    public UserProfileDto updateUserProfile(String email, UserProfileDto profileDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (profileDto.getUsername() != null) {
            user.setUsername(profileDto.getUsername());
        }

        User updatedUser = userRepository.save(user);

        return UserProfileDto.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .username(updatedUser.getUsername())
                .fingerprint(updatedUser.getFingerprint())
                .createdAt(updatedUser.getCreatedAt())
                .lastLogin(updatedUser.getLastLogin())
                .build();
    }

    private void saveSession(User user, String refreshToken) {
        Session session = new Session();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setActive(true);
        sessionRepository.save(session);
    }
}
