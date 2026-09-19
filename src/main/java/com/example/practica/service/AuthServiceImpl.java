package com.example.practica.service;

import com.example.practica.dto.LoginRequest;
import com.example.practica.dto.LoginResponse;
import com.example.practica.dto.UsuarioLoginDTO;
import com.example.practica.model.Usuario;
import com.example.practica.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(
            LoginRequest request
    ) {

        // ==========================================
        // 1. Obtener email
        // ==========================================

        String email =
                request.getUsuario()
                        .trim()
                        .toLowerCase();


        // ==========================================
        // 2. Buscar usuario por email
        // ==========================================

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Credenciales incorrectas"
                        )
                );


        // ==========================================
        // 3. Verificar baja lógica
        // ==========================================

        if (!usuario.isActivo()) {

            throw new RuntimeException(
                    "El usuario se encuentra inactivo"
            );
        }


        // ==========================================
        // 4. Comprobar contraseña
        // ==========================================

        boolean passwordCorrecto =
                passwordEncoder.matches(
                        request.getPassword(),
                        usuario.getPassword()
                );

        if (!passwordCorrecto) {

            throw new RuntimeException(
                    "Credenciales incorrectas"
            );
        }


        // ==========================================
        // 5. Verificar que tenga rol
        // ==========================================

        if (usuario.getRol() == null) {

            throw new RuntimeException(
                    "El usuario no tiene un rol asignado"
            );
        }


        // ==========================================
        // 6. Generar JWT
        // ==========================================

        String token =
                jwtService.generarToken(usuario);


        // ==========================================
        // 7. Usuario que regresamos a Angular
        // ==========================================

        UsuarioLoginDTO usuarioResponse =
                UsuarioLoginDTO.builder()

                        .id(usuario.getId())

                        .email(usuario.getEmail())

                        .rol(
                                usuario
                                        .getRol()
                                        .getNombre()
                        )

                        .build();


        // ==========================================
        // 8. Respuesta final
        // ==========================================

        return LoginResponse.builder()

                .acceso(true)

                .token(token)

                .usuario(usuarioResponse)

                .build();
    }
}