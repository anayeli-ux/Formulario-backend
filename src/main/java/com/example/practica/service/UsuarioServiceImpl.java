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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResumenDTO> listarResumenes(boolean eliminados, String busqueda, Pageable pageable) {
        String termino = busqueda == null ? "" : busqueda.trim();
        Page<Usuario> usuarios;
        if (termino.isEmpty()) {
            usuarios = eliminados
                    ? usuarioRepository.findByFechaBajaIsNotNullOrderByIdAsc(pageable)
                    : usuarioRepository.findByFechaBajaIsNullOrderByIdAsc(pageable);
        } else {
            usuarios = eliminados
                    ? usuarioRepository.buscarEliminados(termino, pageable)
                    : usuarioRepository.buscarActivos(termino, pageable);
        }
        return usuarios.map(usuarioMapper::toResumenDTO);
    }


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuario(Long id) {
        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioContactosDTO buscarContactos(Long id) {
        return usuarioMapper.toContactosDTO(buscarUsuario(id));
    }


    // =========================================================
    // OBTENER MI PERFIL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerMiPerfil(String email) {
        Email correoPrincipal = emailRepository
                .findByValorIgnoreCaseAndTipoIgnoreCase(email, "PRINCIPAL")
                .orElseThrow(() -> new UsuarioNoEncontradoException(email));

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
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto) {
        validarEdad(dto.getFechaNacimiento());
        validarCategoriasPrincipales(dto.getTelefonos(), dto.getCorreos(), dto.getDirecciones());

        validarTelefonosNuevos(dto.getTelefonos());
        validarCorreosNuevos(dto.getCorreos());
        validarDirecciones(dto.getDirecciones());

        Rol rolUser = rolRepository
                .findByNombre("USER")
                .orElseThrow(() -> new IllegalStateException("El rol USER no existe en la base de datos"));

        Usuario usuario = usuarioMapper.toEntity(dto);

        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rolUser);
        usuario.setFechaBaja(null);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        guardarTelefonos(usuarioGuardado, dto.getTelefonos());
        guardarCorreos(usuarioGuardado, dto.getCorreos());
        guardarDirecciones(usuarioGuardado, dto.getDirecciones());

        return usuarioMapper.toResponseDTO(usuarioGuardado);
    }


    // =========================================================
    // ACTUALIZAR USUARIO (HTTP 204 - VOID)
    // =========================================================

    @Override
    @Transactional
    public void actualizarUsuario(Long id, UsuarioUpdateRequestDTO dto) {

        Usuario usuario = usuarioRepository
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        validarEdad(dto.getFechaNacimiento());
        validarCategoriasPrincipales(dto.getTelefonos(), dto.getCorreos(), dto.getDirecciones());

        validarIdsContactos(usuario, dto);
        validarTelefonosActualizacion(usuario, dto.getTelefonos());
        validarCorreosActualizacion(usuario, dto.getCorreos());
        validarDireccionesActualizacion(usuario, dto.getDirecciones());

        usuarioMapper.updateEntity(usuario, dto);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            boolean mismaPassword = passwordEncoder.matches(
                    dto.getPassword(),
                    usuario.getPassword()
            );

            if (!mismaPassword) {
                usuario.setPassword(
                        passwordEncoder.encode(dto.getPassword())
                );
            }
        }

        actualizarTelefonosDiferencial(usuario, dto.getTelefonos());
        actualizarCorreosDiferencial(usuario, dto.getCorreos());
        actualizarDireccionesDiferencial(usuario, dto.getDirecciones());

        // Al ser una entidad administrada dentro de @Transactional,
        // Hibernate aplicará dirty checking al finalizar sin necesidad de save() ni return.
    }


    // =========================================================
    // BAJA LÓGICA
    // =========================================================

    @Override
    @Transactional
    public boolean eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        usuario.setFechaBaja(LocalDateTime.now());
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
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        if (usuario.getFechaBaja() == null) {
            throw new UsuarioYaActivoException(id);
        }

        usuario.setFechaBaja(null);
        Usuario reactivado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(reactivado);
    }


    // =========================================================
    // GUARDAR TELÉFONOS (Creación)
    // =========================================================

    private void guardarTelefonos(Usuario usuario, List<TelefonoRequest> telefonos) {
        Set<String> valores = new HashSet<>();

        for (TelefonoRequest request : telefonos) {
            String valor = request.getValor().trim();

            if (!valores.add(valor)) {
                throw new IllegalArgumentException("No se puede repetir el mismo teléfono");
            }

            Telefono telefono = Telefono.builder()
                    .telefono(valor)
                    .categoria(normalizarCategoria(request.getTipo()))
                    .usuario(usuario)
                    .build();

            telefonoRepository.save(telefono);
            usuario.getTelefonos().add(telefono);
        }
    }


    // =========================================================
    // ACTUALIZAR TELÉFONOS DIFERENCIAL
    // =========================================================

    private void actualizarTelefonosDiferencial(
            Usuario usuario,
            List<TelefonoRequest> nuevos
    ) {
        Map<Long, Telefono> existentes = usuario.getTelefonos().stream()
                .collect(Collectors.toMap(Telefono::getId, telefono -> telefono));
        Set<Long> idsRecibidos = nuevos.stream()
                .map(TelefonoRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        usuario.getTelefonos().removeIf(actual -> !idsRecibidos.contains(actual.getId()));

        for (TelefonoRequest request : nuevos) {
            String valor = request.getValor().trim();
            String categoria = normalizarCategoria(request.getTipo());
            if (request.getId() != null) {
                Telefono existente = existentes.get(request.getId());
                existente.setTelefono(valor);
                existente.setCategoria(categoria);
            } else {
                usuario.getTelefonos().add(Telefono.builder()
                        .telefono(valor)
                        .categoria(categoria)
                        .usuario(usuario)
                        .build());
            }
        }
    }


    // =========================================================
    // GUARDAR CORREOS (Creación)
    // =========================================================

    private void guardarCorreos(Usuario usuario, List<CorreoRequest> correos) {
        Set<String> valores = new HashSet<>();

        for (CorreoRequest request : correos) {
            String valor = normalizarCorreo(request.getValor());

            if (!valores.add(valor)) {
                throw new IllegalArgumentException("No se puede repetir el mismo correo");
            }

            Email email = Email.builder()
                    .tipo(normalizarCategoria(request.getTipo()))
                    .valor(valor)
                    .usuario(usuario)
                    .build();

            emailRepository.save(email);
            usuario.getEmails().add(email);
        }
    }


    // =========================================================
    // ACTUALIZAR CORREOS DIFERENCIAL (VERSIÓN OPTIMIZADA)
    // =========================================================

    private void actualizarCorreosDiferencial(
            Usuario usuario,
            List<CorreoRequest> nuevos
    ) {
        Map<Long, Email> existentes = usuario.getEmails().stream()
                .collect(Collectors.toMap(Email::getId, email -> email));
        Set<Long> idsRecibidos = nuevos.stream()
                .map(CorreoRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        usuario.getEmails().removeIf(actual -> !idsRecibidos.contains(actual.getId()));

        for (CorreoRequest request : nuevos) {
            String tipo = normalizarCategoria(request.getTipo());
            String valor = normalizarCorreo(request.getValor());
            if (request.getId() != null) {
                Email existente = existentes.get(request.getId());
                existente.setTipo(tipo);
                existente.setValor(valor);
            } else {
                usuario.getEmails().add(Email.builder()
                        .tipo(tipo)
                        .valor(valor)
                        .usuario(usuario)
                        .build());
            }
        }
    }


    // =========================================================
    // GUARDAR DIRECCIONES (Creación)
    // =========================================================

    private void guardarDirecciones(
            Usuario usuario,
            List<DireccionRequest> direcciones
    ) {
        Map<String, CodigoPostal> codigosPostales = new HashMap<>();

        for (DireccionRequest request : direcciones) {
            String cp = request.getCodigoPostal().trim();
            CodigoPostal codigoPostal = codigosPostales.get(cp);

            if (codigoPostal == null) {
                codigoPostal = obtenerOCrearCodigoPostal(cp);
                codigosPostales.put(cp, codigoPostal);
            }

            Direccion direccion = Direccion.builder()
                    .usuario(usuario)
                    .categoria(normalizarCategoria(request.getTipo()))
                    .direccion(request.getValor().trim())
                    .codigoPostal(codigoPostal)
                    .build();

            direccionRepository.save(direccion);
            usuario.getDirecciones().add(direccion);
        }
    }


    // =========================================================
    // ACTUALIZAR DIRECCIONES DIFERENCIAL
    // =========================================================

    private void actualizarDireccionesDiferencial(
            Usuario usuario,
            List<DireccionRequest> nuevas
    ) {
        Map<Long, Direccion> existentes = usuario.getDirecciones().stream()
                .collect(Collectors.toMap(Direccion::getId, direccion -> direccion));
        Set<Long> idsRecibidos = nuevas.stream()
                .map(DireccionRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        usuario.getDirecciones().removeIf(actual -> !idsRecibidos.contains(actual.getId()));
        Map<String, CodigoPostal> cacheCodigosPostales = new HashMap<>();

        for (DireccionRequest request : nuevas) {
            String cp = request.getCodigoPostal().trim();
            CodigoPostal codigoPostal = cacheCodigosPostales.get(cp);

            if (codigoPostal == null) {
                codigoPostal = obtenerOCrearCodigoPostal(cp);
                cacheCodigosPostales.put(cp, codigoPostal);
            }

            String categoria = normalizarCategoria(request.getTipo());
            String valor = request.getValor().trim();
            if (request.getId() != null) {
                Direccion existente = existentes.get(request.getId());
                existente.setCategoria(categoria);
                existente.setDireccion(valor);
                existente.setCodigoPostal(codigoPostal);
            } else {
                usuario.getDirecciones().add(Direccion.builder()
                        .usuario(usuario)
                        .categoria(categoria)
                        .direccion(valor)
                        .codigoPostal(codigoPostal)
                        .build());
            }
        }
    }


    // =========================================================
    // VALIDAR TELÉFONOS NUEVOS
    // =========================================================

    private void validarTelefonosNuevos(List<TelefonoRequest> telefonos) {
        Set<String> encontrados = new HashSet<>();

        for (TelefonoRequest request : telefonos) {
            String telefono = request.getValor().trim();

            if (!encontrados.add(telefono)) {
                throw new TelefonoDuplicadoException();
            }

            if (telefonoRepository.existsByTelefono(telefono)) {
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
        Map<Long, Telefono> actuales = usuario.getTelefonos().stream()
                .collect(Collectors.toMap(Telefono::getId, telefono -> telefono));

        Set<String> nuevos = new HashSet<>();

        for (TelefonoRequest request : telefonos) {
            String telefono = request.getValor().trim();

            if (!nuevos.add(telefono)) {
                throw new TelefonoDuplicadoException();
            }

            Telefono identificado = request.getId() == null
                    ? null
                    : actuales.get(request.getId());
            if (identificado != null && identificado.getTelefono().trim().equals(telefono)) {
                continue;
            }

            if (telefonoRepository.existsByTelefono(telefono)) {
                throw new TelefonoDuplicadoException();
            }
        }
    }


    // =========================================================
    // VALIDAR CORREOS NUEVOS
    // =========================================================

    private void validarCorreosNuevos(List<CorreoRequest> correos) {
        Set<String> encontrados = new HashSet<>();

        for (CorreoRequest request : correos) {
            String correo = normalizarCorreo(request.getValor());

            if (!encontrados.add(correo)) {
                throw new EmailDuplicadoException();
            }

            if (emailRepository.existsByValorIgnoreCase(correo)) {
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
        Map<Long, Email> actuales = usuario.getEmails().stream()
                .collect(Collectors.toMap(Email::getId, email -> email));

        Set<String> nuevos = new HashSet<>();

        for (CorreoRequest request : correos) {
            String correo = normalizarCorreo(request.getValor());

            if (!nuevos.add(correo)) {
                throw new EmailDuplicadoException();
            }

            Email identificado = request.getId() == null
                    ? null
                    : actuales.get(request.getId());
            if (identificado != null
                    && normalizarCorreo(identificado.getValor()).equals(correo)) {
                continue;
            }

            Email emailExistente = emailRepository.findByValorIgnoreCase(correo)
                    .orElse(null);
            if (emailExistente != null
                    && !Objects.equals(emailExistente.getId(), request.getId())) {
                throw new EmailDuplicadoException();
            }
        }
    }


    // =========================================================
    // VALIDAR DIRECCIONES EN CREACIÓN
    // =========================================================

    private void validarDirecciones(List<DireccionRequest> direcciones) {
        Set<String> encontradas = new HashSet<>();
        Set<String> codigosPostalesValidados = new HashSet<>();

        for (DireccionRequest request : direcciones) {
            String clave = claveDireccion(request.getValor(), request.getCodigoPostal());

            if (!encontradas.add(clave)) {
                throw new IllegalArgumentException("No se puede repetir la misma dirección");
            }

            String cp = request.getCodigoPostal().trim();

            if (codigosPostalesValidados.add(cp)) {
                postaliaService.consultarCodigoPostal(cp);
            }
        }
    }


    // =========================================================
    // VALIDAR DIRECCIONES AL ACTUALIZAR
    // =========================================================

    private void validarDireccionesActualizacion(Usuario usuario, List<DireccionRequest> direcciones) {
        Set<String> encontradas = new HashSet<>();

        Set<String> codigosPostalesActuales = usuario.getDirecciones().stream()
                .map(Direccion::getCodigoPostal)
                .map(CodigoPostal::getCodigoPostal)
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> codigosPostalesValidados = new HashSet<>();

        for (DireccionRequest request : direcciones) {
            String clave = claveDireccion(request.getValor(), request.getCodigoPostal());

            if (!encontradas.add(clave)) {
                throw new IllegalArgumentException("No se puede repetir la misma dirección");
            }

            String cp = request.getCodigoPostal().trim();

            if (codigosPostalesActuales.contains(cp)) {
                continue;
            }

            if (codigosPostalesValidados.add(cp)) {
                postaliaService.consultarCodigoPostal(cp);
            }
        }
    }


    // =========================================================
    // DETECCIÓN DE CAMBIOS EN COLECCIONES
    // =========================================================

    // =========================================================
    // VALIDAR QUE EXISTA EXACTAMENTE UN PRINCIPAL
    // =========================================================

    private void validarCategoriasPrincipales(
            List<TelefonoRequest> telefonos,
            List<CorreoRequest> correos,
            List<DireccionRequest> direcciones
    ) {
        long telefonosPrincipales = telefonos.stream()
                .filter(t -> esPrincipal(t.getTipo()))
                .count();

        long correosPrincipales = correos.stream()
                .filter(c -> esPrincipal(c.getTipo()))
                .count();

        long direccionesPrincipales = direcciones.stream()
                .filter(d -> esPrincipal(d.getTipo()))
                .count();

        if (telefonosPrincipales != 1) {
            throw new IllegalArgumentException("Debe existir exactamente un teléfono PRINCIPAL");
        }

        if (correosPrincipales != 1) {
            throw new IllegalArgumentException("Debe existir exactamente un correo PRINCIPAL");
        }

        if (direccionesPrincipales != 1) {
            throw new IllegalArgumentException("Debe existir exactamente una dirección PRINCIPAL");
        }
    }


    // =========================================================
    // OBTENER O CREAR CÓDIGO POSTAL
    // =========================================================

    private CodigoPostal obtenerOCrearCodigoPostal(String codigoPostal) {
        String cp = codigoPostal.trim();

        return codigoPostalRepository
                .findById(cp)
                .orElseGet(() -> {
                    CodigoPostal nuevo = CodigoPostal.builder()
                            .codigoPostal(cp)
                            .build();
                    return codigoPostalRepository.save(nuevo);
                });
    }

    private void validarIdsContactos(Usuario usuario, UsuarioUpdateRequestDTO dto) {
        validarIds(
                dto.getTelefonos().stream().map(TelefonoRequest::getId).toList(),
                usuario.getTelefonos().stream().map(Telefono::getId).collect(Collectors.toSet()),
                "teléfono"
        );
        validarIds(
                dto.getCorreos().stream().map(CorreoRequest::getId).toList(),
                usuario.getEmails().stream().map(Email::getId).collect(Collectors.toSet()),
                "correo"
        );
        validarIds(
                dto.getDirecciones().stream().map(DireccionRequest::getId).toList(),
                usuario.getDirecciones().stream().map(Direccion::getId).collect(Collectors.toSet()),
                "dirección"
        );
    }

    private void validarIds(List<Long> idsRecibidos, Set<Long> idsDelUsuario, String contacto) {
        Set<Long> idsUnicos = new HashSet<>();
        for (Long id : idsRecibidos) {
            if (id == null) {
                continue;
            }
            if (!idsUnicos.add(id)) {
                throw new IllegalArgumentException("El ID de " + contacto + " está repetido: " + id);
            }
            if (!idsDelUsuario.contains(id)) {
                throw new IllegalArgumentException("El ID de " + contacto + " no pertenece al usuario");
            }
        }
    }


    // =========================================================
    // UTILIDADES
    // =========================================================

    private boolean esPrincipal(String categoria) {
        return "PRINCIPAL".equals(normalizarCategoria(categoria));
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return "OTRO";
        }
        return categoria.trim().toUpperCase();
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private String claveDireccion(String direccion, String codigoPostal) {
        return direccion.trim().toLowerCase() + "|" + codigoPostal.trim();
    }


    // =========================================================
    // VALIDAR EDAD
    // =========================================================

    private void validarEdad(LocalDate fechaNacimiento) {
        LocalDate fechaLimite = LocalDate.now().minusYears(18);

        if (fechaNacimiento.isAfter(fechaLimite)) {
            throw new MenorDeEdadException();
        }
    }
}