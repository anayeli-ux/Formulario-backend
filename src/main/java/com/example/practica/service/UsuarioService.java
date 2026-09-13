package com.example.practica.service;

import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> listarUsuarios();

    List<UsuarioResponseDTO> listarUsuariosEliminados();

    UsuarioResponseDTO buscarUsuario(Long id);

    UsuarioResponseDTO crearUsuario(
            UsuarioRequestDTO usuario
    );

    UsuarioResponseDTO actualizarUsuario(
            Long id,
            UsuarioRequestDTO usuario
    );

    boolean eliminarUsuario(Long id);

    UsuarioResponseDTO reactivarUsuario(Long id);
}