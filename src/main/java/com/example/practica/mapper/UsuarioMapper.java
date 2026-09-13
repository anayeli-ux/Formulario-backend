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
                .segundoApellido(dto.getSegundoApellido())
                .telefono(dto.getTelefono())
                .codigoPostal(dto.getCodigoPostal())
                // estado y municipio se asignan después, en el Service, vía Postalia
                .direccion(dto.getDireccion())
                .fechaNacimiento(dto.getFechaNacimiento())
                .animalFavorito(dto.getAnimalFavorito())
                .activo(true)
                .build();
    }

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .primerApellido(usuario.getPrimerApellido())
                .segundoApellido(usuario.getSegundoApellido())
                .telefono(usuario.getTelefono())
                .codigoPostal(usuario.getCodigoPostal())
                .estado(usuario.getEstado())
                .municipio(usuario.getMunicipio())
                .direccion(usuario.getDireccion())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .animalFavorito(usuario.getAnimalFavorito())
                .activo(usuario.isActivo())
                .build();
    }

    public void updateEntity(UsuarioRequestDTO dto, Usuario usuario) {
        usuario.setNombre(dto.getNombre());
        usuario.setPrimerApellido(dto.getPrimerApellido());
        usuario.setSegundoApellido(dto.getSegundoApellido());
        usuario.setTelefono(dto.getTelefono());
        usuario.setCodigoPostal(dto.getCodigoPostal());
        // estado y municipio se recalculan en el Service si cambia el codigoPostal
        usuario.setDireccion(dto.getDireccion());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setAnimalFavorito(dto.getAnimalFavorito());
    }
}