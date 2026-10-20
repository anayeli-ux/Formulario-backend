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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Usuarios", description = "Documentación de endpoints de usuarios")
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

        @Operation(summary = "Listar usuarios", description = "Lista resúmenes de usuarios con paginación, búsqueda, rol y filtro de eliminados. Requiere rol ADMIN.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Listado obtenido"),
                @ApiResponse(responseCode = "400", description = "Parámetros inválidos, incluido un rol no permitido"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
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

        @Operation(summary = "Consultar mi perfil", description = "Obtiene el perfil del usuario autenticado.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Perfil obtenido"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @GetMapping("/me")
        public ResponseEntity<UsuarioResponseDTO> obtenerMiPerfil(
                Authentication authentication) {

                String email = authentication.getName();

                return ResponseEntity.ok(
                        service.obtenerMiPerfil(email));
        }

        @Operation(summary = "Consultar resumen de sesión", description = "Obtiene los datos resumidos de la sesión del usuario autenticado.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Resumen de sesión obtenido"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
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

        @Operation(summary = "Buscar usuario por ID", description = "Consulta un usuario por identificador. Requiere rol ADMIN.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                @ApiResponse(responseCode = "400", description = "Identificador inválido"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
                @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
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

        @Operation(summary = "Registrar usuario", description = "Crea un usuario con datos personales y de contacto. Permite el registro público; la autorización administrativa se comprueba en el servicio.")
        @ApiResponses({
                @ApiResponse(responseCode = "201", description = "Usuario creado"),
                @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario menor de edad"),
                @ApiResponse(responseCode = "409", description = "Correo o teléfono duplicado"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
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

        @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente. Requiere rol ADMIN.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
                @ApiResponse(responseCode = "400", description = "Datos inválidos o identificador incorrecto"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
                @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                @ApiResponse(responseCode = "409", description = "Conflicto de correo o teléfono duplicado, si se valida en el servicio"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
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

        @Operation(summary = "Dar de baja a un usuario", description = "Realiza la baja lógica de un usuario. Requiere rol ADMIN.")
        @ApiResponses({
                @ApiResponse(responseCode = "204", description = "Baja lógica realizada"),
                @ApiResponse(responseCode = "400", description = "Identificador inválido"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
                @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
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

        @Operation(summary = "Reactivar usuario", description = "Reactiva un usuario dado de baja. Requiere rol ADMIN.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Usuario reactivado"),
                @ApiResponse(responseCode = "400", description = "Identificador inválido"),
                @ApiResponse(responseCode = "401", description = "No autenticado"),
                @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
                @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                @ApiResponse(responseCode = "409", description = "Usuario ya activo"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PutMapping("/{id}/reactivar")
        public ResponseEntity<UsuarioResponseDTO> reactivarUsuario(
                @PathVariable Long id) {

                return ResponseEntity.ok(
                        service.reactivarUsuario(id));
        }
}