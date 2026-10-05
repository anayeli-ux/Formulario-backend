package com.example.practica.repository;

import com.example.practica.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    List<Usuario> findByFechaBajaIsNullOrderByIdAsc();

    List<Usuario> findByFechaBajaIsNotNullOrderByIdAsc();

    Page<Usuario> findByFechaBajaIsNullOrderByIdAsc(Pageable pageable);

    Page<Usuario> findByFechaBajaIsNotNullOrderByIdAsc(Pageable pageable);

    @Query(value = """
            select distinct u from Usuario u
            left join u.telefonos t
            left join u.emails e
            left join u.direcciones d
            left join d.codigoPostal cp
            where u.fechaBaja is null and (
                cast(u.id as string) like concat('%', :term, '%') or
                lower(u.nombre) like lower(concat('%', :term, '%')) or
                lower(u.primerApellido) like lower(concat('%', :term, '%')) or
                lower(t.telefono) like lower(concat('%', :term, '%')) or
                lower(e.valor) like lower(concat('%', :term, '%')) or
                lower(d.direccion) like lower(concat('%', :term, '%')) or
                cp.codigoPostal like concat('%', :term, '%')
            )
            order by u.id asc
            """,
            countQuery = """
            select count(distinct u.id) from Usuario u
            left join u.telefonos t
            left join u.emails e
            left join u.direcciones d
            left join d.codigoPostal cp
            where u.fechaBaja is null and (
                cast(u.id as string) like concat('%', :term, '%') or
                lower(u.nombre) like lower(concat('%', :term, '%')) or
                lower(u.primerApellido) like lower(concat('%', :term, '%')) or
                lower(t.telefono) like lower(concat('%', :term, '%')) or
                lower(e.valor) like lower(concat('%', :term, '%')) or
                lower(d.direccion) like lower(concat('%', :term, '%')) or
                cp.codigoPostal like concat('%', :term, '%')
            )
            """)
    Page<Usuario> buscarActivos(@Param("term") String term, Pageable pageable);

    @Query(value = """
            select distinct u from Usuario u
            left join u.telefonos t
            left join u.emails e
            left join u.direcciones d
            left join d.codigoPostal cp
            where u.fechaBaja is not null and (
                cast(u.id as string) like concat('%', :term, '%') or
                lower(u.nombre) like lower(concat('%', :term, '%')) or
                lower(u.primerApellido) like lower(concat('%', :term, '%')) or
                lower(t.telefono) like lower(concat('%', :term, '%')) or
                lower(e.valor) like lower(concat('%', :term, '%')) or
                lower(d.direccion) like lower(concat('%', :term, '%')) or
                cp.codigoPostal like concat('%', :term, '%')
            )
            order by u.id asc
            """,
            countQuery = """
            select count(distinct u.id) from Usuario u
            left join u.telefonos t
            left join u.emails e
            left join u.direcciones d
            left join d.codigoPostal cp
            where u.fechaBaja is not null and (
                cast(u.id as string) like concat('%', :term, '%') or
                lower(u.nombre) like lower(concat('%', :term, '%')) or
                lower(u.primerApellido) like lower(concat('%', :term, '%')) or
                lower(t.telefono) like lower(concat('%', :term, '%')) or
                lower(e.valor) like lower(concat('%', :term, '%')) or
                lower(d.direccion) like lower(concat('%', :term, '%')) or
                cp.codigoPostal like concat('%', :term, '%')
            )
            """)
    Page<Usuario> buscarEliminados(@Param("term") String term, Pageable pageable);

    Optional<Usuario> findByIdAndFechaBajaIsNull(Long id);
}