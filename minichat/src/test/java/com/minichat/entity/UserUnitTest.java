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
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity class.
 * Tests all public methods and field validations in isolation.
 * Covers happy path, edge cases, and error scenarios using parameterized tests.
 */
@DisplayName("User Entity Unit Tests")
public class UserUnitTest {

    private User user;
    private Role userRole;
    private Role adminRole;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Setup roles
        userRole = Role.builder()
                .id(1L)
                .name(RoleType.USER)
                .description("User role")
                .createdAt(now)
                .build();

        adminRole = Role.builder()
                .id(2L)
                .name(RoleType.ADMIN)
                .description("Admin role")
                .createdAt(now)
                .build();

        // Setup test user
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .publicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBg...")
                .fingerprint("ABC123DEF456")
                .roles(new HashSet<>(Set.of(userRole)))
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .lastLogin(now.minusHours(1))
                .isActive(true)
                .build();
    }

    // ============= Constructor & Builder Tests =============

    @DisplayName("User - No-arg constructor creates valid user")
    @Test
    void testNoArgConstructor() {
        User newUser = new User();
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getEmail());
    }

    @DisplayName("User - Builder creates user with all fields")
    @Test
    void testBuilderCreatesCompleteUser() {
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("$2a$10$hashedPassword", user.getPasswordHash());
        assertTrue(user.getIsActive());
    }

    @DisplayName("User - Builder sets default isActive to true")
    @Test
    void testBuilderDefaultIsActive() {
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .passwordHash("hash")
                .build();

        assertTrue(newUser.getIsActive());
    }

    @DisplayName("User - Builder initializes roles as empty HashSet")
    @Test
    void testBuilderDefaultRoles() {
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .passwordHash("hash")
                .build();

        assertNotNull(newUser.getRoles());
        assertEquals(0, newUser.getRoles().size());
        assertTrue(newUser.getRoles() instanceof HashSet);
    }

    // ============= Username Tests =============

    @DisplayName("User - Set and get username")
    @ParameterizedTest
    @ValueSource(strings = {
            "user123",
            "john_doe",
            "admin-user",
            "testuser.name",
            "user@work"
    })
    void testSetAndGetUsername(String username) {
        user.setUsername(username);
        assertEquals(username, user.getUsername());
    }

    @DisplayName("User - Username with special characters")
    @ParameterizedTest
    @ValueSource(strings = {"user!", "user@", "user#", "user$"})
    void testUsernameWithSpecialCharacters(String username) {
        user.setUsername(username);
        assertEquals(username, user.getUsername());
    }

    // ============= Email Tests =============

    @DisplayName("User - Set and get email")
    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "admin@company.org",
            "test.user@domain.co.uk",
            "user+tag@example.com",
            "123@example.com"
    })
    void testSetAndGetEmail(String email) {
        user.setEmail(email);
        assertEquals(email, user.getEmail());
    }

    // ============= Password Hash Tests =============

    @DisplayName("User - Set and get password hash")
    @ParameterizedTest
    @ValueSource(strings = {
            "$2a$10$hashedPassword",
            "$2b$12$anotherHashedPassword",
            "plainTextHash",
            ""
    })
    void testSetAndGetPasswordHash(String hash) {
        user.setPasswordHash(hash);
        assertEquals(hash, user.getPasswordHash());
    }

    // ============= Public Key Tests =============

