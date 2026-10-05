package com.example.practica.service;

import com.example.practica.dto.UsuarioUpdateRequestDTO;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.dto.UsuarioResumenDTO;
import com.example.practica.dto.UsuarioContactosDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> listarUsuarios();

    List<UsuarioResponseDTO> listarUsuariosEliminados();

        Page<UsuarioResumenDTO> listarResumenes(boolean eliminados, String busqueda, Pageable pageable);

    UsuarioResponseDTO buscarUsuario(Long id);

    UsuarioContactosDTO buscarContactos(Long id);

    // =====================================================
    // OBTENER MIS DATOS
    // =====================================================

    UsuarioResponseDTO obtenerMiPerfil(String email);

    UsuarioResponseDTO crearUsuario(
            UsuarioRequestDTO usuario
    );

    void actualizarUsuario(
            Long id,
            UsuarioUpdateRequestDTO usuario
    );

    boolean eliminarUsuario(Long id);

    UsuarioResponseDTO reactivarUsuario(Long id);
}