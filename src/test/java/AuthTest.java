import com.ecommerce.users.security.JwtTokenUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private UserDetails userDetails;
    private final String SECRET = "7A7y3vV6x7h4w9B0dQjWkZqLpOaRsXcYfUiHmNtKbPeYgGzMlDnJrFsCeAm="; // 256-bit key
    private final long ACCESS_EXPIRATION = 900; // 15 minutes
    private final long REFRESH_EXPIRATION = 604800; // 7 days

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        jwtTokenUtil.secret = SECRET;
        jwtTokenUtil.accessExpiration = ACCESS_EXPIRATION;
        jwtTokenUtil.refreshExpiration = REFRESH_EXPIRATION;

        userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void generateAccessToken_ValidUser_ReturnsValidToken() {
        String token = jwtTokenUtil.generateAccessToken (userDetails);

        assertNotNull(token);
        assertEquals("testuser", jwtTokenUtil.getUsernameFromToken(token));
        assertFalse(jwtTokenUtil.isTokenExpired(token));
    }

    @Test
    void generateRefreshToken_ValidUser_ReturnsLongLivedToken() {
        String token = jwtTokenUtil.generateRefreshToken(userDetails);

        Date expiration = jwtTokenUtil.getExpirationDateFromToken(token);
        long diff = expiration.getTime() - System.currentTimeMillis();
        assertTrue(diff > TimeUnit.DAYS.toMillis(6)); // ~7 days
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenUtil.generateAccessToken(userDetails);
        assertTrue(jwtTokenUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Force expiration
        jwtTokenUtil.accessExpiration = -1;
        String token = jwtTokenUtil.generateAccessToken(userDetails);

        assertTrue(jwtTokenUtil.isTokenExpired(token));
        assertFalse(jwtTokenUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_InvalidUser_ReturnsFalse() {
        String token = jwtTokenUtil.generateAccessToken(userDetails);
        UserDetails otherUser = User.withUsername("otheruser")
                .password("pass")
                .authorities("ROLE_USER")
                .build();

        assertFalse(jwtTokenUtil.validateToken(token, otherUser));
    }

    @Test
    void refreshAccessToken_ValidRefreshToken_ReturnsNewAccessToken() {
        String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
        String newAccessToken = jwtTokenUtil.refreshAccessToken(refreshToken, userDetails);

        assertNotNull(newAccessToken);
        assertEquals("testuser", jwtTokenUtil.getUsernameFromToken(newAccessToken));
    }

    @Test
    void refreshAccessToken_ExpiredRefreshToken_ThrowsException() {
        jwtTokenUtil.refreshExpiration = -1;
        String expiredRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        assertThrows(SecurityException.class, () ->
                jwtTokenUtil.refreshAccessToken(expiredRefreshToken, userDetails)
        );
    }

    @Test
    void refreshAccessToken_InvalidUser_ThrowsException() {
        String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
        UserDetails otherUser = User.withUsername("otheruser")
                .password("pass")
                .authorities("ROLE_USER")
                .build();

        assertThrows(SecurityException.class, () ->
                jwtTokenUtil.refreshAccessToken(refreshToken, otherUser)
        );
    }

    @Test
    void getUsernameFromToken_InvalidToken_ThrowsException() {
        String invalidToken = "invalid.token.string";
        assertThrows(JwtException.class, () ->
                jwtTokenUtil.getUsernameFromToken(invalidToken)
        );
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        String token = jwtTokenUtil.generateAccessToken(userDetails);
        assertFalse(jwtTokenUtil.isTokenExpired(token));
    }

    @Test
    void parseToken_InvalidSignature_ThrowsException() {
        String validToken = jwtTokenUtil.generateAccessToken(userDetails);

        // Create a different signing key
        SecretKey invalidKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("7A7y3vV6x7h4w9B0dQjWkZqLpOaRsXcYfUiHmNtKbPeYgGzMlDnaaFsseAs="));

        assertThrows(SignatureException.class, () ->
                Jwts.parser()
                        .verifyWith(invalidKey)
                        .build()
                        .parseSignedClaims(validToken)
        );

    }
}