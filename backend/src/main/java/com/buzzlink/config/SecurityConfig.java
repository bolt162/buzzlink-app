package com.buzzlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration
 * For this demo, we use a simple setup with CORS enabled
 * In production, would integrate Clerk JWT validation
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.web.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Disabled for demo; in production, use proper CSRF protection
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll() // Allow WebSocket connections
                        .requestMatchers("/info**").permitAll() // Allow SockJS info endpoint with query params
                        .requestMatchers("/sockjs/**").permitAll() // Allow all SockJS endpoints
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 console for dev
                        .requestMatchers("/actuator/**").permitAll() // Allow actuator endpoints
                        .requestMatchers("/api/**").permitAll() // For demo, allow all API access
                        .requestMatchers("/dashboard.html").permitAll() // Allow dashboard access
                        .requestMatchers("/static/**").permitAll() // Allow static resources
                        .requestMatchers("/*.html").permitAll() // Allow HTML files
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // For H2 console

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
