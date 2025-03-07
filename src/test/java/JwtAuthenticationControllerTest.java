import com.ecommerce.users.config.JwtAuthenticationEntryPoint;
import com.ecommerce.users.config.WebSecurityConfig;
import com.ecommerce.users.controllers.JwtAuthenticationController;
import com.ecommerce.users.model.JwtRefreshRequest;
import com.ecommerce.users.model.JwtRequest;
import com.ecommerce.users.model.JwtResponse;
import com.ecommerce.users.security.JwtTokenUtil;
import com.ecommerce.users.service.JwtUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(JwtAuthenticationController.class)
@Import(WebSecurityConfig.class)
@ContextConfiguration(classes = JwtAuthenticationController.class) // Explicit controller configuration
public class JwtAuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private JwtUserDetailsService userDetailsService;

    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    // Teste de login com credenciais válidas
    @Test
    public void testLogin_ValidCredentials_ReturnsJwtResponse() throws Exception {
        String accessToken = "7A7y3vV6x7h4w9B0dQjWkZqLpOaRsXcYfUiHmNtKbPeYgGzMlDnJrFsCeAm=";
        String refreshToken = "refresh-token";

        // Simula autenticação bem-sucedida (nenhuma exceção é lançada)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "password", userDetails.getAuthorities()));

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtTokenUtil.generateAccessToken(userDetails)).thenReturn(accessToken);
        when(jwtTokenUtil.generateRefreshToken(userDetails)).thenReturn(refreshToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));
    }

    // Teste de login com usuário desativado
    @Test
    public void testLogin_DisabledUser_ReturnsUnauthorized() throws Exception {
        // Simula que a autenticação lança DisabledException
        doThrow(new DisabledException("Usuário desativado"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Usuário desativado"));
    }

    // Teste de login com credenciais inválidas
    @Test
    public void testLogin_BadCredentials_ReturnsUnauthorized() throws Exception {
        doThrow(new BadCredentialsException("Credenciais inválidas"))
                .when(authenticationManager).authenticate(any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"wrongpassword\"}")
                        .with(csrf())) // Add CSRF token
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciais inválidas"));
    }

    // Teste de login com erro genérico (Erro interno)
    @Test
    public void testLogin_InternalError_ReturnsInternalServerError() throws Exception {
        // Simula que ocorre uma exceção genérica durante a autenticação
        doThrow(new RuntimeException("Erro inesperado"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro interno"));
    }

    // Teste do endpoint de refresh com refresh token válido
    @Test
    public void testRefreshToken_ValidRefreshToken_ReturnsNewAccessToken() throws Exception {
        String validRefreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";

        // Simula a extração do username a partir do refresh token
        when(jwtTokenUtil.getUsernameFromToken(validRefreshToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(validRefreshToken, userDetails)).thenReturn(true);
        when(jwtTokenUtil.generateAccessToken(userDetails)).thenReturn(newAccessToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"valid-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(validRefreshToken));
    }

    // Teste do endpoint de refresh com refresh token expirado
    @Test
    public void testRefreshToken_ExpiredRefreshToken_ReturnsUnauthorized() throws Exception {
        String expiredRefreshToken = "expired-refresh-token";

        // Simula que a extração do username lança ExpiredJwtException
        when(jwtTokenUtil.getUsernameFromToken(expiredRefreshToken))
                .thenThrow(new ExpiredJwtException(null, null, "Token expirado"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"expired-refresh-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Refresh token expirado"));
    }

    // Teste do endpoint de refresh com refresh token inválido
    @Test
    public void testRefreshToken_InvalidRefreshToken_ReturnsUnauthorized() throws Exception {
        String invalidRefreshToken = "invalid-refresh-token";

        when(jwtTokenUtil.getUsernameFromToken(invalidRefreshToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(invalidRefreshToken, userDetails)).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid-refresh-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Refresh token inválido"));
    }

    // Teste do endpoint de refresh para erro genérico durante a renovação
    @Test
    public void testRefreshToken_GenericException_ReturnsBadRequest() throws Exception {
        String refreshToken = "any-refresh-token";

        when(jwtTokenUtil.getUsernameFromToken(refreshToken))
                .thenThrow(new RuntimeException("Erro ao extrair username"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"any-refresh-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro ao renovar token"));
    }
}
