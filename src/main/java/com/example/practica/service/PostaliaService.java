package com.example.practica.service;

import com.example.practica.dto.PostaliaResponse;
import com.example.practica.exception.CodigoPostalNoEncontradoException;
import com.example.practica.exception.PostaliaNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PostaliaService {

    private final RestClient restClient;
    private final String apiKey;

    public PostaliaService(
            @Value("${postalia.api.url}") String apiUrl,
            @Value("${postalia.api.key}") String apiKey) {

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .build();

        this.apiKey = apiKey;
    }

    public PostaliaResponse consultarCodigoPostal(String codigoPostal) {
        try {
            return restClient
                    .get()
                    .uri("/{codigoPostal}", codigoPostal)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(PostaliaResponse.class);

        } catch (RestClientResponseException e) {
            // Postalia SÍ respondió, pero con un código de error
            HttpStatusCode status = e.getStatusCode();

            if (status.is4xxClientError()) {
                // 400/404 → el código postal no existe o la petición está mal formada
                throw new CodigoPostalNoEncontradoException(codigoPostal);
            }

            // 5xx → Postalia tuvo un error interno de su lado
            throw new PostaliaNoDisponibleException();

        } catch (RestClientException e) {
            // Postalia no respondió: timeout, conexión rechazada, DNS, etc.
            throw new PostaliaNoDisponibleException();
        }
    }
}