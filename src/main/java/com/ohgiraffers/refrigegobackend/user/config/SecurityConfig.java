package com.ohgiraffers.refrigegobackend.user.config;

import com.ohgiraffers.refrigegobackend.user.jwt.CustomLogoutFilter;
import com.ohgiraffers.refrigegobackend.user.jwt.JWTFilter;
import com.ohgiraffers.refrigegobackend.user.jwt.JWTUtil;
import com.ohgiraffers.refrigegobackend.user.jwt.LoginFilter;
import com.ohgiraffers.refrigegobackend.user.repository.RefreshRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final AuthenticationConfiguration authenticationConfiguration;
        private final JWTUtil jwtUtil;
        private final RefreshRepository refreshRepository;

        public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil,
                              RefreshRepository refreshRepository) {
                this.authenticationConfiguration = authenticationConfiguration;
                this.jwtUtil = jwtUtil;
                this.refreshRepository = refreshRepository;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        @Bean
        public BCryptPasswordEncoder bCryptPasswordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://refrige.shop", "https://www.refrige.shop"));
                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
                config.setExposedHeaders(Arrays.asList("access", "Authorization"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .csrf(csrf -> csrf.disable())
                        .formLogin(form -> form.disable())
                        .httpBasic(basic -> basic.disable())
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/login", "/join", "/reissue", "/", "/api/recipe/**").permitAll()
                                .requestMatchers("/api/bookmark/**").hasAnyAuthority("ROLE_USER")
                                .requestMatchers("/admin").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated()
                        )
                        .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
                        .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class)
                        .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class)
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                return http.build();
        }
}
