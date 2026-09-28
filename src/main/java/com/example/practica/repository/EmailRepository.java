package com.example.practica.repository;

import com.example.practica.model.Email;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailRepository
        extends JpaRepository<Email, Long> {

    List<Email> findByUsuarioId(Long usuarioId);

    boolean existsByValorIgnoreCase(String valor);

    Optional<Email> findByValorIgnoreCase(String valor);

    @EntityGraph(attributePaths = {
            "usuario",
            "usuario.rol"
    })
    Optional<Email> findByValorIgnoreCaseAndTipoIgnoreCase(
            String valor,
            String tipo
    );
}