//    @DisplayName("User - Set and get public key")
//    @ParameterizedTest
//    @ValueSource(strings = {
//            "-----BEGIN PUBLIC KEY-----\nMIIBIjANBg...",
//            "-----BEGIN RSA PUBLIC KEY-----\n...",
//            "",
//            null
//    })
//    void testSetAndGetPublicKey(String publicKey) {
//        user.setPublicKey(publicKey);
//        assertEquals(publicKey, user.getPublicKey());
//    }

    // ============= Fingerprint Tests =============

    @DisplayName("User - Set and get fingerprint")
    @ParameterizedTest
    @ValueSource(strings = {
            "ABC123DEF456",
            "1234567890ABCDEF",
            "SHA256:1234567890abcdef",
            ""
    })
    void testSetAndGetFingerprint(String fingerprint) {
        user.setFingerprint(fingerprint);
        assertEquals(fingerprint, user.getFingerprint());
    }

    // ============= Role Tests =============

    @DisplayName("User - Add single role to user")
    @Test
    void testAddSingleRole() {
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .passwordHash("hash")
                .build();

        newUser.getRoles().add(userRole);

        assertEquals(1, newUser.getRoles().size());
        assertTrue(newUser.getRoles().contains(userRole));
    }

    @DisplayName("User - Add multiple roles to user")
    @Test
    void testAddMultipleRoles() {
        Role moderatorRole = Role.builder()
                .id(3L)
                .name(RoleType.MODERATOR)
                .description("Moderator role")
                .createdAt(now)
                .build();

        user.getRoles().add(adminRole);
        user.getRoles().add(moderatorRole);

        assertEquals(3, user.getRoles().size());
        assertTrue(user.getRoles().contains(userRole));
        assertTrue(user.getRoles().contains(adminRole));
        assertTrue(user.getRoles().contains(moderatorRole));
    }

    @DisplayName("User - Remove role from user")
    @Test
    void testRemoveRole() {
        user.getRoles().remove(userRole);

        assertEquals(0, user.getRoles().size());
        assertFalse(user.getRoles().contains(userRole));
    }

    @DisplayName("User - Replace all roles")
    @Test
    void testReplaceAllRoles() {
        Set<Role> newRoles = new HashSet<>(Set.of(adminRole));
        user.setRoles(newRoles);

        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(adminRole));
        assertFalse(user.getRoles().contains(userRole));
    }

    @DisplayName("User - Clear all roles")
    @Test
    void testClearAllRoles() {
        user.getRoles().clear();

        assertEquals(0, user.getRoles().size());
        assertTrue(user.getRoles().isEmpty());
    }

    // ============= Timestamp Tests =============

    @DisplayName("User - Set and get created timestamp")
    @ParameterizedTest
    @MethodSource("provideLocalDateTimes")
    void testSetAndGetCreatedAt(LocalDateTime dateTime) {
        user.setCreatedAt(dateTime);
        assertEquals(dateTime, user.getCreatedAt());
    }

    @DisplayName("User - Set and get updated timestamp")
    @ParameterizedTest
    @MethodSource("provideLocalDateTimes")
    void testSetAndGetUpdatedAt(LocalDateTime dateTime) {
        user.setUpdatedAt(dateTime);
        assertEquals(dateTime, user.getUpdatedAt());
    }

    @DisplayName("User - Set and get last login timestamp")
    @ParameterizedTest
    @MethodSource("provideLocalDateTimes")
    void testSetAndGetLastLogin(LocalDateTime dateTime) {
        user.setLastLogin(dateTime);
        assertEquals(dateTime, user.getLastLogin());
    }

    @DisplayName("User - Last login can be null")
    @Test
    void testLastLoginCanBeNull() {
        user.setLastLogin(null);
        assertNull(user.getLastLogin());
    }

    // ============= Active Status Tests =============

    @DisplayName("User - Set and get active status")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSetAndGetIsActive(Boolean active) {
        user.setIsActive(active);
        assertEquals(active, user.getIsActive());
    }

    @DisplayName("User - Active status with various boolean values")
    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, false",
            "true, false"
    })
    void testIsActiveTransitions(Boolean initialValue, Boolean newValue) {
        user.setIsActive(initialValue);
        assertEquals(initialValue, user.getIsActive());

        user.setIsActive(newValue);
        assertEquals(newValue, user.getIsActive());
    }

    // ============= ID Tests =============

    @DisplayName("User - Set and get ID")
    @ParameterizedTest
    @ValueSource(longs = {1L, 100L, 999999L, Long.MAX_VALUE})
    void testSetAndGetId(Long id) {
        user.setId(id);
        assertEquals(id, user.getId());
    }

    @DisplayName("User - ID can be null before persistence")
    @Test
    void testIdCanBeNull() {
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .passwordHash("hash")
                .build();

        assertNull(newUser.getId());
    }

    // ============= AllArgsConstructor Tests =============

    @DisplayName("User - All args constructor creates valid user")
    @Test
    void testAllArgsConstructor() {
        Set<Role> roles = new HashSet<>(Set.of(userRole));
        User constructedUser = new User(
                1L,
                "testuser",
                "test@example.com",
                "$2a$10$hashedPassword",
                "publicKey",
                "fingerprint",
                roles,
                now.minusDays(1),
                now,
                now.minusHours(1),
                true
        );

        assertEquals(1L, constructedUser.getId());
        assertEquals("testuser", constructedUser.getUsername());
        assertEquals("test@example.com", constructedUser.getEmail());
        assertEquals("$2a$10$hashedPassword", constructedUser.getPasswordHash());
        assertEquals("publicKey", constructedUser.getPublicKey());
        assertEquals("fingerprint", constructedUser.getFingerprint());
        assertEquals(roles, constructedUser.getRoles());
        assertEquals(now.minusDays(1), constructedUser.getCreatedAt());
        assertEquals(now, constructedUser.getUpdatedAt());
        assertEquals(now.minusHours(1), constructedUser.getLastLogin());
        assertTrue(constructedUser.getIsActive());
    }

    // ============= Data Equality Tests =============

    @DisplayName("User - Equals and hashCode consistency")
    @Test
    void testEqualsAndHashCodeConsistency() {
        User user1 = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hash")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hash")
                .build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @DisplayName("User - Different users are not equal")
    @Test
    void testDifferentUsersNotEqual() {
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .passwordHash("hash1")
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .passwordHash("hash2")
                .build();

        assertNotEquals(user1, user2);
    }

    @DisplayName("User - toString includes username and email")
    @Test
    void testToStringFormat() {
        String userString = user.toString();

        assertNotNull(userString);
        assertTrue(userString.contains("testuser") || userString.contains("username"));
        assertTrue(userString.contains("test@example.com") || userString.contains("email"));
    }

    // ============= Helper Methods =============

    static Stream<LocalDateTime> provideLocalDateTimes() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                now,
                now.minusDays(1),
                now.plusDays(1),
                now.minusHours(5),
                now.plusHours(10),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0)
        );
    }
}
