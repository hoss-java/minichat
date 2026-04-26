package com.minichat.security;

import com.minichat.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for JwtAuthenticationFilter
 * 
 * This test suite focuses exclusively on the internal logic of JwtAuthenticationFilter.
 * All external dependencies (JwtTokenProvider, CustomUserDetailsService, FilterChain, etc.)
 * are mocked to isolate the filter's behavior.
 * 
 * Test Organization:
 * - Public Endpoints: Tests for bypassing authentication on public paths
 * - Token Extraction: Tests for Bearer token extraction logic
 * - Happy Path: Successful authentication with valid token
 * - Error Handling: Invalid/missing tokens and exception scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterUnitTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        // Setup response writer with lenient() - not all tests use it
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
        
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    // ========== PUBLIC ENDPOINTS TESTS ==========
    @Nested
    @DisplayName("Public Endpoint Tests")
    class PublicEndpointTests {

        @ParameterizedTest
        @DisplayName("Should bypass authentication for public endpoints")
        @ValueSource(strings = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/forgot-password"
        })
        void testPublicEndpointsBypassesAuthentication(String publicEndpoint) throws ServletException, IOException {
            // Scenario: Request to public endpoint without token
            when(request.getRequestURI()).thenReturn(publicEndpoint);

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Filter chain continues without requiring authentication
            verify(filterChain).doFilter(request, response);
            verify(jwtTokenProvider, never()).validateToken(anyString());
            verify(userDetailsService, never()).loadUserByUsername(anyString());
        }

        @Test
        @DisplayName("Should bypass authentication for endpoints starting with public path")
        void testPublicEndpointWithPathVariables() throws ServletException, IOException {
            // Scenario: Request to /api/auth/login with query parameters
            when(request.getRequestURI()).thenReturn("/api/auth/login?email=test@test.com");

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Public endpoint matching works with full URI path
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // ========== TOKEN EXTRACTION TESTS ==========
    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests {

        @Test
        @DisplayName("Should extract token from Authorization header with Bearer prefix")
        void testExtractValidBearerToken() throws ServletException, IOException {
            // Scenario: Valid Bearer token in Authorization header
            String token = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.extractUsername(token)).thenReturn("testuser");
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Token is extracted and validated
            verify(jwtTokenProvider).validateToken(token);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle Bearer token with extra spaces")
        void testBearerTokenWithExtraSpaces() throws ServletException, IOException {
            // Scenario: Bearer token with trailing spaces
            String token = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer   " + token + "  ");
            when(jwtTokenProvider.validateToken(token.trim())).thenReturn(true);
            when(jwtTokenProvider.extractUsername(token.trim())).thenReturn("testuser");
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Token is trimmed and processed correctly
            verify(filterChain).doFilter(request, response);
        }

        @ParameterizedTest
        @DisplayName("Should not extract token for invalid header formats")
        @ValueSource(strings = {
            "NotBearer valid.jwt.token",
            "Bearer",
            "bearer valid.jwt.token",
            "valid.jwt.token"
        })
        void testInvalidBearerTokenFormat(String invalidHeader) throws ServletException, IOException {
            // Scenario: Authorization header without proper Bearer format
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn(invalidHeader);

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: No token extracted, unauthorized error sent
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle missing Authorization header")
        void testMissingAuthorizationHeader() throws ServletException, IOException {
            // Scenario: Request without Authorization header
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn(null);

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Unauthorized error sent
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).setContentType("application/json");
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle empty Authorization header")
        void testEmptyAuthorizationHeader() throws ServletException, IOException {
            // Scenario: Request with empty Authorization header
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("");

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Unauthorized error sent
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    // ========== HAPPY PATH TESTS ==========
    @Nested
    @DisplayName("Happy Path - Successful Authentication Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Should authenticate user with valid JWT token")
        void testSuccessfulAuthenticationWithValidToken() throws ServletException, IOException {
            // Scenario: Valid token provided, user exists, authentication succeeds
            String token = "valid.jwt.token";
            String username = "testuser";
            
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.extractUsername(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: User is authenticated and filter chain continues
            verify(jwtTokenProvider).validateToken(token);
            verify(jwtTokenProvider).extractUsername(token);
            verify(userDetailsService).loadUserByUsername(username);
            verify(filterChain).doFilter(request, response);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
                    "Security context should contain authentication after successful token validation");
        }

        @Test
        @DisplayName("Should set user details in security context")
        void testSecurityContextSetWithUserDetails() throws ServletException, IOException {
            // Scenario: Valid token should set authentication with user details
            String token = "valid.jwt.token";
            String username = "testuser";
            
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.extractUsername(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
            when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Authentication principal is the user details object
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertEquals(userDetails, authentication.getPrincipal(),
                    "Authentication principal should be the UserDetails object");
            assertTrue(authentication.isAuthenticated(),
                    "Authentication should be marked as authenticated");
        }
    }

    // ========== ERROR HANDLING TESTS ==========
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should reject invalid or expired token")
        void testInvalidOrExpiredTokenRejection() throws ServletException, IOException {
            // Scenario: Token is invalid or expired
            String token = "expired.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenReturn(false);

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Unauthorized error response sent
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).setContentType("application/json");
            verify(filterChain, never()).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication(),
                    "Security context should not contain authentication for invalid token");
        }

        @Test
        @DisplayName("Should send proper error message for invalid token")
        void testInvalidTokenErrorMessage() throws ServletException, IOException {
            // Scenario: Invalid token should return proper error message
            String token = "invalid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenReturn(false);

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Response contains "Invalid or expired token" message
            String errorResponse = responseWriter.toString();
            assertTrue(errorResponse.contains("Invalid or expired token"),
                    "Error response should contain 'Invalid or expired token' message");
            assertTrue(errorResponse.contains("Unauthorized"),
                    "Error response should contain 'Unauthorized' error");
        }

        @Test
        @DisplayName("Should send proper error message for missing token")
        void testMissingTokenErrorMessage() throws ServletException, IOException {
            // Scenario: No token provided in request
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn(null);

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Response contains "Missing authorization token" message
            String errorResponse = responseWriter.toString();
            assertTrue(errorResponse.contains("Missing authorization token"),
                    "Error response should contain 'Missing authorization token' message");
        }

        @Test
        @DisplayName("Should handle exception during token validation")
        void testExceptionDuringTokenValidation() throws ServletException, IOException {
            // Scenario: Exception thrown during token validation
            String token = "problematic.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token processing error"));

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Catch exception and send unauthorized response
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle exception during username extraction")
        void testExceptionDuringUsernameExtraction() throws ServletException, IOException {
            // Scenario: Exception during username extraction
            String token = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.extractUsername(token)).thenThrow(new RuntimeException("Username extraction failed"));

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Catch exception and send unauthorized response
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String errorResponse = responseWriter.toString();
            assertTrue(errorResponse.contains("Authentication failed"),
                    "Error response should indicate authentication failure");
        }

        @Test
        @DisplayName("Should handle exception during user details loading")
        void testExceptionDuringUserDetailsLoading() throws ServletException, IOException {
            // Scenario: Exception when loading user details
            String token = "valid.jwt.token";
            String username = "nonexistent";
            
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.extractUsername(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenThrow(new RuntimeException("User not found"));

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Catch exception and send unauthorized response
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should catch generic exceptions and send error response")
        void testGenericExceptionHandling() throws ServletException, IOException {
            // Scenario: Unexpected exception during filter processing
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenThrow(new RuntimeException("Unexpected error"));

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Exception caught, unauthorized response sent
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should include exception message in error response")
        void testExceptionMessageInErrorResponse() throws ServletException, IOException {
            // Scenario: Exception occurs and message should be included in response
            String token = "problematic.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/messages");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("Signature verification failed"));

            // Action
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Expected: Error response contains exception message
            String errorResponse = responseWriter.toString();
            assertTrue(errorResponse.contains("Authentication failed"),
                    "Error response should contain 'Authentication failed' message");
            verify(response).setContentType("application/json");
        }
    }
}
