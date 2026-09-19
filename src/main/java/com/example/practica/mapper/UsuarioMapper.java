package com.example.practica.mapper;

import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.model.Direccion;
import com.example.practica.model.Telefono;
import com.example.practica.model.Usuario;
import org.springframework.stereotype.Component;

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
                .activo(true)
                .tokenVersion(0)
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
         * La contraseña NO se actualiza aquí.
         *
         * El Service será responsable de decidir cuándo
         * cambiarla y de codificarla antes de guardarla.
         */
    }


    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    public UsuarioResponseDTO toResponseDTO(
            Usuario usuario,
            Telefono telefono,
            Direccion direccion,
            String estado,
            String municipio
    ) {

        return UsuarioResponseDTO.builder()

                .id(usuario.getId())

                .nombre(usuario.getNombre())

                .primerApellido(
                        usuario.getPrimerApellido()
                )

                .telefono(
                        telefono != null
                                ? telefono.getTelefono()
                                : null
                )

                .codigoPostal(
                        direccion != null
                                && direccion.getCodigoPostal() != null
                                ? direccion
                                .getCodigoPostal()
                                .getCodigoPostal()
                                : null
                )

                .estado(estado)

                .municipio(municipio)

                .direccion(
                        direccion != null
                                ? direccion.getDireccion()
                                : null
                )

                .fechaNacimiento(
                        usuario.getFechaNacimiento()
                )

                .email(
                        usuario.getEmail()
                )

                .activo(
                        usuario.isActivo()
                )

                .rol(
                        usuario.getRol() != null
                                ? usuario.getRol().getNombre()
                                : null
                )

                .build();
    }
}