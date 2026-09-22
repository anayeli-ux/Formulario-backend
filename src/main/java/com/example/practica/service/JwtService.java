package com.example.practica.service;

import com.example.practica.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        this.expiration = expiration;
    }


    // =========================================================
    // GENERAR TOKEN
    // =========================================================

    public String generarToken(
            Usuario usuario
    ) {

        Date ahora = new Date();

        Date expiracion =
                new Date(
                        ahora.getTime() + expiration
                );

        return Jwts.builder()

                // Identificador del usuario
                .subject(
                        usuario.getEmail()
                )

                // Rol
                .claim(
                        "rol",
                        usuario
                                .getRol()
                                .getNombre()
                )

                // Fecha de creación
                .issuedAt(ahora)

                // Fecha de expiración
                .expiration(expiracion)

                // Firma
                .signWith(secretKey)

                .compact();
    }


    // =========================================================
    // EXTRAER CLAIMS
    // =========================================================

    private Claims extraerClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    // =========================================================
    // EXTRAER EMAIL
    // =========================================================

    public String extraerEmail(
            String token
    ) {

        return extraerClaims(token)
                .getSubject();
    }


    // =========================================================
    // EXTRAER ROL
    // =========================================================

    public String extraerRol(
            String token
    ) {

        return extraerClaims(token)
                .get(
                        "rol",
                        String.class
                );
    }


    // =========================================================
    // EXTRAER EXPIRACIÓN
    // =========================================================

    public Date extraerExpiracion(
            String token
    ) {

        return extraerClaims(token)
                .getExpiration();
    }


    // =========================================================
    // VERIFICAR EXPIRACIÓN
    // =========================================================

    public boolean estaExpirado(
            String token
    ) {

        return extraerExpiracion(token)
                .before(new Date());
    }


    // =========================================================
    // VALIDAR TOKEN
    // =========================================================

    public boolean esTokenValido(
            String token
    ) {

        try {

            extraerClaims(token);

            return !estaExpirado(token);

        } catch (Exception exception) {

            return false;
        }
    }
}