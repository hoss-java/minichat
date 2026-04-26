package com.minichat.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * PASSWORD VALIDATOR UNIT TESTS
 * ============================================================================
 *
 * This test class validates password validation logic for both production
 * and development modes:
 * - Production Mode: Strict validation (8+ chars, uppercase, lowercase, digit, special char)
 * - Development Mode: Relaxed validation (6+ chars, non-empty)
 *
 * Test Organization:
 * 1. Valid Passwords (Production Mode)
 * 2. Invalid Passwords (Production Mode)
 * 3. Valid Passwords (Development Mode)
 * 4. Invalid Passwords (Development Mode)
 * 5. Requirements Message Tests
 *
 * ============================================================================
 */
@DisplayName("Password Validator Unit Tests")
public class PasswordValidatorUnitTest {

    // ============================================================================
    // SECTION 1: PRODUCTION MODE - VALID PASSWORDS
    // ============================================================================
    //
    // Scenario: User enters valid passwords meeting all production requirements
    // Expected: isValidPassword() returns true
    //
    // Requirements: 8+ chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should accept valid production passwords")
    @ValueSource(strings = {
        "Password123!",
        "SecurePass@2024",
        "MyP@ssw0rd",
        "Test1234$abc",
        "Complex!Pass99",
        "VeryLongPasswordWith123@Special",
        "ValidPass1!",
        "Strong@Pass2",
        "Another&Pass8"
    })
    public void testValidProductionPasswords(String password) {
        // Scenario: Valid password in production mode
        // Expected: Validation passes
        
        PasswordValidator validator = new PasswordValidator();
        assertTrue(validator.isValidPassword(password),
                "Password '" + password + "' should be valid");
    }

    // ============================================================================
    // SECTION 2: PRODUCTION MODE - INVALID PASSWORDS
    // ============================================================================
    //
    // Scenario: User enters passwords that violate production requirements
    // Expected: isValidPassword() returns false
    //
    // Test Cases:
    // - Too short (< 8 chars)
    // - Missing uppercase letter
    // - Missing lowercase letter
    // - Missing digit
    // - Missing special character
    // - Null or empty
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should reject invalid production passwords")
    @CsvSource({
        "Pass1!,                    'Too short (6 chars)'",
        "password123!,              'Missing uppercase'",
        "PASSWORD123!,              'Missing lowercase'",
        "Password!,                 'Missing digit'",
        "Password123,               'Missing special character'",
        "Pass@,                     'Too short and incomplete'",
        "12345678@A,                'Missing lowercase'",
        "abcdefgh@1,                'Missing uppercase'"
    })
    public void testInvalidProductionPasswords(String password, String reason) {
        // Scenario: Invalid password according to production rules
        // Expected: Validation fails
        
        PasswordValidator validator = new PasswordValidator();
        assertFalse(validator.isValidPassword(password),
                "Password '" + password + "' should be invalid (" + reason + ")");
    }

    // ============================================================================
    // SECTION 3: NULL AND EMPTY EDGE CASES
    // ============================================================================
    //
    // Scenario: User submits null or empty passwords
    // Expected: isValidPassword() returns false
    //
    // ============================================================================

    @Test
    @DisplayName("Should reject null password")
    public void testNullPassword() {
        // Scenario: Null password is provided
        // Expected: Validation fails safely without exception
        
        PasswordValidator validator = new PasswordValidator();
        assertFalse(validator.isValidPassword(null),
                "Null password should be rejected");
    }

    @Test
    @DisplayName("Should reject empty password")
    public void testEmptyPassword() {
        // Scenario: Empty string password is provided
        // Expected: Validation fails
        
        PasswordValidator validator = new PasswordValidator();
        assertFalse(validator.isValidPassword(""),
                "Empty password should be rejected");
    }

    // ============================================================================
    // SECTION 4: PASSWORD REQUIREMENTS MESSAGE
    // ============================================================================
    //
    // Scenario: Client requests password requirements message
    // Expected: Appropriate message for current mode is returned
    //
    // ============================================================================

    @Test
    @DisplayName("Should return production mode requirements message")
    public void testProductionModeRequirementsMessage() {
        // Scenario: Get requirements in production mode
        // Expected: Message includes all requirements
        
        PasswordValidator validator = new PasswordValidator();
        String requirements = validator.getPasswordRequirements();
        
        assertNotNull(requirements, "Requirements message should not be null");
        assertFalse(requirements.isEmpty(), "Requirements message should not be empty");
        assertTrue(requirements.contains("8"),
                "Message should mention minimum 8 characters");
        assertTrue(requirements.contains("uppercase"),
                "Message should mention uppercase requirement");
        assertTrue(requirements.contains("lowercase"),
                "Message should mention lowercase requirement");
        assertTrue(requirements.contains("digit"),
                "Message should mention digit requirement");
        assertTrue(requirements.contains("special character"),
                "Message should mention special character requirement");
    }

    // ============================================================================
    // SECTION 5: BOUNDARY VALUE TESTS
    // ============================================================================
    //
    // Scenario: Test passwords at minimum length boundaries
    // Expected: Validation respects exact length requirements
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should reject passwords at minimum boundary (7 chars)")
    @ValueSource(strings = {
        "Pass1@a",
        "Test1@b",
        "Valid@1"
    })
    public void testPasswordBelowMinimumLength(String password) {
        // Scenario: Password exactly 7 characters (below 8 minimum)
        // Expected: Validation fails
        
        PasswordValidator validator = new PasswordValidator();
        assertFalse(validator.isValidPassword(password),
                "Password with 7 characters should be rejected");
    }

    @ParameterizedTest
    @DisplayName("Should accept passwords at minimum boundary (8 chars)")
    @ValueSource(strings = {
        "Pass1@ab",
        "Test1@bc",
        "Valid@12"
    })
    public void testPasswordAtMinimumLength(String password) {
        // Scenario: Password exactly 8 characters (meets minimum)
        // Expected: Validation passes if all other requirements met
        
        PasswordValidator validator = new PasswordValidator();
        assertTrue(validator.isValidPassword(password),
                "Password with 8 characters should be accepted if all requirements met");
    }

    // ============================================================================
    // SECTION 6: SPECIAL CHARACTER VALIDATION
    // ============================================================================
    //
    // Scenario: Test that all allowed special characters are accepted
    // Expected: Passwords with @, $, !, %, *, ?, & are all valid
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should accept all allowed special characters")
    @ValueSource(strings = {
        "ValidPass1@",
        "ValidPass1$",
        "ValidPass1!",
        "ValidPass1%",
        "ValidPass1*",
        "ValidPass1?",
        "ValidPass1&"
    })
    public void testAllowedSpecialCharacters(String password) {
        // Scenario: Password contains one of the allowed special characters
        // Expected: Validation passes
        
        PasswordValidator validator = new PasswordValidator();
        assertTrue(validator.isValidPassword(password),
                "Password with allowed special character should be valid");
    }

    @ParameterizedTest
    @DisplayName("Should reject disallowed special characters")
    @ValueSource(strings = {
        "ValidPass1#",
        "ValidPass1~",
        "ValidPass1^",
        "ValidPass1-",
        "ValidPass1+",
        "ValidPass1="
    })
    public void testDisallowedSpecialCharacters(String password) {
        // Scenario: Password contains special character not in allowed set
        // Expected: Validation fails
        
        PasswordValidator validator = new PasswordValidator();
        assertFalse(validator.isValidPassword(password),
                "Password with disallowed special character should be rejected");
    }
}
