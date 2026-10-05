package com.example.practica.service;

import com.example.practica.dto.LoginRequest;
import com.example.practica.dto.LoginResponse;
import com.example.practica.dto.UsuarioLoginDTO;

import com.example.practica.model.Email;
import com.example.practica.model.Usuario;

import com.example.practica.repository.EmailRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
        implements AuthService {

    private final EmailRepository emailRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @Override
    public LoginResponse login(
            LoginRequest request
    ) {

        String correo =
                request.getUsuario()
                        .trim()
                        .toLowerCase();


        // =====================================================
        // SOLO SE PUEDE INICIAR SESIÓN CON EL CORREO PRINCIPAL
        // =====================================================

        Email correoPrincipal =
                emailRepository
                        .findByValorIgnoreCaseAndTipoIgnoreCase(
                                correo,
                                "PRINCIPAL"
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Credenciales incorrectas"
                                )
                        );


        Usuario usuario =
                correoPrincipal.getUsuario();


        // =====================================================
        // USUARIO DADO DE BAJA
        // =====================================================

        if (usuario.getFechaBaja() != null) {

            throw new RuntimeException(
                    "Usuario inactivo"
            );
        }


        // =====================================================
        // CONTRASEÑA
        // =====================================================

        if (
                !passwordEncoder.matches(
                        request.getPassword(),
                        usuario.getPassword()
                )
        ) {

            throw new RuntimeException(
                    "Credenciales incorrectas"
            );
        }


        // =====================================================
        // ROL
        // =====================================================

        if (usuario.getRol() == null) {

            throw new RuntimeException(
                    "El usuario no tiene un rol asignado"
            );
        }


        // =====================================================
        // GENERAR JWT
        // =====================================================

        String token =
                jwtService.generarToken(
                        usuario,
                        correoPrincipal.getValor()
                );


        // =====================================================
        // DATOS DE SESIÓN
        // =====================================================

        UsuarioLoginDTO usuarioLogin =
                UsuarioLoginDTO.builder()
                        .id(usuario.getId())
                        .email(
                                correoPrincipal.getValor()
                        )
                        .rol(
                                usuario
                                        .getRol()
                                        .getNombre()
                        )
                        .nombre(usuario.getNombre())
                        .primerApellido(usuario.getPrimerApellido())
                        .build();


        return LoginResponse.builder()
                .acceso(true)
                .token(token)
                .usuario(usuarioLogin)
                .build();
    }
}