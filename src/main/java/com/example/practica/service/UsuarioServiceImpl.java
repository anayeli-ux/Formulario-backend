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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuario(Long id) {
        Usuario usuario = usuarioRepository
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        return usuarioMapper.toResponseDTO(usuario);
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
    // ACTUALIZAR USUARIO
    // =========================================================

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioUpdateRequestDTO dto) {

        Usuario usuario = usuarioRepository
                .findByIdAndFechaBajaIsNull(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        validarEdad(dto.getFechaNacimiento());
        validarCategoriasPrincipales(dto.getTelefonos(), dto.getCorreos(), dto.getDirecciones());

        validarTelefonosActualizacion(usuario, dto.getTelefonos());
        validarCorreosActualizacion(usuario, dto.getCorreos());
        validarDireccionesActualizacion(usuario, dto.getDirecciones());

        boolean cambiaronTelefonos = telefonosCambiaron(usuario, dto.getTelefonos());
        boolean cambiaronCorreos = correosCambiaron(usuario, dto.getCorreos());
        boolean cambiaronDirecciones = direccionesCambiaron(usuario, dto.getDirecciones());

        usuarioMapper.updateEntity(usuario, dto);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (cambiaronTelefonos) {
            actualizarTelefonosDiferencial(usuario, dto.getTelefonos());
        }

        if (cambiaronCorreos) {
            actualizarCorreosDiferencial(usuario, dto.getCorreos());
        }

        if (cambiaronDirecciones) {
            actualizarDireccionesDiferencial(usuario, dto.getDirecciones());
        }

        Usuario actualizado = usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(actualizado);
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
        /*
         * Construimos las claves recibidas desde Angular.
         *
         * Ejemplo:
         * PRINCIPAL|7711234567
         * TRABAJO|7719876543
         */
        Set<String> clavesNuevas = nuevos.stream()
                .map(t -> normalizarCategoria(t.getTipo()) + "|" + t.getValor().trim())
                .collect(Collectors.toSet());

        /*
         * Eliminamos únicamente los teléfonos
         * que ya no existen en la petición.
         *
         * orphanRemoval=true hará el DELETE.
         */
        usuario.getTelefonos().removeIf(actual -> {
            String claveActual = normalizarCategoria(actual.getCategoria())
                    + "|"
                    + actual.getTelefono().trim();
            return !clavesNuevas.contains(claveActual);
        });

        /*
         * Obtenemos las claves que permanecieron.
         */
        Set<String> clavesActuales = usuario.getTelefonos().stream()
                .map(actual -> normalizarCategoria(actual.getCategoria()) + "|" + actual.getTelefono().trim())
                .collect(Collectors.toSet());

        /*
         * Insertamos solamente los teléfonos nuevos.
         */
        for (TelefonoRequest request : nuevos) {
            String valor = request.getValor().trim();
            String clave = normalizarCategoria(request.getTipo()) + "|" + valor;

            if (clavesActuales.contains(clave)) {
                continue;
            }

            Telefono nuevo = Telefono.builder()
                    .telefono(valor)
                    .categoria(normalizarCategoria(request.getTipo()))
                    .usuario(usuario)
                    .build();

            usuario.getTelefonos().add(nuevo);
            clavesActuales.add(clave);
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
    // ACTUALIZAR CORREOS DIFERENCIAL
    // =========================================================

    private void actualizarCorreosDiferencial(
            Usuario usuario,
            List<CorreoRequest> nuevos
    ) {
        /*
         * Correos recibidos desde Angular.
         *
         * Ejemplo:
         * PRINCIPAL|correo@gmail.com
         * TRABAJO|correo@empresa.com
         */
        Set<String> clavesNuevas = nuevos.stream()
                .map(c -> normalizarCategoria(c.getTipo()) + "|" + normalizarCorreo(c.getValor()))
                .collect(Collectors.toSet());

        /*
         * Eliminamos únicamente los correos
         * que dejaron de existir.
         */
        usuario.getEmails().removeIf(actual -> {
            String claveActual = normalizarCategoria(actual.getTipo())
                    + "|"
                    + normalizarCorreo(actual.getValor());
            return !clavesNuevas.contains(claveActual);
        });

        /*
         * Correos que permanecieron sin cambios.
         */
        Set<String> clavesActuales = usuario.getEmails().stream()
                .map(actual -> normalizarCategoria(actual.getTipo()) + "|" + normalizarCorreo(actual.getValor()))
                .collect(Collectors.toSet());

        /*
         * Insertamos solamente los correos nuevos.
         */
        for (CorreoRequest request : nuevos) {
            String valor = normalizarCorreo(request.getValor());
            String clave = normalizarCategoria(request.getTipo()) + "|" + valor;

            if (clavesActuales.contains(clave)) {
                continue;
            }

            Email nuevo = Email.builder()
                    .tipo(normalizarCategoria(request.getTipo()))
                    .valor(valor)
                    .usuario(usuario)
                    .build();

            usuario.getEmails().add(nuevo);
            clavesActuales.add(clave);
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
        Set<String> clavesNuevas = nuevas.stream()
                .map(d -> normalizarCategoria(d.getTipo())
                        + "|"
                        + claveDireccion(d.getValor(), d.getCodigoPostal())
                )
                .collect(Collectors.toSet());

        usuario.getDirecciones().removeIf(actual -> {
            String claveActual = normalizarCategoria(actual.getCategoria())
                    + "|"
                    + claveDireccion(actual.getDireccion(), actual.getCodigoPostal().getCodigoPostal());
            return !clavesNuevas.contains(claveActual);
        });

        Set<String> clavesActuales = usuario.getDirecciones().stream()
                .map(actual -> normalizarCategoria(actual.getCategoria())
                        + "|"
                        + claveDireccion(actual.getDireccion(), actual.getCodigoPostal().getCodigoPostal())
                )
                .collect(Collectors.toSet());

        Map<String, CodigoPostal> cacheCodigosPostales = new HashMap<>();

        for (DireccionRequest request : nuevas) {
            String clave = normalizarCategoria(request.getTipo())
                    + "|"
                    + claveDireccion(request.getValor(), request.getCodigoPostal());

            if (clavesActuales.contains(clave)) {
                continue;
            }

            String cp = request.getCodigoPostal().trim();
            CodigoPostal codigoPostal = cacheCodigosPostales.get(cp);

            if (codigoPostal == null) {
                codigoPostal = obtenerOCrearCodigoPostal(cp);
                cacheCodigosPostales.put(cp, codigoPostal);
            }

            Direccion nueva = Direccion.builder()
                    .usuario(usuario)
                    .categoria(normalizarCategoria(request.getTipo()))
                    .direccion(request.getValor().trim())
                    .codigoPostal(codigoPostal)
                    .build();

            usuario.getDirecciones().add(nueva);
            clavesActuales.add(clave);
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
        Set<String> actuales = usuario.getTelefonos().stream()
                .map(Telefono::getTelefono)
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> nuevos = new HashSet<>();

        for (TelefonoRequest request : telefonos) {
            String telefono = request.getValor().trim();

            if (!nuevos.add(telefono)) {
                throw new TelefonoDuplicadoException();
            }

            if (actuales.contains(telefono)) {
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
        Set<String> actuales = usuario.getEmails().stream()
                .map(Email::getValor)
                .map(this::normalizarCorreo)
                .collect(Collectors.toSet());

        Set<String> nuevos = new HashSet<>();

        for (CorreoRequest request : correos) {
            String correo = normalizarCorreo(request.getValor());

            if (!nuevos.add(correo)) {
                throw new EmailDuplicadoException();
            }

            if (actuales.contains(correo)) {
                continue;
            }

            if (emailRepository.existsByValorIgnoreCase(correo)) {
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

    private boolean telefonosCambiaron(Usuario usuario, List<TelefonoRequest> nuevos) {
        Set<String> actuales = usuario.getTelefonos().stream()
                .map(t -> normalizarCategoria(t.getCategoria()) + "|" + t.getTelefono().trim())
                .collect(Collectors.toSet());

        Set<String> recibidos = nuevos.stream()
                .map(t -> normalizarCategoria(t.getTipo()) + "|" + t.getValor().trim())
                .collect(Collectors.toSet());

        return !actuales.equals(recibidos);
    }

    private boolean correosCambiaron(Usuario usuario, List<CorreoRequest> nuevos) {
        Set<String> actuales = usuario.getEmails().stream()
                .map(e -> normalizarCategoria(e.getTipo()) + "|" + normalizarCorreo(e.getValor()))
                .collect(Collectors.toSet());

        Set<String> recibidos = nuevos.stream()
                .map(e -> normalizarCategoria(e.getTipo()) + "|" + normalizarCorreo(e.getValor()))
                .collect(Collectors.toSet());

        return !actuales.equals(recibidos);
    }

    private boolean direccionesCambiaron(Usuario usuario, List<DireccionRequest> nuevas) {
        Set<String> actuales = usuario.getDirecciones().stream()
                .map(d -> normalizarCategoria(d.getCategoria())
                        + "|" + d.getDireccion().trim().toLowerCase()
                        + "|" + d.getCodigoPostal().getCodigoPostal().trim())
                .collect(Collectors.toSet());

        Set<String> recibidas = nuevas.stream()
                .map(d -> normalizarCategoria(d.getTipo())
                        + "|" + d.getValor().trim().toLowerCase()
                        + "|" + d.getCodigoPostal().trim())
                .collect(Collectors.toSet());

        return !actuales.equals(recibidas);
    }


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