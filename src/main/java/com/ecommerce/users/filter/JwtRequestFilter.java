package com.ecommerce.users.filter;

import com.ecommerce.users.security.JwtTokenUtil;
import com.ecommerce.users.service.JwtUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtRequestFilter(JwtUserDetailsService jwtUserDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // Extrai o token do cabeçalho "Bearer"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (ExpiredJwtException e) {
                logger.warn("Token expirado: {}", e.getMessage());
                sendErrorResponse(response, "Token expirado");
                return;
            } catch (MalformedJwtException | SignatureException e) {
                logger.warn("Token inválido: {}", e.getMessage());
                sendErrorResponse(response, "Token inválido");
                return;
            } catch (Exception e) {
                logger.error("Erro ao processar JWT: {}", e.getMessage());
                sendErrorResponse(response, "Erro interno");
                return;
            }
        } else if (requestTokenHeader != null) {
            logger.warn("Formato do token incorreto: {}", requestTokenHeader);
            sendErrorResponse(response, "Formato do token incorreto");
            return;
        }

        // Configura a autenticação se o token for válido
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
            if (jwtToken != null && jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                // Extrai roles do token
                List<GrantedAuthority> authorities = (List<GrantedAuthority>) jwtTokenUtil.getClaimFromToken(jwtToken, claims -> claims)
                        .get("roles", List.class)
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(role.toString()))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // Garante que a requisição prossiga mesmo sem token
        chain.doFilter(request, response);
    }

    // Metodo auxiliar para enviar respostas de erro
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
