package com.ecommerce.users.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.function.Function;

import static io.jsonwebtoken.Jwts.header;

@Component
public class JwtTokenUtil implements Serializable {
    @Serial
    private static final long serialVersionUID = -2550185165626007488L;

    @Value("${jwt.secret}")
    public String secret;

    @Value("${jwt.access.expiration}")
    public long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    public long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extrai username do token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Metodo genérico para extrair claims
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Recupera a data de expiração do token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    // Verifica expiração
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }


    // Gera access token
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), accessExpiration);
    }

    // Gera refresh token
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), refreshExpiration);
    }

        private String buildToken(String subject, long expiration) {
            return Jwts.builder()
                    .header()
                    .and ()
                    .subject(subject)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                    .signWith(getSigningKey())
                    .compact();
        }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            // Token expirado: retorna false sem propagar a exceção
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken, UserDetails userDetails) {
        try {
            if (isTokenExpired(refreshToken)) {
                throw new ExpiredJwtException(null, null, "Refresh token expirado");
            }
            if (!getUsernameFromToken(refreshToken).equals(userDetails.getUsername())) {
                throw new JwtException("Usuário inválido para o refresh token");
            }
            return generateAccessToken(userDetails);
        } catch (ExpiredJwtException ex) {
            throw new SecurityException("Refresh token expirado", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new SecurityException("Refresh token inválido", ex);
        }
    }
}
