package com.example.practica.mapper;

import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequestDTO dto) {

        return Usuario.builder()
                .nombre(dto.getNombre())
                .primerApellido(dto.getPrimerApellido())
                .telefono(dto.getTelefono())
                .codigoPostal(dto.getCodigoPostal())
                .direccion(dto.getDireccion())
                .fechaNacimiento(dto.getFechaNacimiento())
                .email(dto.getEmail())
                .activo(true)
                .rol("USER")
                .build();
    }

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {

        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .primerApellido(usuario.getPrimerApellido())
                .telefono(usuario.getTelefono())
                .codigoPostal(usuario.getCodigoPostal())
                .estado(usuario.getEstado())
                .municipio(usuario.getMunicipio())
                .direccion(usuario.getDireccion())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .build();
    }

    public void updateEntity(
            UsuarioRequestDTO dto,
            Usuario usuario
    ) {
        usuario.setNombre(dto.getNombre());
        usuario.setPrimerApellido(dto.getPrimerApellido());
        usuario.setTelefono(dto.getTelefono());
        usuario.setCodigoPostal(dto.getCodigoPostal());
        usuario.setDireccion(dto.getDireccion());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setEmail(dto.getEmail());
    }
}