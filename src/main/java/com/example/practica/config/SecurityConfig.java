package com.example.practica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;


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
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // =================================================
                // CSRF
                // =================================================

                .csrf(
                        csrf ->
                                csrf.disable()
                )


                // =================================================
                // CORS
                // =================================================

                .cors(
                        cors -> {
                        }
                )


                // =================================================
                // SIN SESIONES DE SERVIDOR
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
                                // LOGIN
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
                                // PERFIL DEL USUARIO AUTENTICADO
                                // USER o ADMIN
                                // ---------------------------------

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/usuarios/me"
                                )
                                .authenticated()

                                // ---------------------------------
                                // ADMINISTRACIÓN DE USUARIOS
                                // ---------------------------------

                                .requestMatchers(
                                        "/api/usuarios/**"
                                )
                                .hasRole("ADMIN")


                                // ---------------------------------
                                // CUALQUIER OTRA RUTA
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