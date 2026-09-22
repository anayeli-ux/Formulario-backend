package com.example.practica.service;

import com.example.practica.dto.DireccionRequest;
import com.example.practica.dto.PostaliaResponse;
import com.example.practica.dto.TelefonoRequest;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                .findByFechaBajaIsNullOrderByIdAsc()
                .stream()
                .map(this::construirRespuesta)
                .toList();
    }


    // =========================================================
    // LISTAR USUARIOS ELIMINADOS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuariosEliminados() {

        return usuarioRepository
                .findByFechaBajaIsNotNullOrderByIdAsc()
                .stream()
                .map(this::construirRespuesta)
                .toList();
    }


    // =========================================================
    // BUSCAR USUARIO ACTIVO
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        return construirRespuesta(usuario);
    }


    // =========================================================
    // OBTENER MI PERFIL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerMiPerfil(String email) {

        Usuario usuario = usuarioRepository
                .findByEmailAndFechaBajaIsNull(email)
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

        // 2. Validar email
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException();
        }

        // 3. Validar teléfonos antes de guardar nada
        validarTelefonosNuevos(dto);

        // 4. Validar CP principal con Postalia
        PostaliaResponse ubicacionPrincipal =
                postaliaService.consultarCodigoPostal(
                        dto.getCodigoPostal()
                );

        // 5. Buscar rol USER
        Rol rolUser = rolRepository
                .findByNombre("USER")
                .orElseThrow(
                        () -> new IllegalStateException(
                                "El rol USER no existe en la base de datos"
                        )
                );

        // 6. Crear Usuario
        Usuario usuario = usuarioMapper.toEntity(dto);

        usuario.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        usuario.setRol(rolUser);
        usuario.setFechaBaja(null);

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        // 7. Guardar teléfonos
        guardarTelefonos(
                usuarioGuardado,
                dto
        );

        // 8. Guardar direcciones
        guardarDirecciones(
                usuarioGuardado,
                dto
        );

        /*
         * Como guardamos las relaciones mediante repositories
         * separados, volvemos a cargar las colecciones antes
         * de construir la respuesta.
         */
        recargarRelaciones(usuarioGuardado);

        return usuarioMapper.toResponseDTO(
                usuarioGuardado,
                ubicacionPrincipal.getEstado(),
                ubicacionPrincipal.getMunicipio()
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
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        // 1. Edad
        validarEdad(dto.getFechaNacimiento());

        // 2. Email duplicado
        if (
                !usuario.getEmail()
                        .equalsIgnoreCase(dto.getEmail())
                        && usuarioRepository.existsByEmail(
                        dto.getEmail()
                )
        ) {
            throw new EmailDuplicadoException();
        }

        // 3. Validar teléfonos
        validarTelefonosActualizacion(
                usuario,
                dto
        );

        // 4. Validar CP principal
        PostaliaResponse ubicacionPrincipal =
                postaliaService.consultarCodigoPostal(
                        dto.getCodigoPostal()
                );

        // 5. Actualizar datos de Usuario
        usuarioMapper.updateEntity(
                usuario,
                dto
        );

        /*
         * Si tu formulario de edición manda password,
         * solo la cambiamos cuando realmente venga una.
         */
        if (
                dto.getPassword() != null
                        && !dto.getPassword().isBlank()
        ) {

            usuario.setPassword(
                    passwordEncoder.encode(
                            dto.getPassword()
                    )
            );
        }

        Usuario usuarioActualizado =
                usuarioRepository.save(usuario);

        // 6. Reemplazar teléfonos
        telefonoRepository.deleteAll(
                telefonoRepository.findByUsuarioId(
                        usuarioActualizado.getId()
                )
        );

        telefonoRepository.flush();

        guardarTelefonos(
                usuarioActualizado,
                dto
        );

        // 7. Reemplazar direcciones
        direccionRepository.deleteAll(
                direccionRepository.findByUsuarioId(
                        usuarioActualizado.getId()
                )
        );

        direccionRepository.flush();

        guardarDirecciones(
                usuarioActualizado,
                dto
        );

        // 8. Recargar relaciones
        recargarRelaciones(usuarioActualizado);

        return usuarioMapper.toResponseDTO(
                usuarioActualizado,
                ubicacionPrincipal.getEstado(),
                ubicacionPrincipal.getMunicipio()
        );
    }


    // =========================================================
    // BAJA LÓGICA
    // =========================================================

    @Override
    @Transactional
    public boolean eliminarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        usuario.setFechaBaja(
                LocalDateTime.now()
        );

        usuarioRepository.save(usuario);

        return true;
    }


    // =========================================================
    // REACTIVAR USUARIO
    // =========================================================

    @Override
    @Transactional
    public UsuarioResponseDTO reactivarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        /*
         * fechaBaja == null significa que YA está activo.
         */
        if (usuario.getFechaBaja() == null) {
            throw new UsuarioYaActivoException(id);
        }

        usuario.setFechaBaja(null);

        Usuario reactivado =
                usuarioRepository.save(usuario);

        return construirRespuesta(reactivado);
    }


    // =========================================================
    // GUARDAR TELÉFONOS
    // =========================================================

    private void guardarTelefonos(
            Usuario usuario,
            UsuarioRequestDTO dto
    ) {

        Set<String> telefonosGuardados =
                new HashSet<>();

        /*
         * Primero guardamos el teléfono principal que sigue
         * llegando en dto.telefono.
         */
        Telefono principal = Telefono.builder()
                .telefono(dto.getTelefono())
                .categoria("PRINCIPAL")
                .usuario(usuario)
                .build();

        telefonoRepository.save(principal);

        telefonosGuardados.add(
                dto.getTelefono()
        );


        /*
         * Después guardamos los teléfonos de la lista,
         * evitando repetir el principal.
         */
        if (dto.getTelefonos() != null) {

            for (TelefonoRequest contacto
                    : dto.getTelefonos()) {

                if (
                        contacto.getValor() == null
                                || contacto.getValor().isBlank()
                ) {
                    continue;
                }

                if (
                        telefonosGuardados.add(
                                contacto.getValor()
                        )
                ) {

                    Telefono telefono =
                            Telefono.builder()
                                    .telefono(
                                            contacto.getValor()
                                    )
                                    .categoria(
                                            normalizarCategoria(
                                                    contacto.getTipo()
                                            )
                                    )
                                    .usuario(usuario)
                                    .build();

                    telefonoRepository.save(
                            telefono
                    );
                }
            }
        }
    }


    // =========================================================
    // GUARDAR DIRECCIONES
    // =========================================================

    private void guardarDirecciones(
            Usuario usuario,
            UsuarioRequestDTO dto
    ) {

        Set<String> direccionesGuardadas =
                new HashSet<>();

        // -----------------------------------------------------
        // Dirección principal
        // -----------------------------------------------------

        CodigoPostal codigoPostalPrincipal =
                obtenerOCrearCodigoPostal(
                        dto.getCodigoPostal()
                );

        Direccion principal =
                Direccion.builder()
                        .direccion(dto.getDireccion())
                        .categoria("PRINCIPAL")
                        .usuario(usuario)
                        .codigoPostal(
                                codigoPostalPrincipal
                        )
                        .build();

        direccionRepository.save(principal);

        /*
         * Usamos dirección + CP para detectar duplicados.
         */
        direccionesGuardadas.add(
                claveDireccion(
                        dto.getDireccion(),
                        dto.getCodigoPostal()
                )
        );


        // -----------------------------------------------------
        // Direcciones adicionales
        // -----------------------------------------------------

        if (dto.getDirecciones() != null) {

            for (DireccionRequest contacto
                    : dto.getDirecciones()) {

                if (
                        contacto.getValor() == null
                                || contacto.getValor().isBlank()
                ) {
                    continue;
                }

                String clave = claveDireccion(
                        contacto.getValor(),
                        contacto.getCodigoPostal()
                );

                /*
                 * Si ya guardamos exactamente esa dirección
                 * con ese CP, no la repetimos.
                 */
                if (!direccionesGuardadas.add(clave)) {
                    continue;
                }

                /*
                 * Validamos cada CP adicional con Postalia.
                 */
                postaliaService.consultarCodigoPostal(
                        contacto.getCodigoPostal()
                );

                CodigoPostal codigoPostal =
                        obtenerOCrearCodigoPostal(
                                contacto.getCodigoPostal()
                        );

                Direccion direccion =
                        Direccion.builder()
                                .direccion(
                                        contacto.getValor()
                                )
                                .categoria(
                                        normalizarCategoria(
                                                contacto.getTipo()
                                        )
                                )
                                .usuario(usuario)
                                .codigoPostal(
                                        codigoPostal
                                )
                                .build();

                direccionRepository.save(
                        direccion
                );
            }
        }
    }


    // =========================================================
    // VALIDAR TELÉFONOS AL CREAR
    // =========================================================

    private void validarTelefonosNuevos(
            UsuarioRequestDTO dto
    ) {

        Set<String> telefonos =
                obtenerTelefonosDTO(dto);

        for (String telefono : telefonos) {

            if (
                    telefonoRepository.existsByTelefono(
                            telefono
                    )
            ) {
                throw new TelefonoDuplicadoException();
            }
        }
    }


    // =========================================================
    // VALIDAR TELÉFONOS AL ACTUALIZAR
    // =========================================================

    private void validarTelefonosActualizacion(
            Usuario usuario,
            UsuarioRequestDTO dto
    ) {

        Set<String> nuevosTelefonos =
                obtenerTelefonosDTO(dto);

        Set<String> telefonosActuales =
                telefonoRepository
                        .findByUsuarioId(usuario.getId())
                        .stream()
                        .map(Telefono::getTelefono)
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        for (String telefono : nuevosTelefonos) {

            /*
             * Si el teléfono ya pertenece a este mismo
             * usuario, está permitido.
             */
            if (telefonosActuales.contains(telefono)) {
                continue;
            }

            /*
             * Si pertenece a otro usuario, no.
             */
            if (
                    telefonoRepository.existsByTelefono(
                            telefono
                    )
            ) {
                throw new TelefonoDuplicadoException();
            }
        }
    }


    // =========================================================
    // OBTENER TODOS LOS TELÉFONOS DEL DTO
    // =========================================================

    private Set<String> obtenerTelefonosDTO(
            UsuarioRequestDTO dto
    ) {

        Set<String> telefonos =
                new HashSet<>();

        telefonos.add(
                dto.getTelefono()
        );

        if (dto.getTelefonos() != null) {

            for (TelefonoRequest telefono
                    : dto.getTelefonos()) {

                if (
                        telefono.getValor() != null
                                && !telefono
                                .getValor()
                                .isBlank()
                ) {

                    telefonos.add(
                            telefono.getValor()
                    );
                }
            }
        }

        return telefonos;
    }


    // =========================================================
    // OBTENER O CREAR CÓDIGO POSTAL
    // =========================================================

    private CodigoPostal obtenerOCrearCodigoPostal(
            String codigoPostal
    ) {

        return codigoPostalRepository
                .findById(codigoPostal)
                .orElseGet(() -> {

                    CodigoPostal nuevo =
                            CodigoPostal.builder()
                                    .codigoPostal(
                                            codigoPostal
                                    )
                                    .build();

                    return codigoPostalRepository
                            .save(nuevo);
                });
    }


    // =========================================================
    // RECARGAR RELACIONES
    // =========================================================

    private void recargarRelaciones(
            Usuario usuario
    ) {

        usuario.setTelefonos(
                telefonoRepository.findByUsuarioId(
                        usuario.getId()
                )
        );

        usuario.setDirecciones(
                direccionRepository.findByUsuarioId(
                        usuario.getId()
                )
        );
    }


    // =========================================================
    // CONSTRUIR RESPONSE
    // =========================================================

    private UsuarioResponseDTO construirRespuesta(
            Usuario usuario
    ) {

        recargarRelaciones(usuario);

        /*
         * No consultamos Postalia cuando simplemente
         * listamos usuarios.
         */
        return usuarioMapper.toResponseDTO(
                usuario,
                null,
                null
        );
    }


    // =========================================================
    // NORMALIZAR CATEGORÍA
    // =========================================================

    private String normalizarCategoria(
            String categoria
    ) {

        if (
                categoria == null
                        || categoria.isBlank()
        ) {
            return "OTRO";
        }

        return categoria
                .trim()
                .toUpperCase();
    }


    // =========================================================
    // CLAVE PARA DETECTAR DIRECCIONES REPETIDAS
    // =========================================================

    private String claveDireccion(
            String direccion,
            String codigoPostal
    ) {

        return direccion
                .trim()
                .toLowerCase()
                + "|"
                + codigoPostal;
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

        if (
                fechaNacimiento.isAfter(
                        fechaLimite
                )
        ) {
            throw new MenorDeEdadException();
        }
    }
}