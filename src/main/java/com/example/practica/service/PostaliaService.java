package com.example.practica.service;

import com.example.practica.dto.PostaliaResponse;
import com.example.practica.exception.CodigoPostalNoEncontradoException;
import com.example.practica.exception.PostaliaNoDisponibleException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PostaliaService {

    private static final Logger log =
            LoggerFactory.getLogger(PostaliaService.class);

    private final RestClient restClient;
    private final String apiKey;


    public PostaliaService(
            @Value("${postalia.api.url}") String apiUrl,
            @Value("${postalia.api.key}") String apiKey
    ) {

        this.restClient =
                RestClient.builder()
                        .baseUrl(apiUrl)
                        .defaultHeader(
                                HttpHeaders.ACCEPT,
                                MediaType.APPLICATION_JSON_VALUE
                        )
                        .build();

        this.apiKey = apiKey;
    }


    public PostaliaResponse consultarCodigoPostal(
            String codigoPostal
    ) {

        try {

            log.debug(
                    "Consultando código postal: {}",
                    codigoPostal
            );


            PostaliaResponse respuesta =
                    restClient
                            .get()

                            .uri(
                                    "/{codigoPostal}",
                                    codigoPostal
                            )

                            .accept(
                                    MediaType.APPLICATION_JSON
                            )

                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "Bearer " + apiKey
                            )

                            .retrieve()

                            .body(
                                    PostaliaResponse.class
                            );


            // ==========================================
            // RESPUESTA VACÍA
            // ==========================================

            if (respuesta == null) {

                throw new CodigoPostalNoEncontradoException(
                        codigoPostal
                );
            }


            return respuesta;


        } catch (CodigoPostalNoEncontradoException e) {

            /*
             * Esta excepción ya tiene el significado
             * correcto, por lo tanto NO debemos
             * convertirla en error 503.
             */

            throw e;


        } catch (RestClientResponseException e) {

            log.warn(
                    "Error HTTP de Postalia. Status: {}",
                    e.getStatusCode()
            );


            // ==========================================
            // ERROR 4XX
            // ==========================================

            if (
                    e.getStatusCode()
                            .is4xxClientError()
            ) {

                throw new CodigoPostalNoEncontradoException(
                        codigoPostal
                );
            }


            // ==========================================
            // ERROR 5XX
            // ==========================================

            throw new PostaliaNoDisponibleException();


        } catch (Exception e) {

            log.error(
                    "Error al consultar Postalia: {}",
                    e.getMessage()
            );

            throw new PostaliaNoDisponibleException();
        }
    }
}