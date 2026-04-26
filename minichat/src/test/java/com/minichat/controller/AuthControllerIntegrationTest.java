package com.minichat.controller;

import com.minichat.dto.RegisterRequest;
import com.minichat.dto.RegisterResponse;
import com.minichat.dto.LoginRequest;
import com.minichat.dto.LoginResponse;
import com.minichat.dto.UpdateProfileRequest;
import com.minichat.entity.User;
import com.minichat.entity.Role;
import com.minichat.entity.Session;
import com.minichat.entity.RoleType;
import com.minichat.repository.RoleRepository;
import com.minichat.repository.UserRepository;
import com.minichat.repository.SessionRepository;
import com.minichat.security.JwtTokenProvider;
import com.minichat.service.AuthService;
import com.minichat.util.PasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;
import java.time.LocalDateTime;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for AuthController endpoints
 * 
 * Tests cover all authentication scenarios including registration, login, and profile updates.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String ME_ENDPOINT = "/api/auth/me";
    private static final String PROFILE_ENDPOINT = "/api/auth/profile";
    private static final String REFRESH_ENDPOINT = "/api/auth/refresh";
    private static final String VALID_EMAIL = "testuser@example.com";
    private static final String VALID_USERNAME = "validuser";
    private static final String VALID_PASSWORD = "ValidPass123!";

    @BeforeEach
    public void setUp() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        
        // Create the USER role only if it doesn't exist
        if (roleRepository.findByName(RoleType.USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(RoleType.USER);
            userRole.setCreatedAt(LocalDateTime.now());
            roleRepository.save(userRole);
        }
    }

    // ===== REGISTER ENDPOINT TESTS =====

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpoint {

        // ===== HAPPY PATH =====
        @Test
        @DisplayName("Successfully register with valid data returns 201 Created")
        public void testRegisterSuccess() throws Exception {
            RegisterRequest request = buildValidRegisterRequest();

            mockMvc.perform(post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.username", equalTo(VALID_USERNAME)))
                    .andExpect(jsonPath("$.email", equalTo(VALID_EMAIL)))
                    .andExpect(jsonPath("$.accessToken", notNullValue()))
                    .andExpect(jsonPath("$.refreshToken", notNullValue()))
                    .andExpect(jsonPath("$.createdAt", notNullValue()));

            User savedUser = userRepository.findByUsername(VALID_USERNAME).orElse(null);
            Assertions.assertNotNull(savedUser, "User should be saved in database");
            Assertions.assertEquals(VALID_EMAIL, savedUser.getEmail());
            Assertions.assertTrue(savedUser.getIsActive());
        }

        // ===== VALIDATION FAILURES - BLANK/MISSING FIELDS =====

        @ParameterizedTest
        @DisplayName("Reject registration with blank or missing required fields")
        @MethodSource("provideBlankFieldScenarios")
        public void testRegisterWithBlankFields(String email, String username, String password,
                                               String passwordConfirm, String scenarioDescription) throws Exception {
            // Scenario: User attempts registration with missing required fields ({scenarioDescription})
            RegisterRequest request = new RegisterRequest();
            request.setEmail(email);
            request.setUsername(username);
            request.setPassword(password);
            request.setPasswordConfirm(passwordConfirm);

            // Expected: HTTP 400 Bad Request
            mockMvc.perform(post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<Arguments> provideBlankFieldScenarios() {
            String validPass = "ValidPass123!";
            return Stream.of(
                    Arguments.of(null, "validuser", validPass, validPass, "null email"),
                    Arguments.of("", "validuser", validPass, validPass, "empty email"),
                    Arguments.of("user@example.com", null, validPass, validPass, "null username"),
                    Arguments.of("user@example.com", "", validPass, validPass, "empty username"),
                    Arguments.of("user@example.com", "validuser", null, validPass, "null password"),
                    Arguments.of("user@example.com", "validuser", "", validPass, "empty password"),
                    Arguments.of("user@example.com", "validuser", validPass, null, "null passwordConfirm"),
                    Arguments.of("user@example.com", "validuser", validPass, "", "empty passwordConfirm")
            );
        }

        // ===== VALIDATION FAILURES - INVALID EMAIL FORMAT =====

        @ParameterizedTest
        @DisplayName("Reject registration with invalid email format")
        @MethodSource("provideInvalidEmailScenarios")
        public void testRegisterWithInvalidEmail(String email, String scenarioDescription) throws Exception {
            // Scenario: User attempts registration with malformed email ({scenarioDescription})
            RegisterRequest request = new RegisterRequest();
            request.setEmail(email);
            request.setUsername(VALID_USERNAME);
            request.setPassword(VALID_PASSWORD);
            request.setPasswordConfirm(VALID_PASSWORD);

            // Expected: HTTP 400 Bad Request
            mockMvc.perform(post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<Arguments> provideInvalidEmailScenarios() {
            return Stream.of(
                    Arguments.of("invalid-email", "missing @ symbol"),
                    Arguments.of("user@", "missing domain"),
                    Arguments.of("@example.com", "missing local part"),
                    Arguments.of("user @example.com", "contains space")
            );
        }

        // ===== VALIDATION FAILURES - USERNAME LENGTH =====

        @ParameterizedTest
        @DisplayName("Reject registration with username outside 3-50 character range")
        @MethodSource("provideInvalidUsernameLengthScenarios")
        public void testRegisterWithInvalidUsernameLength(String username, String scenarioDescription) throws Exception {
            // Scenario: User attempts registration with invalid username length ({scenarioDescription})
            RegisterRequest request = new RegisterRequest();
            request.setUsername(username);
            request.setEmail(VALID_EMAIL);
            request.setPassword(VALID_PASSWORD);
            request.setPasswordConfirm(VALID_PASSWORD);

            // Expected: HTTP 400 Bad Request
            mockMvc.perform(post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<Arguments> provideInvalidUsernameLengthScenarios() {
            return Stream.of(
                    Arguments.of("ab", "too short - 2 characters"),
                    Arguments.of("a", "too short - 1 character")
            );
        }

        // ===== PASSWORD VALIDATION FAILURES - MISMATCH =====

        @ParameterizedTest
        @DisplayName("Reject registration when passwords do not match")
        @MethodSource("providePasswordMismatchScenarios")
        public void testRegisterWithPasswordMismatch(String password, String passwordConfirm,
                                                     String scenarioDescription) throws Exception {
            // Scenario: User enters mismatched passwords ({scenarioDescription})
            RegisterRequest request = new RegisterRequest();
            request.setEmail(VALID_EMAIL);
            request.setUsername(VALID_USERNAME);
            request.setPassword(password);
            request.setPasswordConfirm(passwordConfirm);

            // Expected: HTTP 400 Bad Request
            mockMvc.perform(post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<Arguments> providePasswordMismatchScenarios() {
            return Stream.of(
                    Arguments.of("ValidPass123!", "ValidPass124!", "last character differs"),
                    Arguments.of("ValidPass123!", "ValidPass123", "missing special character in confirm"),
                    Arguments.of("ValidPass123!", "validpass123!", "different case")
            );
        }

        // ===== PASSWORD VALIDATION FAILURES - WEAK PASSWORD =====

        @ParameterizedTest
        @DisplayName("Reject registration with weak passwords")
        @MethodSource("provideWeakPasswordScenarios")
        public void testRegisterWithWeakPassword(String password, String scenarioDescription) throws Exception {
            // Scenario: User attempts registration with weak password ({scenarioDescription})
            RegisterRequest request = new RegisterRequest();
            request.setEmail(VALID_EMAIL);
            request.setUsername(VALID_USERNAME);
            request.setPassword(password);
            request.setPasswordConfirm(password);

            // Expected: HTTP 400 Bad Request
            mockMvc.perform(post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<Arguments> provideWeakPasswordScenarios() {
            return Stream.of(
                    Arguments.of("weakpass", "no uppercase, no digit, no special char"),
                    Arguments.of("WeakPass", "no digit, no special char"),
                    Arguments.of("WeakPass1", "no special character"),
                    Arguments.of("weak@1", "too short - 5 characters"),
                    Arguments.of("ALLUPPERCASE123!", "no lowercase letter")
            );
        }

        // ===== CONFLICT SCENARIOS - DUPLICATE EMAIL =====

        @Test
        @DisplayName("Reject registration when email already exists")
        public void testRegisterWithDuplicateEmail() throws Exception {
            // Setup: Create existing user with email
            String existingEmail = "existing@example.com";
            createTestUser("existinguser", existingEmail, "ExistingPass123!",true);

            // Scenario: New user tries to register with the same email
            RegisterRequest request = new RegisterRequest();
            request.setEmail(existingEmail);
            request.setUsername("newuser");
            request.setPassword(VALID_PASSWORD);
            request.setPasswordConfirm(VALID_PASSWORD);

            // Expected: HTTP 409 Conflict
            mockMvc.perform(post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            // Verify: Only one user exists with this email
            long count = userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equals(existingEmail))
                    .count();
            Assertions.assertEquals(1, count, "Only one user should have this email");
        }

        // ===== CONFLICT SCENARIOS - DUPLICATE USERNAME =====

        @Test
        @DisplayName("Reject registration when username already exists")
        public void testRegisterWithDuplicateUsername() throws Exception {
            // Setup: Create existing user with username
            String existingUsername = "takenuser";
            createTestUser(existingUsername, "taken@example.com", "ExistingPass123!",true);

            // Scenario: New user tries to register with the same username
            RegisterRequest request = new RegisterRequest();
            request.setEmail("newuser@example.com");
            request.setUsername(existingUsername);
            request.setPassword(VALID_PASSWORD);
            request.setPasswordConfirm(VALID_PASSWORD);

            // Expected: HTTP 409 Conflict
            mockMvc.perform(post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            // Verify: Only one user exists with this username
            long count = userRepository.findAll().stream()
                    .filter(u -> u.getUsername().equals(existingUsername))
                    .count();
            Assertions.assertEquals(1, count, "Only one user should have this username");
        }
    }

    // ===== LOGIN ENDPOINT TESTS =====

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpoint {

        // ===== HAPPY PATH =====

        @Test
        @DisplayName("Successfully login with valid credentials returns 200 OK")
        public void testLoginSuccess() throws Exception {
            // Setup: Create a registered user with encoded password
            User user = createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);

            // Scenario: User logs in with correct username and password
            LoginRequest request = buildValidLoginRequest();

            // Expected: HTTP 200 OK with tokens and user details
            mockMvc.perform(post(LOGIN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken", notNullValue()))
                    .andExpect(jsonPath("$.refreshToken", notNullValue()))
                    .andExpect(jsonPath("$.userId", notNullValue()))
                    .andExpect(jsonPath("$.username", equalTo(VALID_USERNAME)))
                    .andExpect(jsonPath("$.email", equalTo(VALID_EMAIL)))
                    .andExpect(jsonPath("$.tokenType", equalTo("Bearer")))
                    .andExpect(jsonPath("$.expiresIn", notNullValue()));

            // Verify: User's lastLogin is updated in database
            User updatedUser = userRepository.findByUsername(VALID_USERNAME).orElse(null);
            Assertions.assertNotNull(updatedUser, "User should exist");
            Assertions.assertNotNull(updatedUser.getLastLogin(), "lastLogin should be updated in database");
        }

        // ===== VALIDATION FAILURES - BLANK/MISSING FIELDS =====

        @Nested
        @DisplayName("Validation Failures - Blank/Missing Fields")
        class ValidationBlankFields {

            @ParameterizedTest
            @DisplayName("Reject login with blank or missing required fields")
            @MethodSource("provideBlankFieldScenarios")
            public void testLoginWithBlankFields(String username, String password, String scenarioDescription)
                    throws Exception {
                // Scenario: User attempts login with missing required fields ({scenarioDescription})
                LoginRequest request = new LoginRequest();
                request.setUsername(username);
                request.setPassword(password);

                // Expected: HTTP 400 Bad Request with validation error
                mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }

            private static Stream<Arguments> provideBlankFieldScenarios() {
                return Stream.of(
                        Arguments.of(null, "ValidPass123!", "null username"),
                        Arguments.of("", "ValidPass123!", "empty username"),
                        Arguments.of("testuser", null, "null password"),
                        Arguments.of("testuser", "", "empty password")
                );
            }
        }

        // ===== VALIDATION FAILURES - PASSWORD LENGTH =====

        @Nested
        @DisplayName("Validation Failures - Password Length")
        class ValidationPasswordLength {

            @ParameterizedTest
            @DisplayName("Reject login with password shorter than minimum length (6 characters)")
            @MethodSource("provideShortPasswordScenarios")
            public void testLoginWithShortPassword(String password, String scenarioDescription) throws Exception {
                // Scenario: User attempts login with password below minimum length constraint ({scenarioDescription})
                LoginRequest request = new LoginRequest();
                request.setUsername(VALID_USERNAME);
                request.setPassword(password);

                // Expected: HTTP 400 Bad Request with password length validation error
                mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }

            private static Stream<Arguments> provideShortPasswordScenarios() {
                return Stream.of(
                        Arguments.of("12345", "5 characters"),
                        Arguments.of("1", "1 character"),
                        Arguments.of("abc", "3 characters")
                );
            }
        }

        // ===== AUTHENTICATION FAILURES - INVALID CREDENTIALS =====

        @Nested
        @DisplayName("Authentication Failures - Invalid Credentials")
        class AuthenticationInvalidCredentials {

            @ParameterizedTest
            @DisplayName("Reject login when username does not exist")
            @MethodSource("provideNonexistentUsernames")
            public void testLoginWithNonexistentUsername(String username, String scenarioDescription)
                    throws Exception {
                // Scenario: User attempts login with username that was never registered ({scenarioDescription})
                LoginRequest request = new LoginRequest();
                request.setUsername(username);
                request.setPassword(VALID_PASSWORD);

                // Expected: HTTP 401 Unauthorized with "Invalid username or password"
                mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized());
            }

            private static Stream<Arguments> provideNonexistentUsernames() {
                return Stream.of(
                        Arguments.of("nonexistentuser", "completely nonexistent"),
                        Arguments.of("fakeuser123", "fake username"),
                        Arguments.of("notregistered", "never registered")
                );
            }

            @Test
            @DisplayName("Reject login when password is incorrect")
            public void testLoginWithIncorrectPassword() throws Exception {
                // Setup: Create user with correct password
                createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);

                // Scenario: User attempts login with wrong password
                LoginRequest request = new LoginRequest();
                request.setUsername(VALID_USERNAME);
                request.setPassword("WrongPass123!");

                // Expected: HTTP 401 Unauthorized with "Invalid username or password"
                mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized());

                // Verify: User's lastLogin is NOT updated on failed login
                User userAfterFailedLogin = userRepository.findByUsername(VALID_USERNAME).orElse(null);
                Assertions.assertNotNull(userAfterFailedLogin);
                Assertions.assertNull(userAfterFailedLogin.getLastLogin(),
                        "lastLogin should not be updated on failed login");
            }
        }

        // ===== ACCOUNT STATE FAILURES =====

        @Nested
        @DisplayName("Account State Failures")
        class AccountStateFailures {

            @Test
            @DisplayName("Reject login when user account is inactive")
            public void testLoginWithInactiveUser() throws Exception {
                // Setup: Create inactive user with correct password
                createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, false);

                // Scenario: Inactive user attempts to login with correct credentials
                LoginRequest request = buildValidLoginRequest();

                // Expected: HTTP 401 Unauthorized with "User account is inactive"
                mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized());

                // Verify: User's lastLogin is NOT updated
                User userAfterAttempt = userRepository.findByUsername(VALID_USERNAME).orElse(null);
                Assertions.assertNotNull(userAfterAttempt);
                Assertions.assertNull(userAfterAttempt.getLastLogin(),
                        "lastLogin should not be updated for inactive users");
            }
        }

        // ===== SESSION CREATION TESTS =====

        @Nested
        @DisplayName("Session Creation")
        class SessionCreation {

            @Test
            @DisplayName("Update user lastLogin timestamp on successful login")
            public void testLoginUpdatesLastLogin() throws Exception {
                // Setup: Create user
                createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);

                // Scenario: User successfully logs in
                LoginRequest request = buildValidLoginRequest();

                mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                // Verify: User lastLogin is updated
                User savedUser = userRepository.findByUsername(VALID_USERNAME).orElse(null);
                Assertions.assertNotNull(savedUser);
                Assertions.assertNotNull(savedUser.getLastLogin(), "lastLogin should be updated");
            }

            @Test
            @DisplayName("Allow multiple successful logins for same user")
            public void testMultipleLoginAttempts() throws Exception {
                // Setup: Create user
                createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);

                LoginRequest request = buildValidLoginRequest();

                // Scenario: User logs in successfully first time
                mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                LocalDateTime firstLogin = userRepository.findByUsername(VALID_USERNAME)
                        .orElseThrow().getLastLogin();

                // Wait briefly to ensure timestamp difference
                Thread.sleep(100);

                // Scenario: User logs in successfully second time
                mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                // Expected: Second login succeeds and lastLogin is updated
                User userAfterSecondLogin = userRepository.findByUsername(VALID_USERNAME).orElse(null);
                Assertions.assertNotNull(userAfterSecondLogin);
                Assertions.assertNotNull(userAfterSecondLogin.getLastLogin(),
                        "lastLogin should be updated after second successful login");
                Assertions.assertTrue(userAfterSecondLogin.getLastLogin().isAfter(firstLogin),
                        "lastLogin should be updated to a later timestamp");
            }
        }
    }

    // ===== GET CURRENT USER ENDPOINT TESTS =====

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserEndpoint {

        // ===== HAPPY PATH =====

        @Test
        @DisplayName("Successfully retrieve current user profile with valid authentication")
        public void testGetCurrentUserSuccess() throws Exception {
            // Setup: Create authenticated user with complete profile data
            String publicKey = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgk...\n-----END PUBLIC KEY-----";
            String fingerprint = "SHA256:abcdef1234567890";
            
            User user = createAuthenticatedUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, 
                                               true, publicKey, fingerprint);
            String accessToken = generateTokenForUser(user);

            // Scenario: Authenticated user requests their own profile
            // Expected: Profile data is returned with all user details
            mockMvc.perform(get(ME_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.username", equalTo(VALID_USERNAME)))
                    .andExpect(jsonPath("$.email", equalTo(VALID_EMAIL)))
                    .andExpect(jsonPath("$.publicKey", equalTo(publicKey)))
                    .andExpect(jsonPath("$.fingerprint", equalTo(fingerprint)))
                    .andExpect(jsonPath("$.isActive", equalTo(true)))
                    .andExpect(jsonPath("$.createdAt", notNullValue()));
        }

        // ===== PROFILE DATA HANDLING =====

        @Nested
        @DisplayName("Profile Data Handling")
        class ProfileDataHandling {

            @Test
            @DisplayName("Return user profile with null optional fields")
            public void testGetCurrentUserWithNullOptionalFields() throws Exception {
                // Scenario: Authenticated user with minimal profile data requests their profile
                User user = createAuthenticatedUser("minimaluser", "minimal@example.com", 
                                                   VALID_PASSWORD, true, null, null);
                String accessToken = generateTokenForUser(user);

                // Expected: Profile is returned with null optional fields
                mockMvc.perform(get(ME_ENDPOINT)
                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username", equalTo("minimaluser")))
                        .andExpect(jsonPath("$.email", equalTo("minimal@example.com")))
                        .andExpect(jsonPath("$.publicKey", nullValue()))
                        .andExpect(jsonPath("$.fingerprint", nullValue()))
                        .andExpect(jsonPath("$.isActive", equalTo(true)));
            }

            @ParameterizedTest
            @DisplayName("Retrieve profile with various user states")
            @MethodSource("provideUserProfileScenarios")
            public void testGetCurrentUserWithVariousStates(String username, String email, 
                                                           Boolean isActive, String publicKey) throws Exception {
                // Scenario: Retrieve profile for user with specific state
                User user = createAuthenticatedUser(username, email, VALID_PASSWORD, isActive, publicKey, null);
                String accessToken = generateTokenForUser(user);

                // Expected: Profile reflects current user state
                mockMvc.perform(get(ME_ENDPOINT)
                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username", equalTo(username)))
                        .andExpect(jsonPath("$.email", equalTo(email)))
                        .andExpect(jsonPath("$.isActive", equalTo(isActive)));
            }

            private static Stream<Arguments> provideUserProfileScenarios() {
                return Stream.of(
                        Arguments.of("activeuser", "active@example.com", true, "public-key-123"),
                        Arguments.of("inactiveuser", "inactive@example.com", false, null),
                        Arguments.of("newuser", "new@example.com", true, null),
                        Arguments.of("olduser", "old@example.com", true, "legacy-public-key")
                );
            }

            @ParameterizedTest
            @DisplayName("Retrieve profile regardless of user active status")
            @CsvSource({
                    "true",
                    "false"
            })
            public void testGetCurrentUserRegardlessOfActiveStatus(Boolean isActive) throws Exception {
                // Scenario: User with specified active status requests their profile
                User user = createAuthenticatedUser("statususer", "status@example.com", 
                                                   VALID_PASSWORD, isActive, null, null);
                String accessToken = generateTokenForUser(user);

                // Expected: Profile is returned showing current active status
                mockMvc.perform(get(ME_ENDPOINT)
                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.isActive", equalTo(isActive)))
                        .andExpect(jsonPath("$.username", equalTo("statususer")));
            }

            @Test
            @DisplayName("Profile data is current and not cached from previous requests")
            public void testGetCurrentUserReturnsLatestData() throws Exception {
                // Setup: Create user with initial public key as null
                User user = createAuthenticatedUser("cacheuser", "cache@example.com", 
                                                   VALID_PASSWORD, true, null, null);
                String accessToken = generateTokenForUser(user);

                // First request: Get profile
                mockMvc.perform(get(ME_ENDPOINT)
                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.publicKey", nullValue()));

                // Update user's public key
                user.setPublicKey("updated-public-key-xyz");
                userRepository.save(user);

                // Second request: Get profile again
                // Expected: Should return updated public key, not cached old value
                mockMvc.perform(get(ME_ENDPOINT)
                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.publicKey", equalTo("updated-public-key-xyz")));
            }
        }

        // ===== LAST LOGIN HANDLING =====

        @Nested
        @DisplayName("Last Login Handling")
        class LastLoginHandling {

            @Test
            @DisplayName("Retrieve profile including lastLogin timestamp from previous login")
            public void testGetCurrentUserIncludesLastLogin() throws Exception {
                // Setup: Create user and simulate a previous login
                LocalDateTime previousLoginTime = LocalDateTime.now().minusHours(2);
                
                User user = User.builder()
                        .username("lastloginuser")
                        .email("lastlogin@example.com")
                        .passwordHash(PasswordEncoder.encode(VALID_PASSWORD))
                        .isActive(true)
                        .lastLogin(previousLoginTime)
                        .build();
                User savedUser = userRepository.save(user);
                String accessToken = generateTokenForUser(savedUser);

                // Scenario: User requests profile after having logged in before
                // Expected: lastLogin timestamp is included in response
                mockMvc.perform(get(ME_ENDPOINT)
                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.lastLogin", notNullValue()))
                        .andExpect(jsonPath("$.username", equalTo("lastloginuser")));
            }

            @Test
            @DisplayName("Retrieve profile with null lastLogin for first-time access")
            public void testGetCurrentUserWithNullLastLogin() throws Exception {
                // Scenario: User that has never logged in before requests their profile
                User user = createAuthenticatedUser("firsttimeuser", "firsttime@example.com", 
                                                   VALID_PASSWORD, true, null, null);
                String accessToken = generateTokenForUser(user);

                // Expected: lastLogin is null in response
                mockMvc.perform(get(ME_ENDPOINT)
                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.lastLogin", nullValue()))
                        .andExpect(jsonPath("$.username", equalTo("firsttimeuser")));
            }
        }

        // ===== AUTHENTICATION FAILURES =====

        @Nested
        @DisplayName("Authentication Failures")
        class AuthenticationFailures {

            @Test
            @DisplayName("Reject request without authentication")
            public void testGetCurrentUserWithoutAuthentication() throws Exception {
                // Scenario: Unauthenticated user attempts to access their profile
                // Expected: HTTP 401 Unauthorized
                mockMvc.perform(get(ME_ENDPOINT))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("Return 401 when authenticated user no longer exists in database")
            public void testGetCurrentUserNotFound() throws Exception {
                // Setup: Create user, generate token, then delete user
                User user = createAuthenticatedUser("deleteduser", "deleted@example.com", 
                                                   VALID_PASSWORD, true, null, null);
                String accessToken = generateTokenForUser(user);
                
                // Delete the user to simulate the scenario
                userRepository.delete(user);

                // Scenario: User that was deleted from database still has valid JWT token
                // Expected: HTTP 401 Unauthorized (User not found)
                mockMvc.perform(get(ME_ENDPOINT)
                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isUnauthorized());
            }
        }
    }

    @Nested
    @DisplayName("UpdateProfile Integration Tests")
    class UpdateProfileTests {

        @ParameterizedTest
        @DisplayName("Successfully update user profile with valid data")
        @CsvSource({
                "john_doe, john@example.com, john_updated, john.updated@example.com",
                "alice123, alice@test.com, alice_new, alice.new@test.com",
                "bob_smith, bob@domain.com, bob_modified, bob.modified@domain.com"
        })
        public void testUpdateProfileSuccess(String oldUsername, String oldEmail, 
                                             String newUsername, String newEmail) throws Exception {
            User user = createTestUser(oldUsername, oldEmail, VALID_PASSWORD, true);
            String accessToken = generateTokenForUser(user);

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .username(newUsername)
                    .email(newEmail)
                    .build();

            mockMvc.perform(put(PROFILE_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username", equalTo(newUsername)))
                    .andExpect(jsonPath("$.email", equalTo(newEmail)))
                    .andExpect(jsonPath("$.id", notNullValue()));

            User updatedUser = userRepository.findByUsername(newUsername).orElse(null);
            Assertions.assertNotNull(updatedUser, "Updated user should exist with new username");
            Assertions.assertEquals(newEmail, updatedUser.getEmail());
        }

        @ParameterizedTest
        @DisplayName("Update only username or email while keeping other field same")
        @CsvSource({
                "original_user, user@example.com, updated_user, user@example.com",
                "user123, original@example.com, user123, updated@example.com"
        })
        public void testUpdateProfilePartial(String oldUsername, String oldEmail, 
                                            String newUsername, String newEmail) throws Exception {
            User user = createTestUser(oldUsername, oldEmail, VALID_PASSWORD, true);
            String accessToken = generateTokenForUser(user);

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .username(newUsername)
                    .email(newEmail)
                    .build();

            mockMvc.perform(put(PROFILE_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username", equalTo(newUsername)))
                    .andExpect(jsonPath("$.email", equalTo(newEmail)));

            User updatedUser = userRepository.findByUsername(newUsername).orElse(null);
            Assertions.assertNotNull(updatedUser);
        }

        // ===== VALIDATION FAILURE TESTS =====

        @ParameterizedTest
        @DisplayName("Update profile fails with invalid username")
        @MethodSource("provideInvalidUsernames")
        public void testUpdateProfileInvalidUsername(String invalidUsername) throws Exception {
            User user = createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);
            String accessToken = generateTokenForUser(user);

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .username(invalidUsername)
                    .email("newemail@example.com")
                    .build();

            mockMvc.perform(put(PROFILE_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<Arguments> provideInvalidUsernames() {
            return Stream.of(
                    Arguments.of("ab"),  // Too short
                    Arguments.of("a" + "b".repeat(50)),  // Too long
                    Arguments.of("")  // Blank
            );
        }

        // ===== CONFLICT TESTS =====

        @Test
        @DisplayName("Update profile fails when new email already exists")
        public void testUpdateProfileDuplicateEmail() throws Exception {
            User user1 = createTestUser("user1", "user1@example.com", VALID_PASSWORD, true);
            User user2 = createTestUser("user2", "user2@example.com", VALID_PASSWORD, true);
            String accessToken = generateTokenForUser(user1);

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .username("user1_updated")
                    .email("user2@example.com")
                    .build();

            mockMvc.perform(put(PROFILE_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            User unchangedUser = userRepository.findByUsername("user1").orElse(null);
            Assertions.assertNotNull(unchangedUser);
            Assertions.assertEquals("user1@example.com", unchangedUser.getEmail());
        }

        @Test
        @DisplayName("Update profile succeeds when keeping own existing email")
        public void testUpdateProfileKeepOwnEmail() throws Exception {
            User user = createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);
            String accessToken = generateTokenForUser(user);

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .username("user_updated")
                    .email(VALID_EMAIL)
                    .build();

            mockMvc.perform(put(PROFILE_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", equalTo(VALID_EMAIL)));
        }

        // ===== AUTHORIZATION TESTS =====

        @ParameterizedTest
        @DisplayName("Update profile fails without valid JWT token or with invalid token")
        @ValueSource(strings = {"", "invalid.token.here"})
        public void testUpdateProfileAuthenticationFailure(String token) throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .username("newname")
                    .email("newemail@example.com")
                    .build();

            MockHttpServletRequestBuilder requestBuilder = put(PROFILE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request));

            if (!token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            mockMvc.perform(requestBuilder)
                    .andExpect(status().isUnauthorized());
        }


        @Test
        @DisplayName("Update profile fails when user no longer exists")
        public void testUpdateProfileUserNotFound() throws Exception {
            String accessToken = jwtTokenProvider.generateAccessToken("deleted_user", 999L);

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .username("newname")
                    .email("newemail@example.com")
                    .build();

            mockMvc.perform(put(PROFILE_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ===== REFRESH TOKEN ENDPOINT =====

    @Nested
    @DisplayName("Refresh Token Endpoint")
    class RefreshTokenEndpoint {

        @Test
        @DisplayName("Successfully refresh tokens with valid refresh token")
        public void testRefreshTokenSuccess() throws Exception {
            // Setup: Create user and login to get refresh token
            createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);
            LoginRequest loginRequest = buildValidLoginRequest();

            MvcResult loginResult = mockMvc.perform(post(LOGIN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(),
                    LoginResponse.class);

            String refreshToken = loginResponse.getRefreshToken();
            String oldAccessToken = loginResponse.getAccessToken();

            // Add delay to ensure different timestamp in token generation
            Thread.sleep(100);

            // Scenario: User refreshes token with valid refresh token
            MvcResult refreshResult = mockMvc.perform(post(REFRESH_ENDPOINT)
                    .param("refreshToken", refreshToken))
                    .andExpect(status().isOk())
                    .andReturn();

            LoginResponse refreshResponse = objectMapper.readValue(
                    refreshResult.getResponse().getContentAsString(),
                    LoginResponse.class);

            // Expected: New tokens are returned and are different from old ones
            Assertions.assertNotNull(refreshResponse.getAccessToken());
            Assertions.assertNotNull(refreshResponse.getRefreshToken());
            Assertions.assertNotEquals(oldAccessToken, refreshResponse.getAccessToken(),
                    "New access token should be different from old one");
            Assertions.assertNotEquals(refreshToken, refreshResponse.getRefreshToken(),
                    "New refresh token should be different from old one");
            Assertions.assertEquals(VALID_USERNAME, refreshResponse.getUsername());
        }

        @Test
        @DisplayName("Refresh fails with invalid refresh token")
        public void testRefreshTokenInvalid() throws Exception {
            mockMvc.perform(post(REFRESH_ENDPOINT)
                    .param("refreshToken", "invalid.token.here"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Refresh fails with expired refresh token")
        public void testRefreshTokenExpired() throws Exception {
            // Setup: Create user and login
            createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);
            LoginRequest loginRequest = buildValidLoginRequest();

            MvcResult loginResult = mockMvc.perform(post(LOGIN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(),
                    LoginResponse.class);

            // Manually expire the session in database
            Session session = sessionRepository.findByRefreshToken(loginResponse.getRefreshToken())
                    .orElseThrow();
            session.setExpiresAt(LocalDateTime.now().minusHours(1));
            sessionRepository.save(session);

            // Scenario: User tries to refresh with expired session
            mockMvc.perform(post(REFRESH_ENDPOINT)
                    .param("refreshToken", loginResponse.getRefreshToken()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Refresh fails with inactive session")
        public void testRefreshTokenInactiveSession() throws Exception {
            // Setup: Create user and login
            createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);
            LoginRequest loginRequest = buildValidLoginRequest();

            MvcResult loginResult = mockMvc.perform(post(LOGIN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(),
                    LoginResponse.class);

            // Manually revoke the session
            Session session = sessionRepository.findByRefreshToken(loginResponse.getRefreshToken())
                    .orElseThrow();
            session.setActive(false);
            sessionRepository.save(session);

            // Scenario: User tries to refresh with revoked session
            mockMvc.perform(post(REFRESH_ENDPOINT)
                    .param("refreshToken", loginResponse.getRefreshToken()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Refresh with empty refresh token parameter")
        public void testRefreshTokenEmpty() throws Exception {
            mockMvc.perform(post(REFRESH_ENDPOINT)
                    .param("refreshToken", ""))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Multiple successive refreshes work correctly")
        public void testMultipleSuccessiveRefreshes() throws Exception {
            // Setup: Create user and login
            createTestUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, true);
            LoginRequest loginRequest = buildValidLoginRequest();

            MvcResult loginResult = mockMvc.perform(post(LOGIN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(),
                    LoginResponse.class);

            String currentRefreshToken = loginResponse.getRefreshToken();

            // Scenario: User refreshes token multiple times
            for (int i = 0; i < 3; i++) {
                MvcResult refreshResult = mockMvc.perform(post(REFRESH_ENDPOINT)
                        .param("refreshToken", currentRefreshToken))
                        .andExpect(status().isOk())
                        .andReturn();

                LoginResponse refreshResponse = objectMapper.readValue(
                        refreshResult.getResponse().getContentAsString(),
                        LoginResponse.class);

                Assertions.assertNotNull(refreshResponse.getAccessToken());
                currentRefreshToken = refreshResponse.getRefreshToken();
            }

            // Expected: All refreshes succeed
            Assertions.assertNotNull(currentRefreshToken);
        }
    }

    // ===== HELPER METHODS =====

    private LoginRequest buildValidLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername(VALID_USERNAME);
        request.setPassword(VALID_PASSWORD);
        return request;
    }

    private RegisterRequest buildValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(VALID_EMAIL);
        request.setUsername(VALID_USERNAME);
        request.setPassword(VALID_PASSWORD);
        request.setPasswordConfirm(VALID_PASSWORD);
        return request;
    }

    private User createTestUser(String username, String email, String password, boolean isActive) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(PasswordEncoder.encode(password))
                .isActive(isActive)
                .build();
        return userRepository.save(user);
    }

    private User createAuthenticatedUser(String username, String email, String password, 
                                         Boolean isActive, String publicKey, String fingerprint) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(PasswordEncoder.encode(password))
                .isActive(isActive)
                .publicKey(publicKey)
                .fingerprint(fingerprint)
                .build();
        return userRepository.save(user);
    }

    private String generateTokenForUser(User user) {
        return jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId());
    }
}
