package com.example.practica.repository;

import com.example.practica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    List<Usuario> findByActivoTrueOrderByIdAsc();

    List<Usuario> findByActivoFalseOrderByIdAsc();
}