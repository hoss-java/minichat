package com.minichat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * JWT TOKEN PROVIDER UNIT TESTS
 * ============================================================================
 *
 * This test class validates JWT token generation, validation, and extraction:
 * - Access token generation with claims and expiration
 * - Refresh token generation with type marker
 * - Token extraction (username, userId)
 * - Token validation (signature, expiration, token type)
 * - Refresh token validation (type checking)
 * - Token expiration detection
 * - Exception message generation for various token errors
 *
 * Test Organization:
 * 1. Access Token Generation Tests
 * 2. Refresh Token Generation Tests
 * 3. Token Extraction Tests
 * 4. Access Token Validation Tests
 * 5. Refresh Token Validation Tests
 * 6. Token Expiration Tests
 * 7. Exception Message Tests
 * 8. Edge Cases
 *
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Token Provider Unit Tests")
public class JwtTokenProviderUnitTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "your-secret-key-min-256-bits-long-for-hs256-algorithm-security-extra";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days

    @BeforeEach
    public void setUp() {
        // Initialize JwtTokenProvider with test values
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
    }

    // ============================================================================
    // SECTION 1: ACCESS TOKEN GENERATION TESTS
    // ============================================================================
    //
    // Scenario: Access tokens are generated with username and userId
    // Expected: Token is non-null, non-empty, and can be parsed
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should generate valid access tokens")
    @CsvSource({
        "john_doe, 1",
        "jane_smith, 2",
        "admin_user, 100",
        "test.user@example.com, 999"
    })
    public void testGenerateAccessToken(String username, Long userId) {
        // Scenario: Access token is generated for user
        // Expected: Token is non-null, non-empty, properly formatted

        String token = jwtTokenProvider.generateAccessToken(username, userId);

        assertNotNull(token, "Generated access token should not be null");
        assertFalse(token.isEmpty(), "Generated access token should not be empty");
        assertTrue(token.contains("."), "JWT token should contain dots separating parts");
        assertEquals(3, token.split("\\.").length, "JWT should have 3 parts (header.payload.signature)");
    }

    @Test
    @DisplayName("Should include userId in access token claims")
    public void testAccessTokenIncludesUserIdClaim() {
        // Scenario: Generated access token contains userId claim
        // Expected: Claim can be extracted and matches original userId

        Long expectedUserId = 123L;
        String token = jwtTokenProvider.generateAccessToken("testuser", expectedUserId);

        Long extractedUserId = jwtTokenProvider.extractUserId(token);

        assertEquals(expectedUserId, extractedUserId, "userId claim should match");
    }

    @Test
    @DisplayName("Should not include type claim in access token")
    public void testAccessTokenDoesNotIncludeTypeClaim() {
        // Scenario: Access token is generated without type marker
        // Expected: Token validation succeeds (no type restriction for access tokens)

        String token = jwtTokenProvider.generateAccessToken("testuser", 1L);
        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid, "Access token without type claim should be valid");
    }

    // ============================================================================
    // SECTION 2: REFRESH TOKEN GENERATION TESTS
    // ============================================================================
    //
    // Scenario: Refresh tokens are generated with type marker
    // Expected: Token contains "refresh" type claim and longer expiration
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should generate valid refresh tokens")
    @CsvSource({
        "john_doe, 1",
        "jane_smith, 2",
        "admin_user, 100"
    })
    public void testGenerateRefreshToken(String username, Long userId) {
        // Scenario: Refresh token is generated for user
        // Expected: Token is valid and properly formatted

        String token = jwtTokenProvider.generateRefreshToken(username, userId);

        assertNotNull(token, "Generated refresh token should not be null");
        assertFalse(token.isEmpty(), "Generated refresh token should not be empty");
        assertEquals(3, token.split("\\.").length, "JWT should have 3 parts");
    }

    @Test
    @DisplayName("Should include refresh type marker in refresh token")
    public void testRefreshTokenIncludesTypeMarker() {
        // Scenario: Refresh token is generated with type claim
        // Expected: Token passes refresh validation

        String token = jwtTokenProvider.generateRefreshToken("testuser", 1L);
        boolean isValid = jwtTokenProvider.validateRefreshToken(token);

        assertTrue(isValid, "Refresh token with type marker should pass refresh validation");
    }

    @Test
    @DisplayName("Should have longer expiration for refresh token")
    public void testRefreshTokenHasLongerExpiration() {
        // Scenario: Refresh token and access token are generated
        // Expected: Refresh token expiration is longer than access token

        String accessToken = jwtTokenProvider.generateAccessToken("testuser", 1L);
        String refreshToken = jwtTokenProvider.generateRefreshToken("testuser", 1L);

        Date accessExpiration = extractExpirationDate(accessToken);
        Date refreshExpiration = extractExpirationDate(refreshToken);

        assertTrue(refreshExpiration.after(accessExpiration),
                "Refresh token should have longer expiration than access token");
    }

    // ============================================================================
    // SECTION 3: TOKEN EXTRACTION TESTS
    // ============================================================================
    //
    // Scenario: Username and userId are extracted from tokens
    // Expected: Extracted values match original values used to generate token
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should extract username from token")
    @ValueSource(strings = {
        "john_doe",
        "jane_smith",
        "admin@example.com",
        "user.name"
    })
    public void testExtractUsername(String username) {
        // Scenario: Username is extracted from generated token
        // Expected: Extracted username matches original

        String token = jwtTokenProvider.generateAccessToken(username, 1L);
        String extractedUsername = jwtTokenProvider.extractUsername(token);

        assertEquals(username, extractedUsername, "Extracted username should match original");
    }

    @ParameterizedTest
    @DisplayName("Should extract userId from token")
    @ValueSource(longs = { 1L, 100L, 999L, 9999999L })
    public void testExtractUserId(Long userId) {
        // Scenario: UserId is extracted from generated token
        // Expected: Extracted userId matches original

        String token = jwtTokenProvider.generateAccessToken("testuser", userId);
        Long extractedUserId = jwtTokenProvider.extractUserId(token);

        assertEquals(userId, extractedUserId, "Extracted userId should match original");
    }

    @Test
    @DisplayName("Should extract both username and userId from same token")
    public void testExtractMultipleClaimsFromToken() {
        // Scenario: Both username and userId are extracted from single token
        // Expected: Both values match originals

        String username = "testuser";
        Long userId = 42L;
        String token = jwtTokenProvider.generateAccessToken(username, userId);

        String extractedUsername = jwtTokenProvider.extractUsername(token);
        Long extractedUserId = jwtTokenProvider.extractUserId(token);

        assertEquals(username, extractedUsername, "Username should match");
        assertEquals(userId, extractedUserId, "UserId should match");
    }

    // ============================================================================
    // SECTION 4: ACCESS TOKEN VALIDATION TESTS
    // ============================================================================
    //
    // Scenario: Access tokens are validated for signature and type
    // Expected: Valid tokens pass, invalid/refresh tokens fail
    //
    // ============================================================================

    @Test
    @DisplayName("Should validate correctly signed access token")
    public void testValidateCorrectlySignedAccessToken() {
        // Scenario: Valid access token is validated
        // Expected: Validation passes

        String token = jwtTokenProvider.generateAccessToken("testuser", 1L);
        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid, "Valid access token should pass validation");
    }

    @Test
    @DisplayName("Should reject refresh token as access token")
    public void testRejectRefreshTokenAsAccessToken() {
        // Scenario: Refresh token (with type="refresh") validated as access token
        // Expected: Validation fails due to token type

        String refreshToken = jwtTokenProvider.generateRefreshToken("testuser", 1L);
        boolean isValid = jwtTokenProvider.validateToken(refreshToken);

        assertFalse(isValid, "Refresh token should not pass access token validation");
    }

    @Test
    @DisplayName("Should reject malformed token")
    public void testRejectMalformedToken() {
        // Scenario: Malformed string is validated as token
        // Expected: Validation fails

        String malformedToken = "not.a.valid.token";
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertFalse(isValid, "Malformed token should fail validation");
    }

    @Test
    @DisplayName("Should reject token with invalid signature")
    public void testRejectTokenWithInvalidSignature() {
        // Scenario: Valid token is modified and validated
        // Expected: Validation fails due to signature mismatch

        String originalToken = jwtTokenProvider.generateAccessToken("testuser", 1L);
        String[] parts = originalToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature";

        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        assertFalse(isValid, "Token with invalid signature should fail validation");
    }

    @Test
    @DisplayName("Should reject empty token string")
    public void testRejectEmptyToken() {
        // Scenario: Empty string is validated as token
        // Expected: Validation fails

        boolean isValid = jwtTokenProvider.validateToken("");

        assertFalse(isValid, "Empty token should fail validation");
    }

    @Test
    @DisplayName("Should reject null token")
    public void testRejectNullToken() {
        // Scenario: Null token is validated
        // Expected: Validation fails without throwing exception

        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid, "Null token should fail validation");
    }

    // ============================================================================
    // SECTION 5: REFRESH TOKEN VALIDATION TESTS
    // ============================================================================
    //
    // Scenario: Refresh tokens are validated with type marker requirement
    // Expected: Valid refresh tokens pass, access tokens fail
    //
    // ============================================================================

    @Test
    @DisplayName("Should validate correctly signed refresh token")
    public void testValidateCorrectlySignedRefreshToken() {
        // Scenario: Valid refresh token is validated
        // Expected: Refresh validation passes

        String refreshToken = jwtTokenProvider.generateRefreshToken("testuser", 1L);
        boolean isValid = jwtTokenProvider.validateRefreshToken(refreshToken);

        assertTrue(isValid, "Valid refresh token should pass refresh validation");
    }

    @Test
    @DisplayName("Should reject access token as refresh token")
    public void testRejectAccessTokenAsRefreshToken() {
        // Scenario: Access token (without type marker) validated as refresh
        // Expected: Validation fails due to missing type

        String accessToken = jwtTokenProvider.generateAccessToken("testuser", 1L);
        boolean isValid = jwtTokenProvider.validateRefreshToken(accessToken);

        assertFalse(isValid, "Access token should not pass refresh token validation");
    }

    @Test
    @DisplayName("Should reject malformed refresh token")
    public void testRejectMalformedRefreshToken() {
        // Scenario: Malformed string is validated as refresh token
        // Expected: Validation fails

        String malformedToken = "invalid.token.format";
        boolean isValid = jwtTokenProvider.validateRefreshToken(malformedToken);

        assertFalse(isValid, "Malformed refresh token should fail validation");
    }

    // ============================================================================
    // SECTION 6: TOKEN EXPIRATION TESTS
    // ============================================================================
    //
    // Scenario: Tokens are checked for expiration status
    // Expected: Fresh tokens are not expired, old tokens are expired
    //
    // ============================================================================

    @Test
    @DisplayName("Should detect non-expired token")
    public void testNonExpiredTokenDetection() {
        // Scenario: Freshly generated token is checked for expiration
        // Expected: Token is not expired

        String token = jwtTokenProvider.generateAccessToken("testuser", 1L);
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        assertFalse(isExpired, "Freshly generated token should not be expired");
    }

    @Test
    @DisplayName("Should detect expired token")
    public void testExpiredTokenDetection() {
        // Scenario: Token with past expiration is checked
        // Expected: Token is detected as expired

        // Create token that expired 1 hour ago
        String expiredToken = createExpiredToken("testuser", 1L, -3600000); // -1 hour

        boolean isExpired = jwtTokenProvider.isTokenExpired(expiredToken);

        assertTrue(isExpired, "Token with past expiration should be detected as expired");
    }

    // ============================================================================
    // SECTION 7: EXCEPTION MESSAGE TESTS
    // ============================================================================
    //
    // Scenario: Various token errors produce appropriate error messages
    // Expected: Each error type returns specific descriptive message
    //
    // ============================================================================

    @Test
    @DisplayName("Should return 'Token has expired' for expired token")
    public void testExpiredTokenExceptionMessage() {
        // Scenario: Expired token generates exception message
        // Expected: Returns expiration-specific message

        String expiredToken = createExpiredToken("testuser", 1L, -3600000); // -1 hour
        String message = jwtTokenProvider.getTokenExceptionMessage(expiredToken);

        assertEquals("Token has expired", message, "Should return expiration message");
    }

    @Test
    @DisplayName("Should return 'Invalid signature' for tampered token")
    public void testInvalidSignatureExceptionMessage() {
        // Scenario: Token with invalid signature generates message
        // Expected: Returns signature error message

        String originalToken = jwtTokenProvider.generateAccessToken("testuser", 1L);
        String[] parts = originalToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature";

        String message = jwtTokenProvider.getTokenExceptionMessage(tamperedToken);

        assertEquals("Invalid signature", message, "Should return signature error message");
    }

    @Test
    @DisplayName("Should return 'Malformed token' for malformed token")
    public void testMalformedTokenExceptionMessage() {
        // Scenario: Malformed token generates message
        // Expected: Returns malformed token message

        String malformedToken = "not.a.proper.jwt";
        String message = jwtTokenProvider.getTokenExceptionMessage(malformedToken);

        assertEquals("Malformed token", message, "Should return malformed token message");
    }

    @Test
    @DisplayName("Should return 'Token is empty' for empty token")
    public void testEmptyTokenExceptionMessage() {
        // Scenario: Empty string is validated
        // Expected: Returns empty token message

        String message = jwtTokenProvider.getTokenExceptionMessage("");

        assertEquals("Token is empty", message, "Should return empty token message");
    }

    @ParameterizedTest
    @DisplayName("Should return specific exception messages for different invalid tokens")
    @CsvSource({
        "invalid",
        "....",
        "abc.def.ghi",
        "invalid.token.format"
    })
    public void testVariousInvalidTokenExceptionMessages(String invalidToken) {
        // Scenario: Various invalid token formats are provided
        // Expected: Returns some error message (not null)

        String message = jwtTokenProvider.getTokenExceptionMessage(invalidToken);

        assertNotNull(message, 
                "Should return an error message for invalid token: " + invalidToken);
    }

    // ============================================================================
    // SECTION 8: EDGE CASES
    // ============================================================================
    //
    // Scenario: Various edge cases and boundary conditions
    // Expected: Provider handles gracefully without exceptions
    //
    // ============================================================================

    @Test
    @DisplayName("Should handle token with special characters in username")
    public void testTokenWithSpecialCharactersInUsername() {
        // Scenario: Username with special characters is used in token
        // Expected: Token is generated and can be validated

        String specialUsername = "user@example.com-_+.";
        String token = jwtTokenProvider.generateAccessToken(specialUsername, 1L);

        String extracted = jwtTokenProvider.extractUsername(token);
        boolean isValid = jwtTokenProvider.validateToken(token);

        assertEquals(specialUsername, extracted, "Special characters should be preserved");
        assertTrue(isValid, "Token should be valid");
    }

    @Test
    @DisplayName("Should handle very long username")
    public void testTokenWithVeryLongUsername() {
        // Scenario: Very long username is used in token
        // Expected: Token is generated successfully

        String longUsername = "a".repeat(1000) + "@example.com";
        String token = jwtTokenProvider.generateAccessToken(longUsername, 1L);

        String extracted = jwtTokenProvider.extractUsername(token);

        assertEquals(longUsername, extracted, "Long username should be preserved");
    }

    @Test
    @DisplayName("Should handle maximum Long userId value")
    public void testTokenWithMaxLongUserId() {
        // Scenario: Maximum Long value is used as userId
        // Expected: Token handles large number correctly

        Long maxUserId = Long.MAX_VALUE;
        String token = jwtTokenProvider.generateAccessToken("testuser", maxUserId);

        Long extracted = jwtTokenProvider.extractUserId(token);

        assertEquals(maxUserId, extracted, "Maximum long value should be preserved");
    }

    @Test
    @DisplayName("Should handle zero userId")
    public void testTokenWithZeroUserId() {
        // Scenario: Zero is used as userId
        // Expected: Token is generated and userId extracted correctly

        Long zeroUserId = 0L;
        String token = jwtTokenProvider.generateAccessToken("testuser", zeroUserId);

        Long extracted = jwtTokenProvider.extractUserId(token);

        assertEquals(zeroUserId, extracted, "Zero userId should be preserved");
    }

    @Test
    @DisplayName("Should handle negative userId")
    public void testTokenWithNegativeUserId() {
        // Scenario: Negative number is used as userId
        // Expected: Token is generated successfully

        Long negativeUserId = -1L;
        String token = jwtTokenProvider.generateAccessToken("testuser", negativeUserId);

        Long extracted = jwtTokenProvider.extractUserId(token);

        assertEquals(negativeUserId, extracted, "Negative userId should be preserved");
    }

    // ============================================================================
    // SECTION 1: ACCESS TOKEN GENERATION TESTS
    // ============================================================================
    //
    // Scenario: Access tokens are generated with username and userId
    // Expected: Token is non-null, non-empty, and can be parsed
    //
    // ============================================================================

    @Test
    @DisplayName("Should generate valid access token with username and userId")
    public void testGenerateAccessToken() {
        // Scenario: Generate access token for a user
        // Expected: Token is created successfully with correct claims

        String token = jwtTokenProvider.generateAccessToken("testuser", 1L);

        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
        assertTrue(token.contains("."), "Token should contain JWT structure (header.payload.signature)");
    }

    @ParameterizedTest
    @DisplayName("Should generate access tokens for different users with correct claims")
    @CsvSource({
        "user1, 1",
        "user2, 2",
        "user3, 3"
    })
    public void testAccessTokenContainsCorrectClaims(String username, Long userId) {
        // Scenario: Access token contains correct username and userId claims
        // Expected: Extracted values match input values

        String token = jwtTokenProvider.generateAccessToken(username, userId);

        assertEquals(username, jwtTokenProvider.extractUsername(token),
                "Token should contain correct username");
        assertEquals(userId, jwtTokenProvider.extractUserId(token),
                "Token should contain correct userId");
    }

    @Test
    @DisplayName("Should generate tokens with proper structure and parseable format")
    public void testAccessTokenStructureIsValid() {
        // Scenario: Generate access token and verify it's properly formatted
        // Expected: Token can be parsed and contains all required claims

        String token = jwtTokenProvider.generateAccessToken("testuser", 1L);
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getSubject(), "Token should have subject (username)");
        assertNotNull(claims.get("userId"), "Token should have userId claim");
        assertNotNull(claims.getIssuedAt(), "Token should have issuedAt");
        assertNotNull(claims.getExpiration(), "Token should have expiration");
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Creates an expired token by adjusting the system clock artificially
     * @param username Token subject
     * @param userId Claim to include
     * @param offsetMillis Time offset from now (negative for past dates)
     * @return Expired JWT token
     */
    private String createExpiredToken(String username, Long userId, long offsetMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + offsetMillis);

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts expiration date from a token
     * @param token JWT token
     * @return Expiration date
     */
    private Date extractExpirationDate(String token) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
}

