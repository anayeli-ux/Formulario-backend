package com.example.practica.service;

import com.example.practica.dto.*;
import com.example.practica.exception.EmailDuplicadoException;
import com.example.practica.exception.MenorDeEdadException;
import com.example.practica.exception.TelefonoDuplicadoException;
import com.example.practica.exception.UsuarioNoEncontradoException;
import com.example.practica.exception.UsuarioYaActivoException;
import com.example.practica.mapper.UsuarioMapper;
import com.example.practica.model.*;
import com.example.practica.repository.*;

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
    private final EmailRepository emailRepository;

    private final UsuarioMapper usuarioMapper;
    private final PostaliaService postaliaService;
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // LISTAR ACTIVOS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios() {

        return usuarioRepository
                .findByFechaBajaIsNullOrderByIdAsc()
                .stream()
                .map(usuarioMapper::toResponseDTO)
                .toList();
    }


    // =========================================================
    // LISTAR ELIMINADOS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuariosEliminados() {

        return usuarioRepository
                .findByFechaBajaIsNotNullOrderByIdAsc()
                .stream()
                .map(usuarioMapper::toResponseDTO)
                .toList();
    }


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuario(Long id) {

        Usuario usuario = usuarioRepository
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        return usuarioMapper.toResponseDTO(usuario);
    }


    // =========================================================
    // OBTENER MI PERFIL
    // El JWT contiene como subject el correo PRINCIPAL.
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerMiPerfil(String email) {

        Email correoPrincipal = emailRepository
                .findByValorIgnoreCaseAndTipoIgnoreCase(
                        email,
                        "PRINCIPAL"
                )
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(email)
                );

        Usuario usuario = correoPrincipal.getUsuario();

        if (usuario.getFechaBaja() != null) {
            throw new UsuarioNoEncontradoException(email);
        }

        return usuarioMapper.toResponseDTO(usuario);
    }


    // =========================================================
    // CREAR USUARIO
    // =========================================================

    @Override
    @Transactional
    public UsuarioResponseDTO crearUsuario(
            UsuarioRequestDTO dto
    ) {

        validarEdad(dto.getFechaNacimiento());

        validarCategoriasPrincipales(
                dto.getTelefonos(),
                dto.getCorreos(),
                dto.getDirecciones()
        );

        validarTelefonosNuevos(dto.getTelefonos());

        validarCorreosNuevos(dto.getCorreos());

        validarDirecciones(dto.getDirecciones());

        Rol rolUser = rolRepository
                .findByNombre("USER")
                .orElseThrow(
                        () -> new IllegalStateException(
                                "El rol USER no existe en la base de datos"
                        )
                );

        Usuario usuario = usuarioMapper.toEntity(dto);

        usuario.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        usuario.setRol(rolUser);
        usuario.setFechaBaja(null);

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        guardarTelefonos(
                usuarioGuardado,
                dto.getTelefonos()
        );

        guardarCorreos(
                usuarioGuardado,
                dto.getCorreos()
        );

        guardarDirecciones(
                usuarioGuardado,
                dto.getDirecciones()
        );

        return usuarioMapper.toResponseDTO(
                usuarioGuardado
        );
    }


    // =========================================================
    // ACTUALIZAR USUARIO
    // =========================================================

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(
            Long id,
            UsuarioUpdateRequestDTO dto
    ) {

        Usuario usuario = usuarioRepository
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException(id)
                );

        validarEdad(dto.getFechaNacimiento());

        validarCategoriasPrincipales(
                dto.getTelefonos(),
                dto.getCorreos(),
                dto.getDirecciones()
        );

        validarTelefonosActualizacion(
                usuario,
                dto.getTelefonos()
        );

        validarCorreosActualizacion(
                usuario,
                dto.getCorreos()
        );

        validarDirecciones(dto.getDirecciones());

        usuarioMapper.updateEntity(
                usuario,
                dto
        );

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

        /*
         * Limpiamos las colecciones administradas por Hibernate.
         * No reemplazamos las listas.
         */
        usuario.getTelefonos().clear();
        usuario.getDirecciones().clear();
        usuario.getEmails().clear();

        /*
         * Procesar los DELETE antes de insertar los contactos
         * nuevos evita conflictos con restricciones UNIQUE.
         */
        usuarioRepository.flush();

        guardarTelefonos(
                usuario,
                dto.getTelefonos()
        );

        guardarCorreos(
                usuario,
                dto.getCorreos()
        );

        guardarDirecciones(
                usuario,
                dto.getDirecciones()
        );

        Usuario actualizado =
                usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(
                actualizado
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

        if (usuario.getFechaBaja() == null) {
            throw new UsuarioYaActivoException(id);
        }

        usuario.setFechaBaja(null);

        Usuario reactivado =
                usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(
                reactivado
        );
    }


    // =========================================================
    // GUARDAR TELÉFONOS
    // =========================================================

    private void guardarTelefonos(
            Usuario usuario,
            List<TelefonoRequest> telefonos
    ) {

        Set<String> valores = new HashSet<>();

        for (TelefonoRequest request : telefonos) {

            String valor =
                    request.getValor().trim();

            if (!valores.add(valor)) {
                throw new IllegalArgumentException(
                        "No se puede repetir el mismo teléfono"
                );
            }

            Telefono telefono = Telefono.builder()
                    .telefono(valor)
                    .categoria(
                            normalizarCategoria(
                                    request.getTipo()
                            )
                    )
                    .usuario(usuario)
                    .build();

            telefonoRepository.save(telefono);

            usuario.getTelefonos().add(telefono);
        }
    }


    // =========================================================
    // GUARDAR CORREOS
    // =========================================================

    private void guardarCorreos(
            Usuario usuario,
            List<CorreoRequest> correos
    ) {

        Set<String> valores = new HashSet<>();

        for (CorreoRequest request : correos) {

            String valor = normalizarCorreo(
                    request.getValor()
            );

            if (!valores.add(valor)) {
                throw new IllegalArgumentException(
                        "No se puede repetir el mismo correo"
                );
            }

            Email email = Email.builder()
                    .tipo(
                            normalizarCategoria(
                                    request.getTipo()
                            )
                    )
                    .valor(valor)
                    .usuario(usuario)
                    .build();

            emailRepository.save(email);

            usuario.getEmails().add(email);
        }
    }


    // =========================================================
    // GUARDAR DIRECCIONES
    // =========================================================

    private void guardarDirecciones(
            Usuario usuario,
            List<DireccionRequest> direcciones
    ) {

        Set<String> valores = new HashSet<>();

        for (DireccionRequest request : direcciones) {

            String clave = claveDireccion(
                    request.getValor(),
                    request.getCodigoPostal()
            );

            if (!valores.add(clave)) {
                throw new IllegalArgumentException(
                        "No se puede repetir la misma dirección"
                );
            }

            CodigoPostal codigoPostal =
                    obtenerOCrearCodigoPostal(
                            request.getCodigoPostal()
                    );

            Direccion direccion = Direccion.builder()
                    .direccion(
                            request.getValor().trim()
                    )
                    .categoria(
                            normalizarCategoria(
                                    request.getTipo()
                            )
                    )
                    .usuario(usuario)
                    .codigoPostal(codigoPostal)
                    .build();

            direccionRepository.save(direccion);

            usuario.getDirecciones().add(direccion);
        }
    }


    // =========================================================
    // VALIDAR TELÉFONOS NUEVOS
    // =========================================================

    private void validarTelefonosNuevos(
            List<TelefonoRequest> telefonos
    ) {

        Set<String> encontrados = new HashSet<>();

        for (TelefonoRequest request : telefonos) {

            String telefono =
                    request.getValor().trim();

            if (!encontrados.add(telefono)) {
                throw new TelefonoDuplicadoException();
            }

            if (
                    telefonoRepository
                            .existsByTelefono(telefono)
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
            List<TelefonoRequest> telefonos
    ) {

        Set<String> actuales =
                telefonoRepository
                        .findByUsuarioId(usuario.getId())
                        .stream()
                        .map(Telefono::getTelefono)
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        Set<String> nuevos =
                new HashSet<>();

        for (TelefonoRequest request : telefonos) {

            String telefono =
                    request.getValor().trim();

            if (!nuevos.add(telefono)) {
                throw new TelefonoDuplicadoException();
            }

            if (actuales.contains(telefono)) {
                continue;
            }

            if (
                    telefonoRepository
                            .existsByTelefono(telefono)
            ) {
                throw new TelefonoDuplicadoException();
            }
        }
    }


    // =========================================================
    // VALIDAR CORREOS NUEVOS
    // =========================================================

    private void validarCorreosNuevos(
            List<CorreoRequest> correos
    ) {

        Set<String> encontrados =
                new HashSet<>();

        for (CorreoRequest request : correos) {

            String correo =
                    normalizarCorreo(
                            request.getValor()
                    );

            if (!encontrados.add(correo)) {
                throw new EmailDuplicadoException();
            }

            if (
                    emailRepository
                            .existsByValorIgnoreCase(correo)
            ) {
                throw new EmailDuplicadoException();
            }
        }
    }


    // =========================================================
    // VALIDAR CORREOS AL ACTUALIZAR
    // =========================================================

    private void validarCorreosActualizacion(
            Usuario usuario,
            List<CorreoRequest> correos
    ) {

        Set<String> actuales =
                emailRepository
                        .findByUsuarioId(usuario.getId())
                        .stream()
                        .map(Email::getValor)
                        .map(this::normalizarCorreo)
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        Set<String> nuevos =
                new HashSet<>();

        for (CorreoRequest request : correos) {

            String correo =
                    normalizarCorreo(
                            request.getValor()
                    );

            if (!nuevos.add(correo)) {
                throw new EmailDuplicadoException();
            }

            if (actuales.contains(correo)) {
                continue;
            }

            if (
                    emailRepository
                            .existsByValorIgnoreCase(correo)
            ) {
                throw new EmailDuplicadoException();
            }
        }
    }


    // =========================================================
    // VALIDAR DIRECCIONES Y CÓDIGOS POSTALES
    // =========================================================

    private void validarDirecciones(
            List<DireccionRequest> direcciones
    ) {

        Set<String> encontradas =
                new HashSet<>();

        for (DireccionRequest request : direcciones) {

            String clave = claveDireccion(
                    request.getValor(),
                    request.getCodigoPostal()
            );

            if (!encontradas.add(clave)) {
                throw new IllegalArgumentException(
                        "No se puede repetir la misma dirección"
                );
            }

            /*
             * Cada dirección puede tener un CP distinto.
             */
            postaliaService.consultarCodigoPostal(
                    request.getCodigoPostal()
            );
        }
    }


    // =========================================================
    // VALIDAR QUE EXISTA EXACTAMENTE UN PRINCIPAL
    // =========================================================

    private void validarCategoriasPrincipales(
            List<TelefonoRequest> telefonos,
            List<CorreoRequest> correos,
            List<DireccionRequest> direcciones
    ) {

        long telefonosPrincipales =
                telefonos.stream()
                        .filter(t ->
                                esPrincipal(t.getTipo())
                        )
                        .count();

        long correosPrincipales =
                correos.stream()
                        .filter(c ->
                                esPrincipal(c.getTipo())
                        )
                        .count();

        long direccionesPrincipales =
                direcciones.stream()
                        .filter(d ->
                                esPrincipal(d.getTipo())
                        )
                        .count();

        if (telefonosPrincipales != 1) {
            throw new IllegalArgumentException(
                    "Debe existir exactamente un teléfono PRINCIPAL"
            );
        }

        if (correosPrincipales != 1) {
            throw new IllegalArgumentException(
                    "Debe existir exactamente un correo PRINCIPAL"
            );
        }

        if (direccionesPrincipales != 1) {
            throw new IllegalArgumentException(
                    "Debe existir exactamente una dirección PRINCIPAL"
            );
        }
    }


    // =========================================================
    // OBTENER O CREAR CÓDIGO POSTAL
    // =========================================================

    private CodigoPostal obtenerOCrearCodigoPostal(
            String codigoPostal
    ) {

        String cp = codigoPostal.trim();

        return codigoPostalRepository
                .findById(cp)
                .orElseGet(() -> {

                    CodigoPostal nuevo =
                            CodigoPostal.builder()
                                    .codigoPostal(cp)
                                    .build();

                    return codigoPostalRepository
                            .save(nuevo);
                });
    }


    // =========================================================
    // UTILIDADES
    // =========================================================

    private boolean esPrincipal(
            String categoria
    ) {

        return "PRINCIPAL".equals(
                normalizarCategoria(categoria)
        );
    }


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


    private String normalizarCorreo(
            String correo
    ) {

        return correo
                .trim()
                .toLowerCase();
    }


    private String claveDireccion(
            String direccion,
            String codigoPostal
    ) {

        return direccion
                .trim()
                .toLowerCase()
                + "|"
                + codigoPostal.trim();
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