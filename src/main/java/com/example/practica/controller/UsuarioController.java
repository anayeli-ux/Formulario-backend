package com.example.practica.controller;

import com.example.practica.dto.CorreoResponseDTO;
import com.example.practica.dto.PageResponse;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioResponseDTO;
import com.example.practica.dto.UsuarioResumenDTO;
import com.example.practica.dto.UsuarioSesionDTO;
import com.example.practica.dto.UsuarioUpdateRequestDTO;
import com.example.practica.service.UsuarioService;
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
        public ResponseEntity<PageResponse<UsuarioResumenDTO>> listarUsuarios(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "5") int size,
                @RequestParam(defaultValue = "") String search,
                @RequestParam(defaultValue = "USER") String rol,
                @RequestParam(defaultValue = "false") boolean eliminados) {
                Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 5));
                return ResponseEntity.ok(
                        PageResponse.from(service.listarResumenes(
                                eliminados,
                                search,
                                pageable,
                                obtenerIdRol(rol))));
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
                @Valid @RequestBody UsuarioRequestDTO usuario,
                Authentication authentication) {

                boolean administradorAutorizado = esAdministrador(authentication);
                UsuarioResponseDTO creado = service.crearUsuario(usuario, administradorAutorizado);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(creado);
        }

        // =====================================================
        // ACTUALIZAR USUARIO
        // Solo ADMIN
        // =====================================================

        @PutMapping("/{id}")
        public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
                @PathVariable Long id,
                @Valid @RequestBody UsuarioUpdateRequestDTO usuario,
                Authentication authentication) {

                UsuarioResponseDTO actualizado = service.actualizarUsuario(
                        id,
                        usuario,
                        esAdministrador(authentication));

                return ResponseEntity.ok(actualizado);
        }

        private boolean esAdministrador(Authentication authentication) {
                return authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        }

        private Long obtenerIdRol(String rol) {
                if ("USER".equalsIgnoreCase(rol)) return 1L;
                if ("ADMIN".equalsIgnoreCase(rol)) return 2L;
                throw new IllegalArgumentException("El rol debe ser USER o ADMIN");
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