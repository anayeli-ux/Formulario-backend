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

    private static final Logger log = LoggerFactory.getLogger(PostaliaService.class);

    private final RestClient restClient;
    private final String apiKey;

    public PostaliaService(
            @Value("${postalia.api.url}") String apiUrl,
            @Value("${postalia.api.key}") String apiKey) {

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();

        this.apiKey = apiKey;
    }

    public PostaliaResponse consultarCodigoPostal(String codigoPostal) {

        try {

            log.debug("=== LLAMADA REAL A POSTALIA ===");
            log.debug("CP: [{}]", codigoPostal);
            log.debug("API KEY cargada: {}", apiKey != null && !apiKey.isBlank());

            PostaliaResponse respuesta = restClient
                    .get()
                    .uri("/{codigoPostal}", codigoPostal)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + apiKey
                    )
                    .retrieve()
                    .body(PostaliaResponse.class);

            log.debug("=== RESPUESTA POSTALIA ===");
            log.debug("{}", respuesta);

            if (respuesta == null) {
                throw new CodigoPostalNoEncontradoException(codigoPostal);
            }

            return respuesta;

        } catch (RestClientResponseException e) {

            log.warn("=== ERROR HTTP POSTALIA ===");
            log.warn("Status: {}", e.getStatusCode());
            log.warn("Body: {}", e.getResponseBodyAsString());

            if (e.getStatusCode().is4xxClientError()) {
                throw new CodigoPostalNoEncontradoException(codigoPostal);
            }

            throw new PostaliaNoDisponibleException();

        } catch (Exception e) {

            log.error("=== ERROR POSTALIA ===");
            log.error("Tipo: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());

            throw new PostaliaNoDisponibleException();
        }
    }
}