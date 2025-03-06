package com.ecommerce.users.controllers;

import com.ecommerce.users.model.JwtRefreshRequest;
import com.ecommerce.users.model.JwtRequest;
import com.ecommerce.users.model.JwtResponse;
import com.ecommerce.users.security.JwtTokenUtil;
import com.ecommerce.users.service.JwtUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    // Endpoint de Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest request) {
        try {
            authenticate(request.getUsername(), request.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            final String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário desativado");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno");
        }
    }

    // Endpoint de Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody JwtRefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtTokenUtil.validateToken(refreshToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token inválido");
            }

            String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
            return ResponseEntity.ok(new JwtResponse(newAccessToken, refreshToken));
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expirado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao renovar token");
        }
    }

    // Metodo de Autenticação
    private void authenticate(String username, String password) throws DisabledException, BadCredentialsException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }
}