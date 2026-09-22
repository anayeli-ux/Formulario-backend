package com.example.practica.controller;

import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    // LISTAR USUARIOS ACTIVOS
    // Solo ADMIN por SecurityConfig
    // =====================================================

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>>
    listarUsuarios() {

        return ResponseEntity.ok(
                service.listarUsuarios()
        );
    }


    // =====================================================
    // LISTAR USUARIOS ELIMINADOS
    // Solo ADMIN
    // =====================================================

    @GetMapping("/eliminados")
    public ResponseEntity<List<UsuarioResponseDTO>>
    listarUsuariosEliminados() {

        return ResponseEntity.ok(
                service.listarUsuariosEliminados()
        );
    }


    // =====================================================
    // OBTENER MI PERFIL
    // USER o ADMIN autenticado
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO>
    obtenerMiPerfil(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(
                service.obtenerMiPerfil(email)
        );
    }


    // =====================================================
    // BUSCAR USUARIO POR ID
    // Solo ADMIN
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
    // CREAR USUARIO
    // Registro público
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
    // ACTUALIZAR USUARIO
    // Solo ADMIN
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
    // BAJA LÓGICA
    // Solo ADMIN
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
    // REACTIVAR USUARIO
    // Solo ADMIN
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