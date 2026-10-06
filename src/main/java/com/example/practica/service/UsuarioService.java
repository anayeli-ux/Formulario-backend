package com.example.practica.service;

import com.example.practica.dto.UsuarioUpdateRequestDTO;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.dto.UsuarioResumenDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {
    Page<UsuarioResumenDTO> listarResumenes(boolean eliminados, String busqueda, Pageable pageable, Long rolId);

    UsuarioResponseDTO buscarUsuario(Long id);

    // =====================================================
    // OBTENER MIS DATOS
    // =====================================================

    UsuarioResponseDTO obtenerMiPerfil(String email);

    UsuarioResponseDTO crearUsuario(
            UsuarioRequestDTO usuario
    );

        UsuarioResponseDTO crearUsuario(
            UsuarioRequestDTO usuario,
            boolean administradorAutorizado
        );

        UsuarioResponseDTO actualizarUsuario(
            Long id,
            UsuarioUpdateRequestDTO usuario
    );

        UsuarioResponseDTO actualizarUsuario(
            Long id,
            UsuarioUpdateRequestDTO usuario,
            boolean administradorAutorizado
        );

    boolean eliminarUsuario(Long id);

    UsuarioResponseDTO reactivarUsuario(Long id);
}