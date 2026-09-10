package com.example.practica.service;

import com.example.practica.dto.PostaliaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

        return restClient
                .get()
                .uri("/{codigoPostal}", codigoPostal)
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .body(PostaliaResponse.class);
    }
}