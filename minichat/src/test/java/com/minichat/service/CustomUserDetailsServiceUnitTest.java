package com.minichat.service;

import com.minichat.entity.Role;
import com.minichat.entity.RoleType;
import com.minichat.entity.User;
import com.minichat.repository.UserRepository;
import com.minichat.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomUserDetailsService.
 * 
 * Tests cover:
 * - Successful user loading with various role combinations
 * - User not found exception handling
 * - Authority mapping from roles
 * - Account status flags based on user properties
 * - Edge cases with empty roles and inactive users
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
public class CustomUserDetailsServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private Set<Role> testRoles;

    @BeforeEach
    public void setUp() {
        // Initialize common test data
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashedpassword123");
        testUser.setIsActive(true);
        testRoles = new HashSet<>();
    }

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("Should load user with ROLE_USER authority when user has USER role")
    public void testLoadUserByUsername_WithUserRole_Success() {
        // Scenario: User exists with USER role and is active
        Role userRole = new Role();
        userRole.setName(RoleType.USER);
        testRoles.add(userRole);
        testUser.setRoles(testRoles);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Expected: UserDetails returned with ROLE_USER authority
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals("testuser", userDetails.getUsername(), "Username should match");
        assertEquals("hashedpassword123", userDetails.getPassword(), "Password should match");
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")),
                "Should contain ROLE_USER authority");
        assertTrue(userDetails.isAccountNonExpired(), "Account should not be expired");
        assertTrue(userDetails.isAccountNonLocked(), "Account should not be locked");
        assertTrue(userDetails.isCredentialsNonExpired(), "Credentials should not be expired");
        assertTrue(userDetails.isEnabled(), "Account should be enabled");

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should load user with multiple authorities when user has multiple roles")
    public void testLoadUserByUsername_WithMultipleRoles_Success() {
        // Scenario: User exists with USER, ADMIN, and MODERATOR roles
        Role userRole = new Role();
        userRole.setName(RoleType.USER);
        Role adminRole = new Role();
        adminRole.setName(RoleType.ADMIN);
        Role moderatorRole = new Role();
        moderatorRole.setName(RoleType.MODERATOR);

        testRoles.add(userRole);
        testRoles.add(adminRole);
        testRoles.add(moderatorRole);
        testUser.setRoles(testRoles);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Expected: UserDetails with all three role authorities
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals(3, userDetails.getAuthorities().size(), "Should have 3 authorities");
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")),
                "Should contain ROLE_USER");
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")),
                "Should contain ROLE_ADMIN");
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MODERATOR")),
                "Should contain ROLE_MODERATOR");

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @ParameterizedTest
    @DisplayName("Should load user successfully with different role types")
    @ValueSource(strings = {"USER", "ADMIN", "MODERATOR"})
    public void testLoadUserByUsername_VariousRoles_Success(String roleType) {
        // Scenario: User has different role types
        Role role = new Role();
        role.setName(RoleType.valueOf(roleType));
        testRoles.add(role);
        testUser.setRoles(testRoles);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Expected: UserDetails loaded with corresponding role authority
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails, "UserDetails should not be null");
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + roleType)),
                "Should contain ROLE_" + roleType);

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should load inactive user with disabled flag set to true")
    public void testLoadUserByUsername_InactiveUser_DisabledFlagSet() {
        // Scenario: User exists but is not active (disabled)
        testUser.setIsActive(false);
        Role userRole = new Role();
        userRole.setName(RoleType.USER);
        testRoles.add(userRole);
        testUser.setRoles(testRoles);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Expected: UserDetails with disabled flag set to true
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails, "UserDetails should not be null");
        assertFalse(userDetails.isEnabled(), "Account should be disabled when user is inactive");

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should load user with empty roles set without exception")
    public void testLoadUserByUsername_EmptyRoles_Success() {
        // Scenario: User exists but has no roles assigned
        testUser.setRoles(new HashSet<>()); // Empty roles

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Expected: UserDetails loaded successfully with empty authorities
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails, "UserDetails should not be null");
        assertTrue(userDetails.getAuthorities().isEmpty(), "Authorities should be empty");

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @ParameterizedTest
    @DisplayName("Should handle various username formats correctly")
    @CsvSource({
            "john.doe,john.doe",
            "admin@example.com,admin@example.com",
            "user_123,user_123",
            "a,a"
    })
    public void testLoadUserByUsername_VariousUsernameFormats_Success(String username, String expectedUsername) {
        // Scenario: Different username formats
        testUser.setUsername(expectedUsername);
        Role userRole = new Role();
        userRole.setName(RoleType.USER);
        testRoles.add(userRole);
        testUser.setRoles(testRoles);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Expected: User loaded with correct username
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertEquals(expectedUsername, userDetails.getUsername(), "Username should match");

        verify(userRepository, times(1)).findByUsername(username);
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    public void testLoadUserByUsername_UserNotFound_ThrowsException() {
        // Scenario: User doesn't exist in repository
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Expected: UsernameNotFoundException thrown with appropriate message
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistent"),
                "Should throw UsernameNotFoundException");

        assertTrue(exception.getMessage().contains("User not found with username"),
                "Exception message should contain descriptive text");
        assertTrue(exception.getMessage().contains("nonexistent"),
                "Exception message should contain the searched username");

        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @ParameterizedTest
    @DisplayName("Should throw UsernameNotFoundException for various non-existent usernames")
    @ValueSource(strings = {"ghost", "phantom", "invisible", "notauser"})
    public void testLoadUserByUsername_MultipleNonExistentUsers_ThrowsException(String username) {
        // Scenario: Various non-existent users
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Expected: UsernameNotFoundException thrown
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username),
                "Should throw UsernameNotFoundException for username: " + username);

        verify(userRepository, times(1)).findByUsername(username);
    }

    // ==================== Account Status Flag Tests ====================

    @Test
    @DisplayName("Should set all account status flags correctly for active user")
    public void testLoadUserByUsername_ActiveUser_AllFlagsCorrect() {
        // Scenario: Active user with all status flags expected to be positive
        testUser.setIsActive(true);
        Role userRole = new Role();
        userRole.setName(RoleType.USER);
        testRoles.add(userRole);
        testUser.setRoles(testRoles);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Expected: All account status flags indicate active, non-expired account
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertTrue(userDetails.isAccountNonExpired(),
                "Account should not be expired");
        assertTrue(userDetails.isAccountNonLocked(),
                "Account should not be locked");
        assertTrue(userDetails.isCredentialsNonExpired(),
                "Credentials should not be expired");
        assertTrue(userDetails.isEnabled(),
                "Account should be enabled when user is active");

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should correctly map role names with ROLE_ prefix")
    public void testLoadUserByUsername_RoleNameMapping_Correct() {
        // Scenario: Verify that role names are correctly prefixed with ROLE_
        Role adminRole = new Role();
        adminRole.setName(RoleType.ADMIN);
        testRoles.add(adminRole);
        testUser.setRoles(testRoles);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Expected: Authority should be "ROLE_ADMIN" not just "ADMIN"
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")),
                "Role should be prefixed with ROLE_");
        assertFalse(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN")),
                "Role should not exist without ROLE_ prefix");

        verify(userRepository, times(1)).findByUsername("testuser");
    }
}
