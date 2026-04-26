package com.minichat.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Session entity class.
 * Tests all properties, constructors, and getters/setters.
 * Uses parametrized tests for data validation scenarios.
 */
@DisplayName("Session Entity Unit Tests")
public class SessionUnitTest {

    private Session session;
    private User testUser;
    private LocalDateTime now;
    private LocalDateTime tomorrow;
    private LocalDateTime yesterday;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        tomorrow = now.plusDays(1);
        yesterday = now.minusDays(1);

        // Setup test User
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .isActive(true)
                .createdAt(yesterday)
                .updatedAt(now)
                .lastLogin(now)
                .roles(new HashSet<>())
                .build();

        // Setup test Session
        session = new Session();
        session.setId(1L);
        session.setUser(testUser);
        session.setAccessToken("access-token-123");
        session.setRefreshToken("refresh-token-123");
        session.setExpiresAt(tomorrow);
        session.setRefreshTokenExpiresAt(tomorrow.plusDays(7));
        session.setActive(true);
        session.setCreatedAt(now);
        session.setIpAddress("192.168.1.1");
        session.setUserAgent("Mozilla/5.0");
    }

    // ==================== Constructor Tests ====================

    @DisplayName("NoArgsConstructor - Creates empty Session instance")
    @Test
    void testNoArgsConstructor() {
        Session emptySession = new Session();
        assertNotNull(emptySession);
        assertNull(emptySession.getId());
        assertNull(emptySession.getUser());
    }

    @DisplayName("AllArgsConstructor - Creates Session with all parameters")
    @Test
    void testAllArgsConstructor() {
        Session newSession = new Session(
                2L,
                testUser,
                "new-access-token",
                "new-refresh-token",
                tomorrow,
                tomorrow.plusDays(7),
                now,
                null,
                true,
                "10.0.0.1",
                "Chrome/91.0"
        );

        assertEquals(2L, newSession.getId());
        assertEquals(testUser, newSession.getUser());
        assertEquals("new-access-token", newSession.getAccessToken());
        assertEquals("new-refresh-token", newSession.getRefreshToken());
        assertEquals(tomorrow, newSession.getExpiresAt());
        assertEquals(tomorrow.plusDays(7), newSession.getRefreshTokenExpiresAt());
        assertEquals(now, newSession.getCreatedAt());
        assertNull(newSession.getRevokedAt());
        assertTrue(newSession.getActive());
        assertEquals("10.0.0.1", newSession.getIpAddress());
        assertEquals("Chrome/91.0", newSession.getUserAgent());
    }

    // ==================== ID Property Tests ====================

    @ParameterizedTest
    @ValueSource(longs = { 1L, 100L, 999999L })
    @DisplayName("setId and getId - Valid IDs")
    void testIdPropertyWithValidValues(Long id) {
        session.setId(id);
        assertEquals(id, session.getId());
    }

    @DisplayName("setId - Null ID is allowed")
    @Test
    void testIdPropertyWithNullValue() {
        session.setId(null);
        assertNull(session.getId());
    }

    // ==================== User Property Tests ====================

    @DisplayName("setUser and getUser - Valid User object")
    @Test
    void testUserPropertyWithValidUser() {
        User newUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .email("another@example.com")
                .passwordHash("$2a$10$anotherHash")
                .isActive(true)
                .createdAt(yesterday)
                .roles(new HashSet<>())
                .build();

        session.setUser(newUser);
        assertEquals(newUser, session.getUser());
        assertEquals(2L, session.getUser().getId());
    }

    @DisplayName("setUser - Null User is allowed")
    @Test
    void testUserPropertyWithNullValue() {
        session.setUser(null);
        assertNull(session.getUser());
    }

    // ==================== Token Property Tests ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
            "access-token-12345",
            "very-long-access-token-with-many-characters-abcdefghijklmnopqrstuvwxyz"
    })
    @DisplayName("setAccessToken and getAccessToken - Valid tokens")
    void testAccessTokenPropertyWithValidTokens(String token) {
        session.setAccessToken(token);
        assertEquals(token, session.getAccessToken());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
            "refresh-token-67890",
            "very-long-refresh-token-with-many-characters-xyz"
    })
    @DisplayName("setRefreshToken and getRefreshToken - Valid tokens")
    void testRefreshTokenPropertyWithValidTokens(String token) {
        session.setRefreshToken(token);
        assertEquals(token, session.getRefreshToken());
    }

    @DisplayName("setAccessToken - Null token is allowed")
    @Test
    void testAccessTokenPropertyWithNullValue() {
        session.setAccessToken(null);
        assertNull(session.getAccessToken());
    }

    @DisplayName("setRefreshToken - Null token is allowed")
    @Test
    void testRefreshTokenPropertyWithNullValue() {
        session.setRefreshToken(null);
        assertNull(session.getRefreshToken());
    }

    // ==================== Expiration DateTime Tests ====================

    @ParameterizedTest
    @MethodSource("provideExpirationDates")
    @DisplayName("setExpiresAt and getExpiresAt - Valid expiration dates")
    void testExpiresAtPropertyWithValidDates(LocalDateTime expirationDate) {
        session.setExpiresAt(expirationDate);
        assertEquals(expirationDate, session.getExpiresAt());
    }

    @ParameterizedTest
    @MethodSource("provideRefreshTokenExpirationDates")
    @DisplayName("setRefreshTokenExpiresAt and getRefreshTokenExpiresAt - Valid dates")
    void testRefreshTokenExpiresAtPropertyWithValidDates(LocalDateTime expirationDate) {
        session.setRefreshTokenExpiresAt(expirationDate);
        assertEquals(expirationDate, session.getRefreshTokenExpiresAt());
    }

    @DisplayName("setExpiresAt - Null expiration is allowed")
    @Test
    void testExpiresAtPropertyWithNullValue() {
        session.setExpiresAt(null);
        assertNull(session.getExpiresAt());
    }

    @DisplayName("setRefreshTokenExpiresAt - Null expiration is allowed")
    @Test
    void testRefreshTokenExpiresAtPropertyWithNullValue() {
        session.setRefreshTokenExpiresAt(null);
        assertNull(session.getRefreshTokenExpiresAt());
    }

    // ==================== CreatedAt Property Tests ====================

    @ParameterizedTest
    @MethodSource("provideCreationDates")
    @DisplayName("setCreatedAt and getCreatedAt - Valid creation dates")
    void testCreatedAtPropertyWithValidDates(LocalDateTime creationDate) {
        session.setCreatedAt(creationDate);
        assertEquals(creationDate, session.getCreatedAt());
    }

    @DisplayName("setCreatedAt - Null creation date is allowed")
    @Test
    void testCreatedAtPropertyWithNullValue() {
        session.setCreatedAt(null);
        assertNull(session.getCreatedAt());
    }

    // ==================== RevokedAt Property Tests ====================

    @ParameterizedTest
    @MethodSource("provideRevocationDates")
    @DisplayName("setRevokedAt and getRevokedAt - Valid revocation dates")
    void testRevokedAtPropertyWithValidDates(LocalDateTime revocationDate) {
        session.setRevokedAt(revocationDate);
        assertEquals(revocationDate, session.getRevokedAt());
    }

    @DisplayName("setRevokedAt - Null revocation date is allowed")
    @Test
    void testRevokedAtPropertyWithNullValue() {
        session.setRevokedAt(null);
        assertNull(session.getRevokedAt());
    }

    // ==================== Active Status Tests ====================

    @ParameterizedTest
    @CsvSource({
            "true",
            "false"
    })
    @DisplayName("setActive and getActive - Valid boolean values")
    void testActivePropertyWithValidValues(Boolean active) {
        session.setActive(active);
        assertEquals(active, session.getActive());
    }

    @DisplayName("setActive - Default value is true")
    @Test
    void testActivePropertyDefaultValue() {
        Session newSession = new Session();
        assertTrue(newSession.getActive());
    }

    @DisplayName("setActive - Null active status is allowed")
    @Test
    void testActivePropertyWithNullValue() {
        session.setActive(null);
        assertNull(session.getActive());
    }

    // ==================== IP Address Property Tests ====================

    @ParameterizedTest
    @CsvSource({
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "8.8.8.8",
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
    })
    @DisplayName("setIpAddress and getIpAddress - Valid IP addresses")
    void testIpAddressPropertyWithValidAddresses(String ipAddress) {
        session.setIpAddress(ipAddress);
        assertEquals(ipAddress, session.getIpAddress());
    }

    @DisplayName("setIpAddress - Null IP address is allowed")
    @Test
    void testIpAddressPropertyWithNullValue() {
        session.setIpAddress(null);
        assertNull(session.getIpAddress());
    }

    @DisplayName("setIpAddress - Empty string is allowed")
    @Test
    void testIpAddressPropertyWithEmptyString() {
        session.setIpAddress("");
        assertEquals("", session.getIpAddress());
    }

    // ==================== User Agent Property Tests ====================

    @ParameterizedTest
    @CsvSource({
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
            "Mozilla/5.0 (Linux; Android 11)",
            "Chrome/91.0.4472.124",
            "Safari/605.1.15"
    })
    @DisplayName("setUserAgent and getUserAgent - Valid user agents")
    void testUserAgentPropertyWithValidAgents(String userAgent) {
        session.setUserAgent(userAgent);
        assertEquals(userAgent, session.getUserAgent());
    }

    @DisplayName("setUserAgent - Null user agent is allowed")
    @Test
    void testUserAgentPropertyWithNullValue() {
        session.setUserAgent(null);
        assertNull(session.getUserAgent());
    }

    @DisplayName("setUserAgent - Empty string is allowed")
    @Test
    void testUserAgentPropertyWithEmptyString() {
        session.setUserAgent("");
        assertEquals("", session.getUserAgent());
    }

    // ==================== Property Method Sources ====================

    private static Stream<LocalDateTime> provideExpirationDates() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                now.plusHours(1),
                now.plusDays(1),
                now.plusDays(7),
                now.plusMonths(1),
                now.minusHours(1)
        );
    }

    private static Stream<LocalDateTime> provideRefreshTokenExpirationDates() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                now.plusDays(1),
                now.plusDays(7),
                now.plusDays(30),
                now.plusMonths(1),
                now.minusDays(1)
        );
    }

    private static Stream<LocalDateTime> provideCreationDates() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                now,
                now.minusMinutes(5),
                now.minusHours(1),
                now.minusDays(1),
                now.minusMonths(1)
        );
    }

    private static Stream<LocalDateTime> provideRevocationDates() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                now,
                now.minusMinutes(30),
                now.minusHours(2),
                now.minusDays(1)
        );
    }
}
