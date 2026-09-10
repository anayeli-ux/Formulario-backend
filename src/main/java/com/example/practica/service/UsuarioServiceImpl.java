package com.example.practica.service;

import com.example.practica.model.Usuario;
import com.example.practica.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl
        implements UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioServiceImpl(
            UsuarioRepository repository
    ) {
        this.repository = repository;
    }


    @Override
    public List<Usuario> listarUsuarios() {

        return repository
                .findByActivoTrueOrderByIdAsc();
    }


    @Override
    public List<Usuario> listarUsuariosEliminados() {

        return repository
                .findByActivoFalseOrderByIdAsc();
    }


    @Override
    public Usuario buscarUsuario(Long id) {

        return repository
                .findById(id)
                .orElse(null);
    }


    @Override
    public Usuario crearUsuario(
            Usuario usuario
    ) {

        usuario.setId(null);

        usuario.setActivo(true);

        return repository.save(usuario);
    }


    @Override
    public Usuario actualizarUsuario(
            Long id,
            Usuario datos
    ) {

        Usuario usuario =
                buscarUsuario(id);

        if (usuario == null) {
            return null;
        }


        usuario.setNombre(
                datos.getNombre()
        );

        usuario.setPrimerApellido(
                datos.getPrimerApellido()
        );

        usuario.setSegundoApellido(
                datos.getSegundoApellido()
        );

        usuario.setTelefono(
                datos.getTelefono()
        );

        usuario.setCodigoPostal(
                datos.getCodigoPostal()
        );

        usuario.setEstado(
                datos.getEstado()
        );

        usuario.setMunicipio(
                datos.getMunicipio()
        );

        usuario.setDireccion(
                datos.getDireccion()
        );

        usuario.setFechaNacimiento(
                datos.getFechaNacimiento()
        );

        usuario.setAnimalFavorito(
                datos.getAnimalFavorito()
        );


        return repository.save(usuario);
    }


    @Override
    public boolean eliminarUsuario(
            Long id
    ) {

        Usuario usuario =
                buscarUsuario(id);

        if (usuario == null) {
            return false;
        }

        usuario.setActivo(false);

        repository.save(usuario);

        return true;
    }
}