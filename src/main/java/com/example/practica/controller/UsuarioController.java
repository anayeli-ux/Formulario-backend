package com.example.practica.controller;

import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // =====================================================
    // LISTAR ACTIVOS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>>
    listarUsuarios() {

        return ResponseEntity.ok(
                service.listarUsuarios()
        );
    }

    // =====================================================
    // LISTAR ELIMINADOS
    // =====================================================

    @GetMapping("/eliminados")
    public ResponseEntity<List<UsuarioResponseDTO>>
    listarUsuariosEliminados() {

        return ResponseEntity.ok(
                service.listarUsuariosEliminados()
        );
    }

    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO>
    buscarUsuario(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                service.buscarUsuario(id)
        );
    }

    // =====================================================
    // CREAR
    // =====================================================

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO>
    crearUsuario(
            @Valid
            @RequestBody
            UsuarioRequestDTO usuario
    ) {

        UsuarioResponseDTO creado =
                service.crearUsuario(usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // =====================================================
    // ACTUALIZAR
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO>
    actualizarUsuario(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UsuarioRequestDTO usuario
    ) {

        return ResponseEntity.ok(
                service.actualizarUsuario(
                        id,
                        usuario
                )
        );
    }

    // =====================================================
    // ELIMINAR LÓGICAMENTE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    eliminarUsuario(
            @PathVariable Long id
    ) {

        service.eliminarUsuario(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    // =====================================================
    // REACTIVAR
    // =====================================================

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<UsuarioResponseDTO>
    reactivarUsuario(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                service.reactivarUsuario(id)
        );
    }
}