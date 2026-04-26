package com.minichat.service;

import com.minichat.dto.RegisterRequest;
import com.minichat.dto.RegisterResponse;
import com.minichat.dto.LoginRequest;
import com.minichat.dto.LoginResponse;
import com.minichat.dto.UserProfileDto;
import com.minichat.dto.UpdateProfileRequest;
import com.minichat.entity.User;
import com.minichat.entity.RoleType;
import com.minichat.entity.Role;
import com.minichat.entity.Session;
import com.minichat.repository.UserRepository;
import com.minichat.repository.SessionRepository;
import com.minichat.exception.UserAlreadyExistsException;
import com.minichat.exception.UnauthorizedException;
import com.minichat.exception.InvalidPasswordException;
import com.minichat.exception.PasswordMismatchException;
import com.minichat.exception.RoleNotFoundException;
import com.minichat.repository.RoleRepository;
import com.minichat.util.PasswordValidator;
import com.minichat.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.time.Duration;

@Service
@Transactional
public class AuthService {

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    public RegisterResponse register(RegisterRequest request) {
        // Validate password strength
        if (!passwordValidator.isValidPassword(request.getPassword())) {
            throw new InvalidPasswordException(passwordValidator.getPasswordRequirements());
        }
        
        // Validate passwords match
        if (!request.isPasswordMatch()) {
            throw new PasswordMismatchException("Passwords do not match");
        }
        
        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }
        
        // Check duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        
        Role userRole = roleRepository.findByName(RoleType.USER)
            .orElseThrow(() -> new RoleNotFoundException("USER role not found"));
        user.setRoles(new HashSet<>(Set.of(userRole)));


        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getUsername(), savedUser.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getUsername(), savedUser.getId());

        saveSession(user, accessToken, refreshToken);

        return RegisterResponse.builder()
            .id(savedUser.getId())
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .createdAt(savedUser.getCreatedAt())
            .build();
    }

    public LoginResponse login(LoginRequest request) {
        System.out.println("Login attempt: " + request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        
        System.out.println("User found: " + user.getUsername());
        System.out.println("Password match: " + passwordEncoder.matches(request.getPassword(), user.getPasswordHash()));
        System.out.println("Is active: " + user.getIsActive());

        System.out.println("User found: " + user.getUsername());
        System.out.println("Raw password: " + request.getPassword());
        System.out.println("Hashed password: " + user.getPasswordHash());
        System.out.println("Password match: " + passwordEncoder.matches(request.getPassword(), user.getPasswordHash()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

        saveSession(user, accessToken, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Session not found"));

        if (!session.getActive() || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Session expired");
        }

        User user = session.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

        session.setActive(false);
        sessionRepository.save(session);

        saveSession(user, accessToken, newRefreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    public UserProfileDto getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .publicKey(user.getPublicKey())
                .fingerprint(user.getFingerprint())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .isActive(user.getIsActive())
                .build();
    }

    public UserProfileDto updateUserProfile(String username, UpdateProfileRequest updateRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Check if new email already exists (and it's not the same user)
        if (!user.getEmail().equals(updateRequest.getEmail())) {
            userRepository.findByEmail(updateRequest.getEmail())
                    .ifPresent(u -> {
                        throw new UserAlreadyExistsException("Email already in use");
                    });
        }

        user.setUsername(updateRequest.getUsername());
        user.setEmail(updateRequest.getEmail());

        User updatedUser = userRepository.save(user);

        return mapToUserProfileDto(updatedUser);
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .publicKey(user.getPublicKey())
                .fingerprint(user.getFingerprint())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .isActive(user.getIsActive())
                .build();
    }

    private void saveSession(User user, String accessToken, String refreshToken) {
        Session session = new Session();
        session.setUser(user);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(accessTokenExpiration)));
        session.setRefreshTokenExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpiration)));
        session.setActive(true);
        sessionRepository.save(session);
    }
}
