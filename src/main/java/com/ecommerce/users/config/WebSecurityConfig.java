    package com.ecommerce.users.config;

    import com.ecommerce.users.filter.JwtRequestFilter;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity(prePostEnabled = true)
    public class WebSecurityConfig {

        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        // Removido UserDetailsService não utilizado
        public WebSecurityConfig(
                JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
            this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;

        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(); // Bean necessário para criptografia de senhas
        }

        @Bean
        public AuthenticationManager authenticationManager(
                AuthenticationConfiguration authenticationConfiguration) throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            // Lista de endpoints públicos
            final String[] PUBLIC_ENDPOINTS = {
                    "/api/auth/**",
                    "/v2/api-docs",
                    "/swagger-ui/**",
                    "/swagger-resources/**"
            };

            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                            .anyRequest().authenticated()
                    )
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint) // Tratamento de erros de autenticação
                    )
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // API stateless
                    );
            return http.build();
        }
    }