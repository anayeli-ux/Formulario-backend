package com.example.practica.service;

import com.example.practica.dto.CorreoRequest;
import com.example.practica.dto.DireccionRequest;
import com.example.practica.dto.TelefonoRequest;
import com.example.practica.dto.UsuarioRequestDTO;
import com.example.practica.dto.UsuarioUpdateRequestDTO;
import com.example.practica.mapper.UsuarioMapper;
import com.example.practica.model.CodigoPostal;
import com.example.practica.model.Direccion;
import com.example.practica.model.Email;
import com.example.practica.model.Telefono;
import com.example.practica.model.Rol;
import com.example.practica.model.Usuario;
import com.example.practica.repository.CodigoPostalRepository;
import com.example.practica.repository.DireccionRepository;
import com.example.practica.repository.EmailRepository;
import com.example.practica.repository.RolRepository;
import com.example.practica.repository.TelefonoRepository;
import com.example.practica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private TelefonoRepository telefonoRepository;
    @Mock
    private DireccionRepository direccionRepository;
    @Mock
    private CodigoPostalRepository codigoPostalRepository;
    @Mock
    private EmailRepository emailRepository;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private PostaliaService postaliaService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(
                usuarioRepository,
                rolRepository,
                telefonoRepository,
                direccionRepository,
                codigoPostalRepository,
                emailRepository,
                usuarioMapper,
                postaliaService,
                passwordEncoder
        );
    }

    @Test
    void reconcilesContactsByIdAndAddsAndRemovesContacts() {
        Usuario usuario = usuarioConContactos();
                Rol rolAdmin = Rol.builder().id(2L).nombre("ADMIN").build();
        Telefono telefonoExistente = usuario.getTelefonos().get(0);
        Email correoExistente = usuario.getEmails().get(0);
        Direccion direccionExistente = usuario.getDirecciones().get(0);
        when(usuarioRepository.findByIdAndFechaBajaIsNull(7L))
                .thenReturn(Optional.of(usuario));
        when(telefonoRepository.existsByTelefono(anyString())).thenReturn(false);
        when(emailRepository.findByValorIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(postaliaService.consultarCodigoPostal("22222")).thenReturn(null);
        when(codigoPostalRepository.findById("22222"))
                .thenReturn(Optional.of(CodigoPostal.builder().codigoPostal("22222").build()));
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolAdmin));
        UsuarioUpdateRequestDTO request = requestConContactos();
        request.setRol("ADMIN");

        service.actualizarUsuario(7L, request, true);

        assertSame(rolAdmin, usuario.getRol());
        assertEquals(2, usuario.getTelefonos().size());
        assertSame(telefonoExistente, usuario.getTelefonos().stream()
                .filter(contacto -> Objects.equals(contacto.getId(), 11L)).findFirst().orElseThrow());
        assertEquals("2222222222", usuario.getTelefonos().get(0).getTelefono());
        assertEquals("SECUNDARIO", telefonoExistente.getCategoria());
        assertEquals(2, usuario.getEmails().size());
        assertSame(correoExistente, usuario.getEmails().stream()
                .filter(contacto -> Objects.equals(contacto.getId(), 21L)).findFirst().orElseThrow());
        assertEquals("nuevo@example.com", usuario.getEmails().get(0).getValor());
        assertEquals("SECUNDARIO", correoExistente.getTipo());
        assertEquals(2, usuario.getDirecciones().size());
        assertSame(direccionExistente, usuario.getDirecciones().stream()
                .filter(contacto -> Objects.equals(contacto.getId(), 31L)).findFirst().orElseThrow());
        assertEquals("Calle Nueva 10", usuario.getDirecciones().get(0).getDireccion());
        assertEquals("22222", usuario.getDirecciones().get(0).getCodigoPostal().getCodigoPostal());
        assertEquals("SECUNDARIO", direccionExistente.getCategoria());
        assertEquals(0, usuario.getTelefonos().stream()
                .filter(contacto -> Objects.equals(contacto.getId(), 12L)).count());
        assertEquals(0, usuario.getEmails().stream()
                .filter(contacto -> Objects.equals(contacto.getId(), 22L)).count());
        assertEquals(0, usuario.getDirecciones().stream()
                .filter(contacto -> Objects.equals(contacto.getId(), 32L)).count());
        assertEquals(1, usuario.getTelefonos().stream().filter(contacto -> contacto.getId() == null).count());
        assertEquals(1, usuario.getEmails().stream().filter(contacto -> contacto.getId() == null).count());
        assertEquals(1, usuario.getDirecciones().stream().filter(contacto -> contacto.getId() == null).count());
    }

    @Test
    void publicRegistrationCannotPromoteItselfEvenWhenRequestContainsAdminRole() {
        Rol rolUser = Rol.builder().id(1L).nombre("USER").build();
        Rol rolAdmin = Rol.builder().id(2L).nombre("ADMIN").build();
        CodigoPostal codigoPostal = CodigoPostal.builder().codigoPostal("42000").build();
        Usuario usuario = Usuario.builder()
                .telefonos(new ArrayList<>())
                .emails(new ArrayList<>())
                .direcciones(new ArrayList<>())
                .build();
        Usuario usuarioAdmin = Usuario.builder()
                .telefonos(new ArrayList<>())
                .emails(new ArrayList<>())
                .direcciones(new ArrayList<>())
                .build();
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nombre("Ana")
                .primerApellido("Perez")
                .password("Password!1")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .rol("ADMIN")
                .telefonos(List.of(TelefonoRequest.builder().tipo("PRINCIPAL").valor("7711234567").build()))
                .correos(List.of(CorreoRequest.builder().tipo("PRINCIPAL").valor("ana@example.com").build()))
                .direcciones(List.of(DireccionRequest.builder().tipo("PRINCIPAL")
                        .valor("Calle Principal 1").codigoPostal("42000").build()))
                .build();

        when(rolRepository.findByNombre("USER")).thenReturn(Optional.of(rolUser));
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolAdmin));
        when(usuarioMapper.toEntity(request)).thenReturn(usuario, usuarioAdmin);
        when(telefonoRepository.existsByTelefono("7711234567")).thenReturn(false);
        when(emailRepository.existsByValorIgnoreCase("ana@example.com")).thenReturn(false);
        when(postaliaService.consultarCodigoPostal("42000")).thenReturn(null);
        when(codigoPostalRepository.findById("42000")).thenReturn(Optional.of(codigoPostal));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
                when(usuarioRepository.save(usuarioAdmin)).thenReturn(usuarioAdmin);

        service.crearUsuario(request);

        assertSame(rolUser, usuario.getRol());
        verify(rolRepository).findByNombre("USER");

                service.crearUsuario(request, true);

                assertSame(rolAdmin, usuarioAdmin.getRol());
    }

    @Test
    void rejectsContactIdThatDoesNotBelongToUser() {
        Usuario usuario = usuarioConContactos();
        when(usuarioRepository.findByIdAndFechaBajaIsNull(7L))
                .thenReturn(Optional.of(usuario));
        UsuarioUpdateRequestDTO dto = requestConContactos();
        dto.setCorreos(List.of(
                CorreoRequest.builder().id(999L).tipo("PRINCIPAL").valor("correo@example.com").build()
        ));

        assertThrows(IllegalArgumentException.class, () -> service.actualizarUsuario(7L, dto));
    }

        @Test
        void queriesPaginatedUserSummariesByRoleAndDeletionStatus() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Usuario> emptyPage = Page.empty(pageable);
        when(usuarioRepository.findByRolIdAndFechaBajaIsNullOrderByIdAsc(1L, pageable))
                .thenReturn(emptyPage);
        when(usuarioRepository.findByRolIdAndFechaBajaIsNotNullOrderByIdAsc(1L, pageable))
                .thenReturn(emptyPage);
        when(usuarioRepository.findByRolIdAndFechaBajaIsNullOrderByIdAsc(2L, pageable))
                .thenReturn(emptyPage);
        when(usuarioRepository.findByRolIdAndFechaBajaIsNotNullOrderByIdAsc(2L, pageable))
                .thenReturn(emptyPage);
        service.listarResumenes(false, "", pageable, 1L);
        service.listarResumenes(true, "", pageable, 1L);
        service.listarResumenes(false, "", pageable, 2L);
        service.listarResumenes(true, "", pageable, 2L);

        verify(usuarioRepository).findByRolIdAndFechaBajaIsNullOrderByIdAsc(1L, pageable);
        verify(usuarioRepository).findByRolIdAndFechaBajaIsNotNullOrderByIdAsc(1L, pageable);
        verify(usuarioRepository).findByRolIdAndFechaBajaIsNullOrderByIdAsc(2L, pageable);
        verify(usuarioRepository).findByRolIdAndFechaBajaIsNotNullOrderByIdAsc(2L, pageable);
        }

    private Usuario usuarioConContactos() {
        Usuario usuario = Usuario.builder()
                .id(7L)
                .nombre("Nombre")
                .primerApellido("Apellido")
                .fechaNacimiento(LocalDate.of(1980, 1, 1))
                .password("hash")
                .telefonos(new ArrayList<>())
                .emails(new ArrayList<>())
                .direcciones(new ArrayList<>())
                .build();

        CodigoPostal codigoPostal = CodigoPostal.builder().codigoPostal("11111").build();
        usuario.getTelefonos().add(Telefono.builder()
                .id(11L).usuario(usuario).categoria("PRINCIPAL").telefono("1111111111").build());
        usuario.getTelefonos().add(Telefono.builder()
                .id(12L).usuario(usuario).categoria("SECUNDARIO").telefono("3333333333").build());
        usuario.getEmails().add(Email.builder()
                .id(21L).usuario(usuario).tipo("PRINCIPAL").valor("anterior@example.com").build());
        usuario.getEmails().add(Email.builder()
                .id(22L).usuario(usuario).tipo("SECUNDARIO").valor("quitar@example.com").build());
        usuario.getDirecciones().add(Direccion.builder()
                .id(31L).usuario(usuario).categoria("PRINCIPAL")
                .direccion("Calle Vieja 1").codigoPostal(codigoPostal).build());
        usuario.getDirecciones().add(Direccion.builder()
                .id(32L).usuario(usuario).categoria("SECUNDARIO")
                .direccion("Calle a eliminar 2").codigoPostal(codigoPostal).build());
        return usuario;
    }

    private UsuarioUpdateRequestDTO requestConContactos() {
        return UsuarioUpdateRequestDTO.builder()
                .nombre("Nombre")
                .primerApellido("Apellido")
                .fechaNacimiento(LocalDate.of(1980, 1, 1))
                .telefonos(List.of(
                        TelefonoRequest.builder().id(11L).tipo("SECUNDARIO").valor("2222222222").build(),
                        TelefonoRequest.builder().tipo("PRINCIPAL").valor("4444444444").build()
                ))
                .correos(List.of(
                        CorreoRequest.builder().id(21L).tipo("SECUNDARIO").valor("nuevo@example.com").build(),
                        CorreoRequest.builder().tipo("PRINCIPAL").valor("otro@example.com").build()
                ))
                .direcciones(List.of(
                        DireccionRequest.builder().id(31L).tipo("SECUNDARIO")
                                .valor("Calle Nueva 10").codigoPostal("22222").build(),
                        DireccionRequest.builder().tipo("PRINCIPAL")
                                .valor("Calle Nueva 20").codigoPostal("22222").build()
                ))
                .build();
    }
}
