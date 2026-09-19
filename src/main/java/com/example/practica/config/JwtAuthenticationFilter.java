package com.example.practica.config;

import com.example.practica.model.Usuario;
import com.example.practica.repository.UsuarioRepository;
import com.example.practica.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        // 1. Obtener encabezado Authorization
        // =====================================================

        String authHeader =
                request.getHeader("Authorization");


        // Si no existe token, continuamos.
        // SecurityConfig decidirá si la ruta es pública
        // o necesita autenticación.
        if (
                authHeader == null
                        || !authHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =====================================================
        // 2. Extraer JWT
        // =====================================================

        String token =
                authHeader.substring(7);


        try {

            // =================================================
            // 3. Validar firma y expiración
            // =================================================

            if (!jwtService.esTokenValido(token)) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 4. Extraer email
            // =================================================

            String email =
                    jwtService.extraerEmail(token);


            // =================================================
            // 5. Buscar usuario actual en BD
            // =================================================
            Usuario usuario =
                    usuarioRepository
                            .findByEmail(email)
                            .orElse(null);


            if (usuario == null) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // DEBUG TEMPORAL
            // =================================================

            System.out.println("=== JWT DEBUG ===");
            System.out.println("Email: " + usuario.getEmail());
            System.out.println("Activo: " + usuario.isActivo());
            System.out.println("TokenVersion BD: " + usuario.getTokenVersion());
            System.out.println("TokenVersion JWT: " + jwtService.extraerTokenVersion(token));
            System.out.println(
                    "Rol: " +
                            (usuario.getRol() != null
                                    ? usuario.getRol().getNombre()
                                    : "SIN ROL")
            );


            // =================================================
            // 6. Verificar que siga activo
            // =================================================

            if (!usuario.isActivo()) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 7. Verificar tokenVersion
            // =================================================

            Integer tokenVersion =
                    jwtService.extraerTokenVersion(
                            token
                    );


            if (
                    tokenVersion == null
                            || !tokenVersion.equals(
                            usuario.getTokenVersion()
                    )
            ) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 8. Verificar rol
            // =================================================

            if (usuario.getRol() == null) {

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


            // Spring Security utiliza ROLE_ADMIN
            // cuando usamos hasRole("ADMIN")
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + rol
                    );


            // =================================================
            // 9. Crear autenticación de Spring
            // =================================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(authority)
                    );


            // =================================================
            // 10. Registrar usuario como autenticado
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );


            // =================================================
            // DEBUG TEMPORAL
            // =================================================

            System.out.println("Authority creada: " + authority);
            System.out.println("Autenticado: " + authentication.isAuthenticated());
            System.out.println("=================");

        } catch (Exception exception) {

            /*
             * Token inválido, alterado, vencido, etc.
             *
             * No autenticamos al usuario.
             * SecurityConfig decidirá si puede
             * acceder a la ruta.
             */

            // ============================================
            // DEBUG TEMPORAL
            // ============================================

            System.out.println("=== ERROR JWT FILTER ===");
            exception.printStackTrace();
            System.out.println("========================");

            SecurityContextHolder.clearContext();
        }


        // =====================================================
        // 11. Continuar con la petición
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}