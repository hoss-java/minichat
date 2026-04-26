package com.minichat.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Role entity class.
 * Tests all properties, constructors, and getters/setters.
 * Uses parametrized tests for enum and data validation scenarios.
 */
@DisplayName("Role Entity Unit Tests")
public class RoleUnitTest {

    private Role role;
    private LocalDateTime now;
    private LocalDateTime yesterday;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        yesterday = now.minusDays(1);

        role = Role.builder()
                .id(1L)
                .name(RoleType.USER)
                .description("Standard user role")
                .createdAt(now)
                .build();
    }

    // ==================== Constructor Tests ====================

    @DisplayName("NoArgsConstructor - Creates empty Role instance")
    @Test
    void testNoArgsConstructor() {
        Role emptyRole = new Role();
        assertNotNull(emptyRole);
        assertNull(emptyRole.getId());
        assertNull(emptyRole.getName());
        assertNull(emptyRole.getDescription());
        assertNull(emptyRole.getCreatedAt());
    }

    @DisplayName("Builder - Creates Role with specified parameters")
    @Test
    void testBuilderConstructor() {
        Role builtRole = Role.builder()
                .id(2L)
                .name(RoleType.ADMIN)
                .description("Administrator role")
                .createdAt(yesterday)
                .build();

        assertEquals(2L, builtRole.getId());
        assertEquals(RoleType.ADMIN, builtRole.getName());
        assertEquals("Administrator role", builtRole.getDescription());
        assertEquals(yesterday, builtRole.getCreatedAt());
    }

    @DisplayName("AllArgsConstructor - Creates Role with all parameters")
    @Test
    void testAllArgsConstructor() {
        Role newRole = new Role(
                3L,
                RoleType.MODERATOR,
                "Content moderator role",
                now
        );

        assertEquals(3L, newRole.getId());
        assertEquals(RoleType.MODERATOR, newRole.getName());
        assertEquals("Content moderator role", newRole.getDescription());
        assertEquals(now, newRole.getCreatedAt());
    }

    // ==================== ID Property Tests ====================

    @ParameterizedTest
    @ValueSource(longs = { 1L, 100L, 999999L, Long.MAX_VALUE })
    @DisplayName("setId and getId - Valid ID values")
    void testIdPropertyWithValidValues(Long id) {
        role.setId(id);
        assertEquals(id, role.getId());
    }

    @DisplayName("setId - Null ID is allowed")
    @Test
    void testIdPropertyWithNullValue() {
        role.setId(null);
        assertNull(role.getId());
    }

    // ==================== Name (RoleType Enum) Property Tests ====================

    @ParameterizedTest
    @EnumSource(RoleType.class)
    @DisplayName("setName and getName - All RoleType enum values")
    void testNamePropertyWithAllEnumValues(RoleType roleType) {
        role.setName(roleType);
        assertEquals(roleType, role.getName());
    }

    @DisplayName("setName - USER role type")
    @Test
    void testNamePropertyWithUserRole() {
        role.setName(RoleType.USER);
        assertEquals(RoleType.USER, role.getName());
    }

    @DisplayName("setName - ADMIN role type")
    @Test
    void testNamePropertyWithAdminRole() {
        role.setName(RoleType.ADMIN);
        assertEquals(RoleType.ADMIN, role.getName());
    }

    @DisplayName("setName - MODERATOR role type")
    @Test
    void testNamePropertyWithModeratorRole() {
        role.setName(RoleType.MODERATOR);
        assertEquals(RoleType.MODERATOR, role.getName());
    }

    @DisplayName("setName - Null role type is allowed")
    @Test
    void testNamePropertyWithNullValue() {
        role.setName(null);
        assertNull(role.getName());
    }

    // ==================== Description Property Tests ====================

    @ParameterizedTest
    @CsvSource({
            "Standard user role",
            "Administrator with full access",
            "Content moderator with editing permissions",
            "Role with special characters: !@#$%^&*()",
            "Role with numbers: 123456789",
            "Very long description that contains multiple words and sentences describing the purpose and permissions of this particular role"
    })
    @DisplayName("setDescription and getDescription - Valid descriptions")
    void testDescriptionPropertyWithValidValues(String description) {
        role.setDescription(description);
        assertEquals(description, role.getDescription());
    }

    @DisplayName("setDescription - Null description is allowed")
    @Test
    void testDescriptionPropertyWithNullValue() {
        role.setDescription(null);
        assertNull(role.getDescription());
    }

    @DisplayName("setDescription - Empty string is allowed")
    @Test
    void testDescriptionPropertyWithEmptyString() {
        role.setDescription("");
        assertEquals("", role.getDescription());
    }

    @DisplayName("setDescription - Single character string")
    @Test
    void testDescriptionPropertyWithSingleCharacter() {
        role.setDescription("A");
        assertEquals("A", role.getDescription());
    }

    @DisplayName("setDescription - Maximum length string (255 characters)")
    @Test
    void testDescriptionPropertyWithMaxLength() {
        String maxLengthDescription = "a".repeat(255);
        role.setDescription(maxLengthDescription);
        assertEquals(maxLengthDescription, role.getDescription());
    }

    @DisplayName("setDescription - String with whitespace and special characters")
    @Test
    void testDescriptionPropertyWithWhitespaceAndSpecialCharacters() {
        String description = "   Description with   multiple spaces   and\ttabs\nand newlines   ";
        role.setDescription(description);
        assertEquals(description, role.getDescription());
    }

    // ==================== CreatedAt Property Tests ====================

    @ParameterizedTest
    @MethodSource("provideCreationDates")
    @DisplayName("setCreatedAt and getCreatedAt - Valid creation dates")
    void testCreatedAtPropertyWithValidDates(LocalDateTime creationDate) {
        role.setCreatedAt(creationDate);
        assertEquals(creationDate, role.getCreatedAt());
    }

    @DisplayName("setCreatedAt - Null creation date is allowed")
    @Test
    void testCreatedAtPropertyWithNullValue() {
        role.setCreatedAt(null);
        assertNull(role.getCreatedAt());
    }

    @DisplayName("setCreatedAt - Current time")
    @Test
    void testCreatedAtPropertyWithCurrentTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        role.setCreatedAt(currentTime);
        assertEquals(currentTime, role.getCreatedAt());
    }

    @DisplayName("setCreatedAt - Past date")
    @Test
    void testCreatedAtPropertyWithPastDate() {
        LocalDateTime pastDate = LocalDateTime.now().minusYears(1);
        role.setCreatedAt(pastDate);
        assertEquals(pastDate, role.getCreatedAt());
    }

    // ==================== Complex Scenario Tests ====================

    @DisplayName("Builder - Create multiple roles with different types")
    @Test
    void testCreateMultipleRolesWithDifferentTypes() {
        Role userRole = Role.builder()
                .id(1L)
                .name(RoleType.USER)
                .description("Basic user")
                .createdAt(now)
                .build();

        Role adminRole = Role.builder()
                .id(2L)
                .name(RoleType.ADMIN)
                .description("Full admin access")
                .createdAt(now)
                .build();

        Role modRole = Role.builder()
                .id(3L)
                .name(RoleType.MODERATOR)
                .description("Moderator access")
                .createdAt(now)
                .build();

        assertEquals(RoleType.USER, userRole.getName());
        assertEquals(RoleType.ADMIN, adminRole.getName());
        assertEquals(RoleType.MODERATOR, modRole.getName());
    }

    @DisplayName("Properties - All properties maintain their values after setting")
    @Test
    void testAllPropertiesRetainValuesAfterSetting() {
        Long id = 5L;
        RoleType name = RoleType.MODERATOR;
        String description = "Test moderator role";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(7);

        role.setId(id);
        role.setName(name);
        role.setDescription(description);
        role.setCreatedAt(createdAt);

        assertEquals(id, role.getId());
        assertEquals(name, role.getName());
        assertEquals(description, role.getDescription());
        assertEquals(createdAt, role.getCreatedAt());
    }

    @DisplayName("Builder - Partial builder with only required fields")
    @Test
    void testBuilderWithPartialFields() {
        Role partialRole = Role.builder()
                .id(1L)
                .name(RoleType.USER)
                .build();

        assertEquals(1L, partialRole.getId());
        assertEquals(RoleType.USER, partialRole.getName());
        assertNull(partialRole.getDescription());
        assertNull(partialRole.getCreatedAt());
    }

    @DisplayName("Builder - Changing role type after initial creation")
    @Test
    void testChangingRoleTypeAfterCreation() {
        role.setName(RoleType.USER);
        assertEquals(RoleType.USER, role.getName());

        role.setName(RoleType.ADMIN);
        assertEquals(RoleType.ADMIN, role.getName());

        role.setName(RoleType.MODERATOR);
        assertEquals(RoleType.MODERATOR, role.getName());
    }

    // ==================== Property Method Sources ====================

    private static Stream<LocalDateTime> provideCreationDates() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                now,
                now.minusMinutes(5),
                now.minusHours(1),
                now.minusDays(1),
                now.minusMonths(1),
                now.minusYears(1),
                LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59, 59)
        );
    }
}
