package com.api.rest_code.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import com.ecommerce.users.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    // Uma chave secreta válida em Base64 com pelo menos 256 bits.
    // Você pode gerar uma chave com:
    // SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
    private static final String SECRET_BASE64 = "TlB0qHrl+0d26Z9RkBjaPdZ+V8Cy0jivZBc0zM9Tqcc=";

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil ();
        // Injeta a chave secreta manualmente para os testes
        jwtTokenUtil.setSecret(SECRET_BASE64);
    }

    @Test
    void testGenerateToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtTokenUtil.generateToken(userDetails);

        assertNotNull(token, "O token não deve ser nulo");
        assertFalse(token.isEmpty(), "O token não deve ser vazio");
    }

    @Test
    void testValidateToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtTokenUtil.generateToken(userDetails);

        assertTrue(jwtTokenUtil.validateToken(token, userDetails),
                "O token deve ser válido para o usuário informado");
    }

    @Test
    void testGetUsernameFromToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtTokenUtil.generateToken(userDetails);

        String username = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals("testuser", username, "O username extraído do token deve ser 'testuser'");
    }

}
