package com.example.practica.service;

import com.example.practica.model.Usuario;

import java.util.List;

public interface UsuarioService {

    List<Usuario> listarUsuarios();

    List<Usuario> listarUsuariosEliminados();

    Usuario buscarUsuario(Long id);

    Usuario crearUsuario(Usuario usuario);

    Usuario actualizarUsuario(
            Long id,
            Usuario usuario
    );

    boolean eliminarUsuario(Long id);
}