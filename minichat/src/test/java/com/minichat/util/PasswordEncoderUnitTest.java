package com.minichat.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * PASSWORD ENCODER UNIT TESTS
 * ============================================================================
 *
 * This test class validates BCrypt password encoding and matching functionality:
 * - Encoding raw passwords to BCrypt hashes
 * - Matching raw passwords against encoded passwords
 * - Validity checking of password pairs
 * - Null/empty password handling
 * - Security properties (hashes are different for same password)
 *
 * Test Organization:
 * 1. Password Encoding Tests
 * 2. Password Matching Tests
 * 3. Password Validity Tests
 * 4. Edge Cases (null, empty passwords)
 * 5. Security Properties
 *
 * ============================================================================
 */
@DisplayName("Password Encoder Unit Tests")
public class PasswordEncoderUnitTest {

    // ============================================================================
    // SECTION 1: PASSWORD ENCODING TESTS
    // ============================================================================
    //
    // Scenario: Raw passwords are encoded to BCrypt hash
    // Expected: Encoded password is non-null, non-empty, and different from raw
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should successfully encode valid passwords")
    @ValueSource(strings = {
        "Password123!",
        "SecurePass@2024",
        "MyP@ssw0rd",
        "Test1234$abc",
        "ComplexPassword99&",
        "VeryLongPasswordWith123@Special"
    })
    public void testEncodeValidPasswords(String rawPassword) {
        // Scenario: Raw password is encoded
        // Expected: Returns non-null, non-empty BCrypt hash different from original

        String encodedPassword = PasswordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertFalse(encodedPassword.isEmpty(), "Encoded password should not be empty");
        assertNotEquals(rawPassword, encodedPassword,
                "Encoded password should differ from raw password");
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"),
                "Encoded password should be valid BCrypt format");
    }

    @Test
    @DisplayName("Should encode single character password")
    public void testEncodeSingleCharacterPassword() {
        // Scenario: Single character password is encoded
        // Expected: Successfully encodes despite being very short

        String rawPassword = "a";
        String encodedPassword = PasswordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertFalse(encodedPassword.isEmpty(), "Encoded password should not be empty");
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"),
                "Should produce valid BCrypt hash");
    }

    @Test
    @DisplayName("Should encode password with special characters")
    public void testEncodePasswordWithSpecialCharacters() {
        // Scenario: Password contains various special characters
        // Expected: Successfully encodes without issues

        String rawPassword = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
        String encodedPassword = PasswordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Should be different from raw");
    }

    @Test
    @DisplayName("Should encode unicode characters in password")
    public void testEncodePasswordWithUnicodeCharacters() {
        // Scenario: Password contains unicode/non-ASCII characters
        // Expected: Successfully encodes unicode characters

        String rawPassword = "Pässwörd123!Ñ";
        String encodedPassword = PasswordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "Should handle unicode characters");
        assertNotEquals(rawPassword, encodedPassword, "Should be different from raw");
    }

    // ============================================================================
    // SECTION 2: PASSWORD MATCHING TESTS
    // ============================================================================
    //
    // Scenario: Raw password is matched against encoded password
    // Expected: matches() returns true for correct password, false for incorrect
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should match correct password against encoded password")
    @ValueSource(strings = {
        "Password123!",
        "SecurePass@2024",
        "MyP@ssw0rd",
        "Test1234$abc",
        "ComplexPassword99&"
    })
    public void testMatchesCorrectPassword(String rawPassword) {
        // Scenario: Correct raw password is matched against its encoding
        // Expected: matches() returns true

        String encodedPassword = PasswordEncoder.encode(rawPassword);
        boolean matches = PasswordEncoder.matches(rawPassword, encodedPassword);

        assertTrue(matches, "Correct password should match encoded password");
    }

    @ParameterizedTest
    @DisplayName("Should not match incorrect password against encoded password")
    @CsvSource({
        "Password123!, WrongPassword123!",
        "SecurePass@2024, SecurePass@2025",
        "MyP@ssw0rd, MyP@ssw0re",
        "Test1234$abc, Test1234$abd",
        "Correct123!, incorrect123!"
    })
    public void testDoesNotMatchIncorrectPassword(String correctPassword, String wrongPassword) {
        // Scenario: Incorrect password is matched against correct password's encoding
        // Expected: matches() returns false

        String encodedPassword = PasswordEncoder.encode(correctPassword);
        boolean matches = PasswordEncoder.matches(wrongPassword, encodedPassword);

        assertFalse(matches, "Incorrect password should not match encoded password");
    }

    @Test
    @DisplayName("Should not match with case sensitivity")
    public void testMatchingIsCaseSensitive() {
        // Scenario: Password with different case is matched
        // Expected: Does not match due to case difference

        String rawPassword = "Password123!";
        String encodedPassword = PasswordEncoder.encode(rawPassword);
        boolean matches = PasswordEncoder.matches("password123!", encodedPassword);

        assertFalse(matches, "Case-sensitive matching should fail for different case");
    }

    @Test
    @DisplayName("Should not match with extra whitespace")
    public void testMatchingWithWhitespace() {
        // Scenario: Password with extra spaces is matched
        // Expected: Does not match

        String rawPassword = "Password123!";
        String encodedPassword = PasswordEncoder.encode(rawPassword);
        boolean matches = PasswordEncoder.matches("Password123! ", encodedPassword);

        assertFalse(matches, "Password with extra whitespace should not match");
    }

    // ============================================================================
    // SECTION 3: PASSWORD VALIDITY TESTS
    // ============================================================================
    //
    // Scenario: isValid() checks if raw password matches encoded password
    // Expected: Returns true for matching passwords, false otherwise
    //
    // Note: isValid() is an alias for matches()
    //
    // ============================================================================

    @ParameterizedTest
    @DisplayName("Should validate correct password")
    @ValueSource(strings = {
        "Password123!",
        "SecurePass@2024",
        "MyP@ssw0rd",
        "Test1234$abc"
    })
    public void testIsValidWithCorrectPassword(String rawPassword) {
        // Scenario: Correct password is validated
        // Expected: isValid() returns true

        String encodedPassword = PasswordEncoder.encode(rawPassword);
        boolean isValid = PasswordEncoder.isValid(rawPassword, encodedPassword);

        assertTrue(isValid, "Valid password should pass isValid() check");
    }

    @ParameterizedTest
    @DisplayName("Should invalidate incorrect password")
    @CsvSource({
        "Password123!, WrongPassword123!",
        "SecurePass@2024, WrongPass@2024",
        "MyP@ssw0rd, MyP@ssw0re"
    })
    public void testIsValidWithIncorrectPassword(String correctPassword, String wrongPassword) {
        // Scenario: Incorrect password is validated
        // Expected: isValid() returns false

        String encodedPassword = PasswordEncoder.encode(correctPassword);
        boolean isValid = PasswordEncoder.isValid(wrongPassword, encodedPassword);

        assertFalse(isValid, "Invalid password should fail isValid() check");
    }

    // ============================================================================
    // SECTION 4: NULL AND EMPTY EDGE CASES
    // ============================================================================
    //
    // Scenario: Null or empty passwords are processed
    // Expected: Handles gracefully (BCrypt behavior)
    //
    // ============================================================================

    @Test
    @DisplayName("Should handle empty string password")
    public void testEncodeEmptyPassword() {
        // Scenario: Empty string is encoded
        // Expected: Successfully encodes empty string

        String encodedPassword = PasswordEncoder.encode("");

        assertNotNull(encodedPassword, "Should encode empty string");
        assertFalse(encodedPassword.isEmpty(), "Encoded result should not be empty");
    }

    @Test
    @DisplayName("Should match empty password correctly")
    public void testMatchEmptyPassword() {
        // Scenario: Empty string is matched against its encoding
        // Expected: matches() returns true

        String encodedPassword = PasswordEncoder.encode("");
        boolean matches = PasswordEncoder.matches("", encodedPassword);

        assertTrue(matches, "Empty password should match its encoding");
    }

    @Test
    @DisplayName("Should not match non-empty against empty encoded password")
    public void testMatchNonEmptyAgainstEmptyEncoded() {
        // Scenario: Non-empty password matched against empty password's encoding
        // Expected: matches() returns false

        String encodedEmptyPassword = PasswordEncoder.encode("");
        boolean matches = PasswordEncoder.matches("SomePassword123!", encodedEmptyPassword);

        assertFalse(matches, "Non-empty password should not match empty encoding");
    }

    // ============================================================================
    // SECTION 5: SECURITY PROPERTIES
    // ============================================================================
    //
    // Scenario: Same password encoded multiple times produces different hashes
    // Expected: Each encoding produces unique hash (BCrypt salt property)
    //
    // ============================================================================

    @Test
    @DisplayName("Should produce different hashes for same password (salt variation)")
    public void testEncodingProducesDifferentHashesSameSalt() {
        // Scenario: Same password encoded twice
        // Expected: Produces different hashes due to random salt

        String rawPassword = "Password123!";
        String hash1 = PasswordEncoder.encode(rawPassword);
        String hash2 = PasswordEncoder.encode(rawPassword);

        assertNotEquals(hash1, hash2,
                "Same password should produce different hashes (different salts)");
    }

    @Test
    @DisplayName("Should verify both hashes match the same password")
    public void testBothDifferentHashesMatchSamePassword() {
        // Scenario: Two different hashes of same password both match that password
        // Expected: Both matches return true

        String rawPassword = "Password123!";
        String hash1 = PasswordEncoder.encode(rawPassword);
        String hash2 = PasswordEncoder.encode(rawPassword);

        boolean matches1 = PasswordEncoder.matches(rawPassword, hash1);
        boolean matches2 = PasswordEncoder.matches(rawPassword, hash2);

        assertTrue(matches1, "First hash should match password");
        assertTrue(matches2, "Second hash should match password");
    }

    @Test
    @DisplayName("Should not match hash1 with hash2 (hashes are not passwords)")
    public void testHashesDoNotMatchEachOther() {
        // Scenario: Two different hashes of same password are compared
        // Expected: Hashes do not match each other (they're encrypted, not plaintext)

        String rawPassword = "Password123!";
        String hash1 = PasswordEncoder.encode(rawPassword);
        String hash2 = PasswordEncoder.encode(rawPassword);

        boolean hashesMatch = PasswordEncoder.matches(hash1, hash2);

        assertFalse(hashesMatch,
                "Different hashes should not match (unless by chance, extremely rare)");
    }

    // ============================================================================
    // SECTION 6: LONG PASSWORD TESTS
    // ============================================================================
    //
    // Scenario: Very long passwords are encoded and matched
    // Expected: Successfully handles long strings
    //
    // ============================================================================

    @Test
    @DisplayName("Should encode and match very long password")
    public void testLongPasswordHandling() {
        // Scenario: Very long password (500+ characters) is encoded and matched
        // Expected: Successfully encodes and matches

        String longPassword = "a".repeat(500) + "123!";
        String encodedPassword = PasswordEncoder.encode(longPassword);
        boolean matches = PasswordEncoder.matches(longPassword, encodedPassword);

        assertTrue(matches, "Very long password should be encoded and matched correctly");
    }
}
