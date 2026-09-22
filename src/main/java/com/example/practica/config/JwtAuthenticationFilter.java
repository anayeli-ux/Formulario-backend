package com.example.practica.config;

import com.example.practica.model.Usuario;
import com.example.practica.repository.UsuarioRepository;
import com.example.practica.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // =====================================================
        // 1. BUSCAR JWT EN LAS COOKIES
        // =====================================================

        String token = obtenerTokenDeCookie(request);


        // =====================================================
        // 2. SI NO HAY COOKIE JWT, CONTINUAR
        // =====================================================

        /*
         * No significa que automáticamente tenga acceso.
         *
         * SecurityConfig decidirá después si la ruta:
         *
         * - es pública
         * - requiere autenticación
         * - requiere ADMIN
         */

        if (token == null || token.isBlank()) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        try {

            // =================================================
            // 3. VALIDAR JWT
            // =================================================

            /*
             * JwtService comprueba:
             *
             * - firma
             * - estructura
             * - expiración
             */

            if (!jwtService.esTokenValido(token)) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 4. EXTRAER EMAIL DEL JWT
            // =================================================

            String email =
                    jwtService.extraerEmail(token);


            // =================================================
            // 5. BUSCAR USUARIO ACTUAL EN LA BD
            // =================================================

            Usuario usuario =
                    usuarioRepository
                            .findByEmail(email)
                            .orElse(null);


            if (usuario == null) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 6. COMPROBAR BAJA LÓGICA
            // =================================================

            /*
             * fechaBaja == null
             *      → usuario activo
             *
             * fechaBaja != null
             *      → usuario dado de baja
             */

            if (usuario.getFechaBaja() != null) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 7. COMPROBAR ROL
            // =================================================

            if (usuario.getRol() == null) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            String rol =
                    usuario
                            .getRol()
                            .getNombre();


            // =================================================
            // 8. CREAR AUTHORITY
            // =================================================

            /*
             * Si en BD tenemos:
             *
             * ADMIN
             *
             * Spring recibirá:
             *
             * ROLE_ADMIN
             *
             * Esto permite utilizar:
             *
             * .hasRole("ADMIN")
             */

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + rol
                    );


            // =================================================
            // 9. CREAR AUTENTICACIÓN
            // =================================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(authority)
                    );


            // =================================================
            // 10. GUARDAR AUTENTICACIÓN EN SPRING SECURITY
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );


        } catch (Exception exception) {

            /*
             * Puede entrar aquí si:
             *
             * - el JWT fue modificado
             * - la firma no coincide
             * - está vencido
             * - tiene una estructura incorrecta
             */

            SecurityContextHolder.clearContext();
        }


        // =====================================================
        // 11. CONTINUAR PETICIÓN
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );
    }


    // =========================================================
    // OBTENER JWT DESDE COOKIE
    // =========================================================

    private String obtenerTokenDeCookie(
            HttpServletRequest request
    ) {

        Cookie[] cookies =
                request.getCookies();


        // No llegaron cookies
        if (cookies == null) {
            return null;
        }


        // Buscar específicamente la cookie "jwt"
        for (Cookie cookie : cookies) {

            if ("jwt".equals(cookie.getName())) {

                return cookie.getValue();
            }
        }


        return null;
    }
}