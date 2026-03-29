package com.blinders.blinders.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set the secret key (base64 encoded, at least 32 bytes for HS256)
        ReflectionTestUtils.setField(jwtService, "secretKey", "bXlUZXN0U2VjcmV0S2V5MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMg==");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String email = "test@example.com";
        String role = "USER";

        String token = jwtService.generateToken(email, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email, "USER");

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        String email = "test@example.com";
        String role = "ADMIN";
        String token = jwtService.generateToken(email, role);

        String extractedRole = jwtService.extractRole(token);

        assertEquals(role, extractedRole);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email, "USER");

        boolean isValid = jwtService.isTokenValid(token, email);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithWrongEmail_ShouldReturnFalse() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email, "USER");

        boolean isValid = jwtService.isTokenValid(token, "wrong@example.com");

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldThrowException() {
        JwtService expiredJwtService = new JwtService();
        ReflectionTestUtils.setField(expiredJwtService, "secretKey",
                "bXlUZXN0U2VjcmV0S2V5MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMg==");
        ReflectionTestUtils.setField(expiredJwtService, "jwtExpiration", -1000L); // Already expired

        String email = "test@example.com";
        String token = expiredJwtService.generateToken(email, "USER");

        // Expired tokens throw an exception when validation is attempted
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(token, email));
    }
}
