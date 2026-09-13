package com.example.practica.service;

import com.example.practica.dto.PostaliaResponse;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.exception.MenorDeEdadException;
import com.example.practica.exception.TelefonoDuplicadoException;
import com.example.practica.exception.UsuarioNoEncontradoException;
import com.example.practica.exception.UsuarioYaActivoException;
import com.example.practica.mapper.UsuarioMapper;
import com.example.practica.model.Usuario;
import com.example.practica.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PostaliaService postaliaService;

    public UsuarioServiceImpl(
            UsuarioRepository repository,
            UsuarioMapper mapper,
            PostaliaService postaliaService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.postaliaService = postaliaService;
    }

    // =====================================================
    // LISTAR USUARIOS ACTIVOS
    // =====================================================

    @Override
    public List<UsuarioResponseDTO> listarUsuarios() {

        return repository
                .findByActivoTrueOrderByIdAsc()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // LISTAR USUARIOS ELIMINADOS
    // =====================================================

    @Override
    public List<UsuarioResponseDTO> listarUsuariosEliminados() {

        return repository
                .findByActivoFalseOrderByIdAsc()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // BUSCAR USUARIO ACTIVO
    // =====================================================

    @Override
    public UsuarioResponseDTO buscarUsuario(Long id) {

        Usuario usuario = repository
                .findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException(id)
                );

        return mapper.toResponseDTO(usuario);
    }

    // =====================================================
    // CREAR USUARIO
    // =====================================================

    @Override
    public UsuarioResponseDTO crearUsuario(
            UsuarioRequestDTO dto
    ) {

        // Regla de negocio: debe tener mínimo 18 años
        validarEdad(dto.getFechaNacimiento());

        // Regla de negocio: teléfono no repetido
        if (repository.existsByTelefono(dto.getTelefono())) {
            throw new TelefonoDuplicadoException();
        }

        // Consultamos estado y municipio usando Postalia
        PostaliaResponse info =
                postaliaService.consultarCodigoPostal(
                        dto.getCodigoPostal()
                );

        // DTO -> Entity
        Usuario usuario = mapper.toEntity(dto);

        // Estado y municipio NO vienen del frontend
        usuario.setEstado(info.getEstado());
        usuario.setMunicipio(info.getMunicipio());

        // Todo usuario nuevo inicia activo
        usuario.setActivo(true);

        Usuario guardado =
                repository.save(usuario);

        // Entity -> ResponseDTO
        return mapper.toResponseDTO(guardado);
    }

    // =====================================================
    // ACTUALIZAR USUARIO
    // =====================================================

    @Override
    public UsuarioResponseDTO actualizarUsuario(
            Long id,
            UsuarioRequestDTO dto
    ) {

        // Solamente permite actualizar usuarios activos
        Usuario existente = repository
                .findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException(id)
                );

        validarEdad(dto.getFechaNacimiento());

        /*
         * Si cambia el teléfono:
         *
         * comprobamos que el nuevo teléfono
         * no pertenezca a otro usuario.
         */
        if (!dto.getTelefono().equals(existente.getTelefono())
                && repository.existsByTelefono(dto.getTelefono())) {

            throw new TelefonoDuplicadoException();
        }

        PostaliaResponse info =
                postaliaService.consultarCodigoPostal(
                        dto.getCodigoPostal()
                );

        mapper.updateEntity(dto, existente);

        existente.setEstado(info.getEstado());
        existente.setMunicipio(info.getMunicipio());

        Usuario actualizado =
                repository.save(existente);

        return mapper.toResponseDTO(actualizado);
    }

    // =====================================================
    // BAJA LÓGICA
    // =====================================================

    @Override
    public boolean eliminarUsuario(Long id) {

        /*
         * Solo busca activos.
         *
         * Si ya estaba eliminado, no permitimos
         * "eliminarlo otra vez".
         */
        Usuario usuario = repository
                .findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException(id)
                );

        usuario.setActivo(false);

        repository.save(usuario);

        return true;
    }

    // =====================================================
    // REACTIVAR USUARIO
    // =====================================================

    @Override
    public UsuarioResponseDTO reactivarUsuario(Long id) {

        /*
         * Aquí usamos findById normal porque precisamente
         * necesitamos encontrar registros inactivos.
         */
        Usuario usuario = repository
                .findById(id)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException(id)
                );

        // No tiene sentido reactivar algo que ya está activo
        if (usuario.isActivo()) {
            throw new UsuarioYaActivoException(id);
        }

        usuario.setActivo(true);

        Usuario reactivado =
                repository.save(usuario);

        return mapper.toResponseDTO(reactivado);
    }

    // =====================================================
    // VALIDACIÓN DE EDAD
    // =====================================================

    private void validarEdad(LocalDate fechaNacimiento) {

        LocalDate fechaLimite =
                LocalDate.now().minusYears(18);

        /*
         * Ejemplo:
         *
         * hoy: 13/09/2026
         * límite: 13/09/2008
         *
         * Si nació después de esa fecha,
         * todavía no tiene 18 años.
         */
        if (fechaNacimiento.isAfter(fechaLimite)) {
            throw new MenorDeEdadException();
        }
    }
}