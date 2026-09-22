package com.example.practica.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // CORS
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        // Frontend Angular
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200"
                )
        );


        // Métodos permitidos
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );


        // Headers permitidos
        configuration.setAllowedHeaders(
                List.of(
                        "Content-Type",
                        "Authorization",
                        "X-XSRF-TOKEN"
                )
        );


        // Permite enviar cookies
        configuration.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        // =====================================================
        // CONFIGURACIÓN CSRF PARA ANGULAR
        // =====================================================

        CookieCsrfTokenRepository csrfTokenRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();


        /*
         * Spring utilizará:
         *
         * Cookie:
         * XSRF-TOKEN
         *
         * Header:
         * X-XSRF-TOKEN
         *
         * Angular puede trabajar con estos nombres.
         */


        CsrfTokenRequestAttributeHandler requestHandler =
                new CsrfTokenRequestAttributeHandler();

        requestHandler.setCsrfRequestAttributeName(null);


        http

                // =================================================
                // CSRF
                // =================================================

                .csrf(
                        csrf -> csrf

                                .csrfTokenRepository(
                                        csrfTokenRepository
                                )

                                .csrfTokenRequestHandler(
                                        requestHandler
                                )

                                /*
                                 * Login y registro deben poder
                                 * realizarse antes de que exista
                                 * una sesión autenticada.
                                 *
                                 * Por ahora excluimos estos endpoints.
                                 */
                                .ignoringRequestMatchers(
                                        "/api/auth/login",
                                        "/api/usuarios"
                                )
                )


                // =================================================
                // CORS
                // =================================================

                .cors(
                        cors -> cors.configurationSource(
                                corsConfigurationSource()
                        )
                )


                // =================================================
                // SIN SESIÓN HTTP DE SERVIDOR
                // =================================================

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )


                // =================================================
                // AUTORIZACIÓN
                // =================================================

                .authorizeHttpRequests(
                        auth -> auth


                                // ---------------------------------
                                // LOGIN Y LOGOUT
                                // ---------------------------------

                                .requestMatchers(
                                        "/api/auth/**"
                                )
                                .permitAll()


                                // ---------------------------------
                                // REGISTRO PÚBLICO
                                // ---------------------------------

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/usuarios"
                                )
                                .permitAll()


                                // ---------------------------------
                                // POSTALIA
                                // ---------------------------------

                                .requestMatchers(
                                        "/api/postalia/**"
                                )
                                .permitAll()


                                // ---------------------------------
                                // PERFIL
                                // ---------------------------------

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/usuarios/me"
                                )
                                .authenticated()


                                // ---------------------------------
                                // ADMINISTRACIÓN
                                // ---------------------------------

                                .requestMatchers(
                                        "/api/usuarios/**"
                                )
                                .hasRole("ADMIN")


                                // ---------------------------------
                                // RESTO
                                // ---------------------------------

                                .anyRequest()
                                .authenticated()
                )


                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}