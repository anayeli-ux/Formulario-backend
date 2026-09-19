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

import com.example.practica.model.CodigoPostal;
import com.example.practica.model.Direccion;
import com.example.practica.model.Rol;
import com.example.practica.model.Telefono;
import com.example.practica.model.Usuario;

import com.example.practica.repository.CodigoPostalRepository;
import com.example.practica.repository.DireccionRepository;
import com.example.practica.repository.RolRepository;
import com.example.practica.repository.TelefonoRepository;
import com.example.practica.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TelefonoRepository telefonoRepository;
    private final DireccionRepository direccionRepository;
    private final CodigoPostalRepository codigoPostalRepository;

    private final UsuarioMapper usuarioMapper;
    private final PostaliaService postaliaService;
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // LISTAR USUARIOS ACTIVOS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios() {

        return usuarioRepository
                .findByActivoTrueOrderByIdAsc()
                .stream()
                .map(this::construirRespuesta)
                .toList();
    }


    // =========================================================
    // LISTAR USUARIOS INACTIVOS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuariosEliminados() {

        return usuarioRepository
                .findByActivoFalseOrderByIdAsc()
                .stream()
                .map(this::construirRespuesta)
                .toList();
    }


    // =========================================================
    // BUSCAR USUARIO
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findByIdAndActivoTrue(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        return construirRespuesta(usuario);
    }


    // =========================================================
    // OBTENER MI PERFIL
    // =========================================================
    //
    // Usado por el endpoint /api/usuarios/me: el usuario logueado
    // (identificado por el email que viene del JWT, no por un id
    // en la URL) consulta sus propios datos.
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerMiPerfil(String email) {

        Usuario usuario = usuarioRepository
                .findByEmailAndActivoTrue(email)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(email)
                );

        return construirRespuesta(usuario);
    }


    // =========================================================
    // CREAR USUARIO
    // =========================================================

    @Override
    @Transactional
    public UsuarioResponseDTO crearUsuario(
            UsuarioRequestDTO dto
    ) {

        // 1. Validar edad
        validarEdad(dto.getFechaNacimiento());


        // 2. Validar email duplicado
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException();
        }


        // 3. Validar teléfono duplicado
        if (telefonoRepository.existsByTelefono(dto.getTelefono())) {
            throw new TelefonoDuplicadoException();
        }


        // 4. Consultar código postal en Postalia
        PostaliaResponse ubicacion =
                postaliaService.consultarCodigoPostal(
                        dto.getCodigoPostal()
                );


        // 5. Buscar el rol USER
        Rol rolUser = rolRepository
                .findByNombre("USER")
                .orElseThrow(
                        () -> new IllegalStateException(
                                "El rol USER no existe en la base de datos"
                        )
                );


        // 6. Convertir DTO -> Usuario
        Usuario usuario =
                usuarioMapper.toEntity(dto);


        // 7. Hashear contraseña
        usuario.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );


        // 8. Asignar rol
        usuario.setRol(rolUser);

        usuario.setActivo(true);
        usuario.setTokenVersion(0);


        // 9. Guardar usuario
        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);


        // 10. Crear teléfono
        Telefono telefono = Telefono.builder()
                .telefono(dto.getTelefono())
                .usuario(usuarioGuardado)
                .build();

        telefonoRepository.save(telefono);


        // 11. Buscar o crear código postal
        CodigoPostal codigoPostal =
                codigoPostalRepository
                        .findById(dto.getCodigoPostal())
                        .orElseGet(() -> {

                            CodigoPostal nuevo =
                                    CodigoPostal.builder()
                                            .codigoPostal(
                                                    dto.getCodigoPostal()
                                            )
                                            .build();

                            return codigoPostalRepository.save(nuevo);
                        });


        // 12. Crear dirección
        Direccion direccion = Direccion.builder()
                .direccion(dto.getDireccion())
                .usuario(usuarioGuardado)
                .codigoPostal(codigoPostal)
                .build();

        direccionRepository.save(direccion);


        // 13. Construir respuesta
        return usuarioMapper.toResponseDTO(
                usuarioGuardado,
                telefono,
                direccion,
                ubicacion.getEstado(),
                ubicacion.getMunicipio()
        );
    }


    // =========================================================
    // ACTUALIZAR USUARIO
    // =========================================================

    @Override
    @Transactional
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


        // -----------------------------------------------------
        // Obtener teléfono actual
        // -----------------------------------------------------

        Telefono telefono = obtenerTelefono(usuario);


        // -----------------------------------------------------
        // Validar teléfono duplicado
        // -----------------------------------------------------

        if (
                telefono != null
                        && !telefono.getTelefono().equals(dto.getTelefono())
                        && telefonoRepository.existsByTelefono(dto.getTelefono())
        ) {

            throw new TelefonoDuplicadoException();
        }


        // -----------------------------------------------------
        // Validar email duplicado
        // -----------------------------------------------------

        if (
                !usuario.getEmail().equalsIgnoreCase(dto.getEmail())
                        && usuarioRepository.existsByEmail(dto.getEmail())
        ) {

            throw new EmailDuplicadoException();
        }


        // -----------------------------------------------------
        // Consultar Postalia
        // -----------------------------------------------------

        PostaliaResponse ubicacion =
                postaliaService.consultarCodigoPostal(
                        dto.getCodigoPostal()
                );


        // -----------------------------------------------------
        // Actualizar Usuario
        // -----------------------------------------------------

        usuarioMapper.updateEntity(
                usuario,
                dto
        );

        Usuario usuarioActualizado =
                usuarioRepository.save(usuario);


        // -----------------------------------------------------
        // Actualizar teléfono
        // -----------------------------------------------------

        if (telefono == null) {

            telefono = Telefono.builder()
                    .telefono(dto.getTelefono())
                    .usuario(usuarioActualizado)
                    .build();

        } else {

            telefono.setTelefono(
                    dto.getTelefono()
            );
        }

        telefonoRepository.save(telefono);


        // -----------------------------------------------------
        // Buscar o crear código postal
        // -----------------------------------------------------

        CodigoPostal codigoPostal =
                codigoPostalRepository
                        .findById(dto.getCodigoPostal())
                        .orElseGet(() -> {

                            CodigoPostal nuevo =
                                    CodigoPostal.builder()
                                            .codigoPostal(
                                                    dto.getCodigoPostal()
                                            )
                                            .build();

                            return codigoPostalRepository.save(nuevo);
                        });


        // -----------------------------------------------------
        // Actualizar dirección
        // -----------------------------------------------------

        Direccion direccion =
                obtenerDireccion(usuario);


        if (direccion == null) {

            direccion = Direccion.builder()
                    .direccion(dto.getDireccion())
                    .usuario(usuarioActualizado)
                    .codigoPostal(codigoPostal)
                    .build();

        } else {

            direccion.setDireccion(
                    dto.getDireccion()
            );

            direccion.setCodigoPostal(
                    codigoPostal
            );
        }

        direccionRepository.save(direccion);


        return usuarioMapper.toResponseDTO(
                usuarioActualizado,
                telefono,
                direccion,
                ubicacion.getEstado(),
                ubicacion.getMunicipio()
        );
    }


    // =========================================================
    // BAJA LÓGICA
    // =========================================================

    @Override
    @Transactional
    public boolean eliminarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findByIdAndActivoTrue(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        usuario.setActivo(false);

        /*
         * Incrementamos tokenVersion.
         *
         * Esto permitirá invalidar tokens anteriores
         * cuando adaptemos JwtService.
         */
        usuario.setTokenVersion(
                usuario.getTokenVersion() + 1
        );

        usuarioRepository.save(usuario);

        return true;
    }


    // =========================================================
    // REACTIVAR
    // =========================================================

    @Override
    @Transactional
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

        return construirRespuesta(reactivado);
    }


    // =========================================================
    // CONSTRUIR RESPONSE DTO
    // =========================================================
    //
    // CAMBIO: ya no se consulta Postalia aquí. El listado de
    // usuarios (y buscarUsuario/reactivarUsuario, que también
    // usan este método) no necesita estado/municipio.
    //
    // Esos datos se piden a Postalia solo cuando hacen falta:
    // - Angular llama directamente a GET /api/postalia/{cp}
    //   cuando el admin abre el modal de edición o cambia el CP.
    // - crearUsuario() y actualizarUsuario() siguen consultando
    //   Postalia como validación de backend.
    //
    // Esto evita 1 llamada a Postalia POR CADA usuario listado
    // (antes: 30 usuarios = 30 llamadas; ahora: 0).
    // =========================================================

    private UsuarioResponseDTO construirRespuesta(
            Usuario usuario
    ) {

        Telefono telefono =
                obtenerTelefono(usuario);

        Direccion direccion =
                obtenerDireccion(usuario);

        return usuarioMapper.toResponseDTO(
                usuario,
                telefono,
                direccion,
                null,
                null
        );
    }


    // =========================================================
    // OBTENER TELÉFONO PRINCIPAL
    // =========================================================

    private Telefono obtenerTelefono(
            Usuario usuario
    ) {

        return telefonoRepository
                .findByUsuarioId(usuario.getId())
                .stream()
                .findFirst()
                .orElse(null);
    }


    // =========================================================
    // OBTENER DIRECCIÓN PRINCIPAL
    // =========================================================

    private Direccion obtenerDireccion(
            Usuario usuario
    ) {

        return direccionRepository
                .findByUsuarioId(usuario.getId())
                .stream()
                .findFirst()
                .orElse(null);
    }


    // =========================================================
    // VALIDAR EDAD
    // =========================================================

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