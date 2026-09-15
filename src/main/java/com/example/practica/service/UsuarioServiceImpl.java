package com.example.practica.service;

import com.example.practica.dto.PostaliaResponse;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.exception.EmailDuplicadoException;
import com.example.practica.exception.MenorDeEdadException;
import com.example.practica.exception.TelefonoDuplicadoException;
import com.example.practica.exception.UsuarioNoEncontradoException;
import com.example.practica.exception.UsuarioYaActivoException;
import com.example.practica.mapper.UsuarioMapper;
import com.example.practica.model.Usuario;
import com.example.practica.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PostaliaService postaliaService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioResponseDTO> listarUsuarios() {

        return usuarioRepository
                .findByActivoTrueOrderByIdAsc()
                .stream()
                .map(usuarioMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<UsuarioResponseDTO> listarUsuariosEliminados() {

        return usuarioRepository
                .findByActivoFalseOrderByIdAsc()
                .stream()
                .map(usuarioMapper::toResponseDTO)
                .toList();
    }

    @Override
    public UsuarioResponseDTO buscarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findByIdAndActivoTrue(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO crearUsuario(
            UsuarioRequestDTO dto
    ) {

        validarEdad(dto.getFechaNacimiento());

        if (usuarioRepository.existsByTelefono(dto.getTelefono())) {
            throw new TelefonoDuplicadoException();
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException();
        }
        System.out.println("==================================");
        System.out.println("CREAR USUARIO");
        System.out.println("Email: " + dto.getEmail());
        System.out.println("Telefono: " + dto.getTelefono());
        System.out.println("CP: [" + dto.getCodigoPostal() + "]");
        System.out.println("==================================");

        PostaliaResponse ubicacion =
                postaliaService.consultarCodigoPostal(dto.getCodigoPostal());


        Usuario usuario =
                usuarioMapper.toEntity(dto);

        usuario.setEstado(
                ubicacion.getEstado()
        );

        usuario.setMunicipio(
                ubicacion.getMunicipio()
        );

        // Guardamos la contraseña hasheada con BCrypt
        usuario.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );

        usuario.setRol("USER");

        usuario.setActivo(true);

        usuario.setTokenVersion(0);

        Usuario guardado =
                usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(guardado);
    }

    @Override
    public UsuarioResponseDTO actualizarUsuario(
            Long id,
            UsuarioRequestDTO dto
    ) {

        Usuario usuario = usuarioRepository
                .findByIdAndActivoTrue(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        validarEdad(dto.getFechaNacimiento());

        if (
                !usuario.getTelefono().equals(dto.getTelefono())
                        &&
                        usuarioRepository.existsByTelefono(
                                dto.getTelefono()
                        )
        ) {

            throw new TelefonoDuplicadoException();
        }

        if (
                !usuario.getEmail()
                        .equalsIgnoreCase(dto.getEmail())
                        &&
                        usuarioRepository.existsByEmail(
                                dto.getEmail()
                        )
        ) {

            throw new EmailDuplicadoException();
        }

        PostaliaResponse ubicacion =
                postaliaService.consultarCodigoPostal(
                        dto.getCodigoPostal()
                );

        usuarioMapper.updateEntity(
                dto,
                usuario
        );

        usuario.setEstado(
                ubicacion.getEstado()
        );

        usuario.setMunicipio(
                ubicacion.getMunicipio()
        );

        /*
         * Por ahora NO modificamos la contraseña
         * cuando se actualizan los datos del usuario.
         *
         * Después podemos crear un endpoint separado
         * específicamente para cambiar contraseña.
         */

        Usuario actualizado =
                usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(actualizado);
    }

    @Override
    public boolean eliminarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findByIdAndActivoTrue(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        usuario.setActivo(false);

        usuarioRepository.save(usuario);

        return true;
    }

    @Override
    public UsuarioResponseDTO reactivarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        if (usuario.isActivo()) {
            throw new UsuarioYaActivoException(id);
        }

        usuario.setActivo(true);

        Usuario reactivado =
                usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(reactivado);
    }

    private void validarEdad(
            LocalDate fechaNacimiento
    ) {

        LocalDate fechaLimite =
                LocalDate.now()
                        .minusYears(18);

        if (fechaNacimiento.isAfter(fechaLimite)) {

            throw new MenorDeEdadException();
        }
    }
}