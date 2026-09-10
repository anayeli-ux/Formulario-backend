package com.example.practica.controller;

import com.example.practica.model.Usuario;
import com.example.practica.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(
            UsuarioService service
    ) {
        this.service = service;
    }


    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {

        return ResponseEntity.ok(
                service.listarUsuarios()
        );
    }


    @GetMapping("/eliminados")
    public ResponseEntity<List<Usuario>>
    listarUsuariosEliminados() {

        return ResponseEntity.ok(
                service.listarUsuariosEliminados()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarUsuario(
            @PathVariable Long id
    ) {

        Usuario usuario =
                service.buscarUsuario(id);

        if (usuario == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(usuario);
    }


    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(
            @RequestBody Usuario usuario
    ) {

        Usuario creado =
                service.crearUsuario(usuario);

        return ResponseEntity.ok(creado);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody Usuario usuario
    ) {

        Usuario actualizado =
                service.actualizarUsuario(
                        id,
                        usuario
                );

        if (actualizado == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                actualizado
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long id
    ) {

        boolean eliminado =
                service.eliminarUsuario(id);

        if (!eliminado) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .noContent()
                .build();
    }
}