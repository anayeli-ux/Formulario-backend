package com.example.practica.mapper;

import com.example.practica.dto.CorreoResponseDTO;
import com.example.practica.dto.DireccionResponseDTO;
import com.example.practica.dto.TelefonoResponseDTO;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.dto.UsuarioResumenDTO;
import com.example.practica.dto.UsuarioUpdateRequestDTO;

import com.example.practica.model.Direccion;
import com.example.practica.model.CodigoPostal;
import com.example.practica.model.Email;
import com.example.practica.model.Telefono;
import com.example.practica.model.Usuario;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioMapper {

    // =========================================================
    // REQUEST -> ENTITY
    // =========================================================

    public Usuario toEntity(UsuarioRequestDTO dto) {

        return Usuario.builder()
                .nombre(dto.getNombre())
                .primerApellido(dto.getPrimerApellido())
                .fechaNacimiento(dto.getFechaNacimiento())
                .password(dto.getPassword())
                .fechaBaja(null)
                .build();
    }


    // =========================================================
    // UPDATE -> ENTITY
    // =========================================================

    public void updateEntity(
            Usuario usuario,
            UsuarioUpdateRequestDTO dto
    ) {

        usuario.setNombre(dto.getNombre());
        usuario.setPrimerApellido(dto.getPrimerApellido());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());

        /*
         * password se modifica en el Service
         * porque ahí se aplica BCrypt.
         *
         * fechaBaja tampoco se modifica aquí.
         */
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    public UsuarioResponseDTO toResponseDTO(
            Usuario usuario
    ) {

        List<TelefonoResponseDTO> telefonos =
                usuario.getTelefonos()
                        .stream()
                        .map(this::telefonoToResponse)
                        .toList();

        List<CorreoResponseDTO> correos =
                usuario.getEmails()
                        .stream()
                        .map(this::correoToResponse)
                        .toList();

        List<DireccionResponseDTO> direcciones =
                usuario.getDirecciones()
                        .stream()
                        .map(this::direccionToResponse)
                        .toList();

        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .primerApellido(usuario.getPrimerApellido())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .fechaBaja(usuario.getFechaBaja())
                .rol(
                        usuario.getRol() != null
                                ? usuario.getRol().getNombre()
                                : null
                )
                .telefonos(telefonos)
                .correos(correos)
                .direcciones(direcciones)
                .build();
    }

    public UsuarioResumenDTO toResumenDTO(Usuario usuario) {
        return UsuarioResumenDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .primerApellido(usuario.getPrimerApellido())
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : null)
                .telefono(usuario.getTelefonos().stream()
                        .filter(contacto -> "PRINCIPAL".equalsIgnoreCase(contacto.getCategoria()))
                        .map(Telefono::getTelefono)
                        .findFirst().orElse(null))
                .correo(usuario.getEmails().stream()
                        .filter(contacto -> "PRINCIPAL".equalsIgnoreCase(contacto.getTipo()))
                        .map(Email::getValor)
                        .findFirst().orElse(null))
                .codigoPostal(usuario.getDirecciones().stream()
                        .filter(direccion -> "PRINCIPAL".equalsIgnoreCase(direccion.getCategoria()))
                        .map(Direccion::getCodigoPostal)
                        .filter(java.util.Objects::nonNull)
                        .map(CodigoPostal::getCodigoPostal)
                        .findFirst().orElse(null))
                .build();
    }

    // =========================================================
    // TELEFONO -> RESPONSE
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
    // CORREO -> RESPONSE
    // =========================================================

    private CorreoResponseDTO correoToResponse(
            Email email
    ) {

        return CorreoResponseDTO.builder()
                .id(email.getId())
                .tipo(email.getTipo())
                .valor(email.getValor())
                .build();
    }


    // =========================================================
    // DIRECCION -> RESPONSE
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
                                ? direccion.getCodigoPostal()
                                .getCodigoPostal()
                                : null
                )
                .build();
    }
}