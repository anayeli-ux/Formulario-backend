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
    public LoginResponse loginAdmin(
            LoginRequest request
    ) {

        String identificador =
                request.getUsuario().trim();

        Usuario usuario = usuarioRepository
                .findByEmailOrTelefono(
                        identificador,
                        identificador
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Credenciales incorrectas"
                        )
                );

        if (!usuario.isActivo()) {
            throw new RuntimeException(
                    "El usuario se encuentra inactivo"
            );
        }

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

        if (!"ADMIN".equalsIgnoreCase(
                usuario.getRol()
        )) {
            throw new RuntimeException(
                    "El usuario no tiene permisos de administrador"
            );
        }

        String token =
                jwtService.generarToken(usuario);

        UsuarioLoginDTO usuarioResponse =
                UsuarioLoginDTO.builder()
                        .id(usuario.getId())
                        .email(usuario.getEmail())
                        .rol(usuario.getRol())
                        .build();

        return LoginResponse.builder()
                .acceso(true)
                .token(token)
                .usuario(usuarioResponse)
                .build();
    }
}