package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Schema(description = "Información de un código postal")
public class PostaliaResponse {

    @JsonProperty("codigo_postal")
    @Schema(description = "Código postal de cinco dígitos", example = "42000")
    private String codigoPostal;

    @Schema(description = "Estado correspondiente al código postal", example = "Hidalgo")
    private String estado;

    @Schema(description = "Municipio correspondiente al código postal", example = "Pachuca de Soto")
    private String municipio;

    @Schema(description = "Zona del código postal")
    private String zona;

    @Schema(description = "Colonias correspondientes al código postal")
    private List<ColoniaResponse> colonias;

    public PostaliaResponse() {
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public List<ColoniaResponse> getColonias() {
        return colonias;
    }

    public void setColonias(List<ColoniaResponse> colonias) {
        this.colonias = colonias;
    }
}