package com.example.practica.mapper;

import com.example.practica.dto.DireccionResponseDTO;
import com.example.practica.dto.TelefonoResponseDTO;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;

import com.example.practica.model.Direccion;
import com.example.practica.model.Telefono;
import com.example.practica.model.Usuario;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioMapper {

    // =========================================================
    // REQUEST DTO -> ENTITY USUARIO
    // =========================================================

    public Usuario toEntity(UsuarioRequestDTO dto) {

        return Usuario.builder()
                .nombre(dto.getNombre())
                .primerApellido(dto.getPrimerApellido())
                .fechaNacimiento(dto.getFechaNacimiento())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .fechaBaja(null)
                .build();
    }


    // =========================================================
    // ACTUALIZAR ENTITY EXISTENTE
    // =========================================================

    public void updateEntity(
            Usuario usuario,
            UsuarioRequestDTO dto
    ) {

        usuario.setNombre(dto.getNombre());
        usuario.setPrimerApellido(dto.getPrimerApellido());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setEmail(dto.getEmail());

        /*
         * La contraseña no se modifica aquí.
         * El Service se encarga de decidir si debe cambiar
         * y de aplicar BCrypt.
         *
         * fechaBaja tampoco se modifica al editar.
         */
    }


    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    public UsuarioResponseDTO toResponseDTO(
            Usuario usuario,
            String estado,
            String municipio
    ) {

        List<TelefonoResponseDTO> telefonos =
                usuario.getTelefonos()
                        .stream()
                        .map(this::telefonoToResponse)
                        .toList();

        List<DireccionResponseDTO> direcciones =
                usuario.getDirecciones()
                        .stream()
                        .map(this::direccionToResponse)
                        .toList();


        // -----------------------------------------------------
        // Obtener teléfono principal
        // -----------------------------------------------------

        Telefono telefonoPrincipal =
                usuario.getTelefonos()
                        .stream()
                        .filter(t ->
                                "PRINCIPAL".equalsIgnoreCase(
                                        t.getCategoria()
                                )
                        )
                        .findFirst()
                        .orElseGet(() ->
                                usuario.getTelefonos()
                                        .stream()
                                        .findFirst()
                                        .orElse(null)
                        );


        // -----------------------------------------------------
        // Obtener dirección principal
        // -----------------------------------------------------

        Direccion direccionPrincipal =
                usuario.getDirecciones()
                        .stream()
                        .filter(d ->
                                "PRINCIPAL".equalsIgnoreCase(
                                        d.getCategoria()
                                )
                        )
                        .findFirst()
                        .orElseGet(() ->
                                usuario.getDirecciones()
                                        .stream()
                                        .findFirst()
                                        .orElse(null)
                        );


        return UsuarioResponseDTO.builder()

                .id(usuario.getId())

                .nombre(usuario.getNombre())

                .primerApellido(
                        usuario.getPrimerApellido()
                )

                // Campo principal para compatibilidad
                // con el frontend actual.
                .telefono(
                        telefonoPrincipal != null
                                ? telefonoPrincipal.getTelefono()
                                : null
                )

                .codigoPostal(
                        direccionPrincipal != null
                                && direccionPrincipal.getCodigoPostal() != null

                                ? direccionPrincipal
                                .getCodigoPostal()
                                .getCodigoPostal()

                                : null
                )

                .estado(estado)

                .municipio(municipio)

                .direccion(
                        direccionPrincipal != null
                                ? direccionPrincipal.getDireccion()
                                : null
                )

                .fechaNacimiento(
                        usuario.getFechaNacimiento()
                )

                .email(
                        usuario.getEmail()
                )

                .fechaBaja(
                        usuario.getFechaBaja()
                )

                .rol(
                        usuario.getRol() != null
                                ? usuario.getRol().getNombre()
                                : null
                )

                // Nuevas colecciones
                .telefonos(telefonos)

                .direcciones(direcciones)

                .build();
    }


    // =========================================================
    // TELEFONO -> TELEFONO RESPONSE DTO
    // =========================================================

    private TelefonoResponseDTO telefonoToResponse(
            Telefono telefono
    ) {

        return TelefonoResponseDTO.builder()
                .id(telefono.getId())
                .tipo(telefono.getCategoria())
                .valor(telefono.getTelefono())
                .build();
    }


    // =========================================================
    // DIRECCION -> DIRECCION RESPONSE DTO
    // =========================================================

    private DireccionResponseDTO direccionToResponse(
            Direccion direccion
    ) {

        return DireccionResponseDTO.builder()
                .id(direccion.getId())
                .tipo(direccion.getCategoria())
                .valor(direccion.getDireccion())

                .codigoPostal(
                        direccion.getCodigoPostal() != null
                                ? direccion
                                .getCodigoPostal()
                                .getCodigoPostal()
                                : null
                )

                .build();
    }
}