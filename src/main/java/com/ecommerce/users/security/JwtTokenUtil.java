    package com.ecommerce.users.security;

    import java.io.Serial;
    import java.io.Serializable;
    import java.util.Date;
    import java.util.function.Function;

    import io.jsonwebtoken.io.Decoders;
    import io.jsonwebtoken.security.Keys;
    import lombok.Setter;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Component;
    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;

    import javax.crypto.SecretKey;

    @Setter
    @Component
    public class JwtTokenUtil implements Serializable {
        @Serial
        private static final long serialVersionUID = -2550185165626007488L;
        public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

        // Setter para a secret key (usado em testes)
        @Value("${jwt.secret}") // Fixed property injection
        private String secret;

        private SecretKey getSigningKey() {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        }

        // Retorna o username do token JWT
        public String getUsernameFromToken(String token) {
            return getClaimFromToken(token, Claims::getSubject);
        }

        // Retorna a data de expiração do token JWT
        public Date getExpirationDateFromToken(String token) {
            return getClaimFromToken(token, Claims::getExpiration);
        }

        public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
            final Claims claims = getAllClaimsFromToken(token);
            return claimsResolver.apply(claims);
        }

        // Para retornar qualquer informação do token, precisamos da secret key
        private Claims getAllClaimsFromToken(String token) {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }

        // Verifica se o token expirou
        private Boolean isTokenExpired(String token) {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        }

        // Gera token para o usuário
        public String generateToken(UserDetails userDetails) {
            return Jwts.builder()
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                    .signWith(getSigningKey())
                    .compact();
        }

        // Valida o token
        public Boolean validateToken(String token, UserDetails userDetails) {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }

    }