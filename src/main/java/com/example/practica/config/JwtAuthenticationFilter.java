package com.example.practica.config;

import com.example.practica.model.Email;
import com.example.practica.model.Usuario;
import com.example.practica.repository.EmailRepository;
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
    private final EmailRepository emailRepository;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // =====================================================
        // 1. BUSCAR JWT EN COOKIE
        // =====================================================

        String token =
                obtenerTokenDeCookie(request);


        // =====================================================
        // 2. SI NO HAY JWT, CONTINUAR
        // =====================================================

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

            if (!jwtService.esTokenValido(token)) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 4. EXTRAER CORREO PRINCIPAL
            // =================================================

            String emailPrincipal =
                    jwtService.extraerEmail(token);


            // =================================================
            // 5. BUSCAR ESE CORREO COMO PRINCIPAL
            // =================================================

            Email correoPrincipal =
                    emailRepository
                            .findByValorIgnoreCaseAndTipoIgnoreCase(
                                    emailPrincipal,
                                    "PRINCIPAL"
                            )
                            .orElse(null);


            if (correoPrincipal == null) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            Usuario usuario =
                    correoPrincipal.getUsuario();


            // =================================================
            // 6. COMPROBAR BAJA LÓGICA
            // =================================================

            if (usuario.getFechaBaja() != null) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 7. COMPROBAR ROL ACTUAL EN BD
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

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + rol
                    );


            // =================================================
            // 9. CREAR AUTENTICACIÓN
            // =================================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            correoPrincipal.getValor(),
                            null,
                            List.of(authority)
                    );


            // =================================================
            // 10. GUARDAR EN SECURITY CONTEXT
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );


        } catch (Exception exception) {

            SecurityContextHolder.clearContext();
        }


        // =====================================================
        // 11. CONTINUAR
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

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}