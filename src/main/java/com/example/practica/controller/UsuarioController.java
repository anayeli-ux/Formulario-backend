package com.example.practica.controller;

import com.example.practica.dto.CorreoResponseDTO;
import com.example.practica.dto.UsuarioContactosDTO;
import com.example.practica.dto.AdministradorResumenDTO;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.dto.UsuarioResumenDTO;
import com.example.practica.dto.UsuarioSesionDTO;
import com.example.practica.dto.UsuarioUpdateRequestDTO;
import com.example.practica.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
        public ResponseEntity<Page<UsuarioResumenDTO>> listarUsuarios(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        @RequestParam(defaultValue = "") String search) {
                Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 5));
                return ResponseEntity.ok(service.listarResumenes(false, search, pageable));
        }

        // =====================================================
        // LISTAR USUARIOS ELIMINADOS
        // Solo ADMIN
        // =====================================================

        @GetMapping("/eliminados")
        public ResponseEntity<Page<UsuarioResumenDTO>> listarUsuariosEliminados(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        @RequestParam(defaultValue = "") String search) {
                Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 5));
                return ResponseEntity.ok(service.listarResumenes(true, search, pageable));
        }

        @GetMapping("/administradores")
        public ResponseEntity<Page<AdministradorResumenDTO>> listarAdministradores(
                        @RequestParam(defaultValue = "false") boolean eliminados,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 5));
                return ResponseEntity.ok(service.listarAdministradores(eliminados, pageable));
        }

        // =====================================================
        // OBTENER MI PERFIL
        // USER o ADMIN autenticado
        // =====================================================

        @GetMapping("/me")
        public ResponseEntity<UsuarioResponseDTO> obtenerMiPerfil(
                        Authentication authentication) {

                String email = authentication.getName();

                return ResponseEntity.ok(
                                service.obtenerMiPerfil(email));
        }

        @GetMapping("/me/resumen")
        public ResponseEntity<UsuarioSesionDTO> obtenerResumenSesion(Authentication authentication) {
                UsuarioResponseDTO perfil = service.obtenerMiPerfil(authentication.getName());
                String correo = perfil.getCorreos().stream()
                                .filter(item -> "PRINCIPAL".equalsIgnoreCase(item.getTipo()))
                                .map(CorreoResponseDTO::getValor)
                                .findFirst()
                                .orElse("");
                return ResponseEntity.ok(UsuarioSesionDTO.builder()
                                .id(perfil.getId())
                                .nombre(perfil.getNombre())
                                .primerApellido(perfil.getPrimerApellido())
                                .email(correo)
                                .rol(perfil.getRol())
                                .build());
        }

        // =====================================================
        // BUSCAR USUARIO POR ID
        // Solo ADMIN
        // =====================================================

        @GetMapping("/{id}/contactos")
        public ResponseEntity<UsuarioContactosDTO> buscarContactos(@PathVariable Long id) {
                return ResponseEntity.ok(service.buscarContactos(id));
        }

        @GetMapping("/{id}")
        public ResponseEntity<UsuarioResponseDTO> buscarUsuario(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                service.buscarUsuario(id));
        }

        // =====================================================
        // CREAR USUARIO
        // Registro público
        // =====================================================

        @PostMapping
        public ResponseEntity<UsuarioResponseDTO> crearUsuario(
                        @Valid @RequestBody UsuarioRequestDTO usuario) {

                UsuarioResponseDTO creado = service.crearUsuario(usuario);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(creado);
        }

        // =====================================================
        // ACTUALIZAR USUARIO
        // Solo ADMIN
        // =====================================================

        @PutMapping("/{id}")
        public ResponseEntity<Void> actualizarUsuario(
                        @PathVariable Long id,
                        @Valid @RequestBody UsuarioUpdateRequestDTO usuario) {

                service.actualizarUsuario(id, usuario);

                return ResponseEntity.noContent().build();
        }

        // =====================================================
        // BAJA LÓGICA
        // Solo ADMIN
        // =====================================================

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminarUsuario(
                        @PathVariable Long id) {

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
        public ResponseEntity<UsuarioResponseDTO> reactivarUsuario(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                service.reactivarUsuario(id));
        }
}