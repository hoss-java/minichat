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
import com.minichat.repository.RoleRepository;
import com.minichat.exception.UserAlreadyExistsException;
import com.minichat.exception.UnauthorizedException;
import com.minichat.exception.InvalidPasswordException;
import com.minichat.exception.PasswordMismatchException;
import com.minichat.exception.RoleNotFoundException;
import com.minichat.util.PasswordValidator;
import com.minichat.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService class.
 * Tests all public methods in isolation with mocked external dependencies.
 * Covers happy path, edge cases, and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
public class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UpdateProfileRequest updateProfileRequest;
    private User testUser;
    private Role userRole;
    private Session testSession;

    @BeforeEach
    void setUp() {
        // Set JWT expiration values using reflection
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 86400000L);

        // Setup RegisterRequest using actual constructor
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setPasswordConfirm("SecurePass123!");

        // Setup LoginRequest using actual constructor
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("SecurePass123!");

        // Setup UpdateProfileRequest using builder (it has @Builder)
        updateProfileRequest = UpdateProfileRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .build();

        // Setup User using builder
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now().minusHours(1))
                .roles(new HashSet<>())
                .build();

        // Setup Role using builder
        userRole = Role.builder()
                .id(1L)
                .name(RoleType.USER)
                .description("User role")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup Session using actual constructor
        testSession = new Session();
        testSession.setId(1L);
        testSession.setUser(testUser);
        testSession.setAccessToken("access-token-123");
        testSession.setRefreshToken("refresh-token-123");
        testSession.setActive(true);
        testSession.setExpiresAt(LocalDateTime.now().plusHours(1));
        testSession.setRefreshTokenExpiresAt(LocalDateTime.now().plusDays(1));
        testSession.setCreatedAt(LocalDateTime.now());
    }

    // ============== REGISTER TESTS ==============

    @DisplayName("register - Happy Path: Valid registration should create user and return tokens")
    @Test
    void testRegisterSuccess() {
        // Scenario: User provides valid registration details
        when(passwordValidator.isValidPassword("SecurePass123!")).thenReturn(true);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken("testuser", 1L)).thenReturn("access-token-123");
        when(jwtTokenProvider.generateRefreshToken("testuser", 1L)).thenReturn("refresh-token-123");

        // Execute
        RegisterResponse response = authService.register(registerRequest);

        // Expected: User created, tokens generated, session saved
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("access-token-123", response.getAccessToken());
        assertEquals("refresh-token-123", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
        verify(sessionRepository).save(any(Session.class));
    }

    @DisplayName("register - Invalid password throws InvalidPasswordException")
    @Test
    void testRegisterInvalidPassword() {
        // Scenario: Password does not meet strength requirements
        when(passwordValidator.isValidPassword("SecurePass123!")).thenReturn(false);
        when(passwordValidator.getPasswordRequirements()).thenReturn("Password must contain uppercase, lowercase, number, special char");

        // Expected: InvalidPasswordException thrown
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            authService.register(registerRequest);
        });

        assertNotNull(exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("register - Password mismatch throws PasswordMismatchException")
    @Test
    void testRegisterPasswordMismatch() {
        // Scenario: Confirm password does not match password
        registerRequest.setPasswordConfirm("DifferentPass123!");
        when(passwordValidator.isValidPassword("SecurePass123!")).thenReturn(true);

        // Expected: PasswordMismatchException thrown
        PasswordMismatchException exception = assertThrows(PasswordMismatchException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("register - Email already exists throws UserAlreadyExistsException")
    @Test
    void testRegisterDuplicateEmail() {
        // Scenario: Email already registered in system
        when(passwordValidator.isValidPassword("SecurePass123!")).thenReturn(true);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Expected: UserAlreadyExistsException thrown
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("register - Username already exists throws UserAlreadyExistsException")
    @Test
    void testRegisterDuplicateUsername() {
        // Scenario: Username already registered in system
        when(passwordValidator.isValidPassword("SecurePass123!")).thenReturn(true);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Expected: UserAlreadyExistsException thrown
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Username already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("register - USER role not found throws RoleNotFoundException")
    @Test
    void testRegisterRoleNotFound() {
        // Scenario: USER role does not exist in database
        when(passwordValidator.isValidPassword("SecurePass123!")).thenReturn(true);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.empty());

        // Expected: RoleNotFoundException thrown
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("USER role not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ============== LOGIN TESTS ==============

    @DisplayName("login - Happy Path: Valid credentials return tokens")
    @Test
    void testLoginSuccess() {
        // Scenario: User exists and password is correct
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SecurePass123!", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("testuser", 1L)).thenReturn("access-token-123");
        when(jwtTokenProvider.generateRefreshToken("testuser", 1L)).thenReturn("refresh-token-123");

        // Execute
        LoginResponse response = authService.login(loginRequest);

        // Expected: Login successful, tokens generated
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("access-token-123", response.getAccessToken());
        assertEquals("refresh-token-123", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
        verify(sessionRepository).save(any(Session.class));
    }

    @DisplayName("login - User not found throws UnauthorizedException")
    @Test
    void testLoginUserNotFound() {
        // Scenario: Username does not exist
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(sessionRepository, never()).save(any());
    }

    @DisplayName("login - Invalid password throws UnauthorizedException")
    @Test
    void testLoginInvalidPassword() {
        // Scenario: Password does not match hash
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SecurePass123!", "$2a$10$hashedPassword")).thenReturn(false);

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(sessionRepository, never()).save(any());
    }

    @DisplayName("login - Inactive user throws UnauthorizedException")
    @Test
    void testLoginInactiveUser() {
        // Scenario: User account is inactive
        testUser.setIsActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SecurePass123!", "$2a$10$hashedPassword")).thenReturn(true);

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User account is inactive", exception.getMessage());
        verify(sessionRepository, never()).save(any());
    }

    // ============== REFRESH TOKEN TESTS ==============

    @DisplayName("refreshToken - Happy Path: Valid refresh token generates new tokens")
    @Test
    void testRefreshTokenSuccess() {
        // Scenario: Valid refresh token with active session
        when(jwtTokenProvider.validateRefreshToken("refresh-token-123")).thenReturn(true);
        when(sessionRepository.findByRefreshToken("refresh-token-123")).thenReturn(Optional.of(testSession));
        when(jwtTokenProvider.generateAccessToken("testuser", 1L)).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser", 1L)).thenReturn("new-refresh-token");

        // Execute
        LoginResponse response = authService.refreshToken("refresh-token-123");

        // Expected: New tokens generated, old session deactivated
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(sessionRepository, times(2)).save(any(Session.class));
    }

    @DisplayName("refreshToken - Invalid token throws UnauthorizedException")
    @Test
    void testRefreshTokenInvalid() {
        // Scenario: Refresh token is invalid
        when(jwtTokenProvider.validateRefreshToken("invalid-token")).thenReturn(false);

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.refreshToken("invalid-token");
        });

        assertEquals("Invalid or expired refresh token", exception.getMessage());
        verify(sessionRepository, never()).save(any());
    }

    @DisplayName("refreshToken - Session not found throws UnauthorizedException")
    @Test
    void testRefreshTokenSessionNotFound() {
        // Scenario: Valid token but no corresponding session
        when(jwtTokenProvider.validateRefreshToken("refresh-token-123")).thenReturn(true);
        when(sessionRepository.findByRefreshToken("refresh-token-123")).thenReturn(Optional.empty());

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.refreshToken("refresh-token-123");
        });

        assertEquals("Session not found", exception.getMessage());
    }

    @DisplayName("refreshToken - Inactive session throws UnauthorizedException")
    @Test
    void testRefreshTokenInactiveSession() {
        // Scenario: Session is marked inactive
        testSession.setActive(false);
        when(jwtTokenProvider.validateRefreshToken("refresh-token-123")).thenReturn(true);
        when(sessionRepository.findByRefreshToken("refresh-token-123")).thenReturn(Optional.of(testSession));

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.refreshToken("refresh-token-123");
        });

        assertEquals("Session expired", exception.getMessage());
    }


    @DisplayName("refreshToken - Expired session throws UnauthorizedException")
    @Test
    void testRefreshTokenExpiredSession() {
        // Scenario: Session expiration time has passed
        testSession.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(jwtTokenProvider.validateRefreshToken("refresh-token-123")).thenReturn(true);
        when(sessionRepository.findByRefreshToken("refresh-token-123")).thenReturn(Optional.of(testSession));

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.refreshToken("refresh-token-123");
        });

        assertEquals("Session expired", exception.getMessage());
    }

    // ============== GET CURRENT USER PROFILE TESTS ==============

    @DisplayName("getCurrentUserProfile - Happy Path: Returns user profile DTO")
    @Test
    void testGetCurrentUserProfileSuccess() {
        // Scenario: User exists and profile is retrieved
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Execute
        UserProfileDto profile = authService.getCurrentUserProfile("testuser");

        // Expected: User profile returned with correct data
        assertNotNull(profile);
        assertEquals(1L, profile.getId());
        assertEquals("testuser", profile.getUsername());
        assertEquals("test@example.com", profile.getEmail());
        assertTrue(profile.getIsActive());
        verify(userRepository).findByUsername("testuser");
    }

    @DisplayName("getCurrentUserProfile - User not found throws UnauthorizedException")
    @Test
    void testGetCurrentUserProfileUserNotFound() {
        // Scenario: Username does not exist
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.getCurrentUserProfile("nonexistent");
        });

        assertEquals("User not found", exception.getMessage());
    }

    // ============== UPDATE USER PROFILE TESTS ==============

    @DisplayName("updateUserProfile - Happy Path: Successfully updates user profile")
    @Test
    void testUpdateUserProfileSuccess() {
        // Scenario: User updates profile with new username and email
        User updatedUser = User.builder()
                .id(1L)
                .username("updateduser")
                .email("updated@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now().minusHours(1))
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Execute
        UserProfileDto profile = authService.updateUserProfile("testuser", updateProfileRequest);

        // Expected: Profile updated and returned
        assertNotNull(profile);
        assertEquals("updateduser", profile.getUsername());
        assertEquals("updated@example.com", profile.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @DisplayName("updateUserProfile - User not found throws UnauthorizedException")
    @Test
    void testUpdateUserProfileUserNotFound() {
        // Scenario: Username does not exist
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Expected: UnauthorizedException thrown
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.updateUserProfile("nonexistent", updateProfileRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("updateUserProfile - New email already in use throws UserAlreadyExistsException")
    @Test
    void testUpdateUserProfileEmailAlreadyExists() {
        // Scenario: New email is already registered to another user
        User anotherUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .email("updated@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.of(anotherUser));

        // Expected: UserAlreadyExistsException thrown
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.updateUserProfile("testuser", updateProfileRequest);
        });

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @DisplayName("updateUserProfile - Same email as current user does not throw exception")
    @Test
    void testUpdateUserProfileSameEmail() {
        // Scenario: User updates other fields but keeps same email
        UpdateProfileRequest sameEmailRequest = UpdateProfileRequest.builder()
                .username("updateduser")
                .email("test@example.com")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .username("updateduser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now().minusHours(1))
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Execute
        UserProfileDto profile = authService.updateUserProfile("testuser", sameEmailRequest);

        // Expected: Profile updated successfully without email check
        assertNotNull(profile);
        assertEquals("updateduser", profile.getUsername());
        assertEquals("test@example.com", profile.getEmail());
        verify(userRepository).save(any(User.class));
    }
}

