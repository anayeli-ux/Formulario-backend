package com.example.practica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PostaliaResponse {

    @JsonProperty("codigo_postal")
    private String codigoPostal;

    private String estado;

    private String municipio;

    private String zona;

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