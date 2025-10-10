package com.siga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Talhao {
    @JsonProperty("id")
    private String id;
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("fazendaId")
    private String fazendaId;
    @JsonProperty("fazendaNome")
    private String fazendaNome;
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    @JsonProperty("area")
    private Double area;
    @JsonProperty("cultura")
    private String cultura;
    @JsonProperty("status")
    private String status;
    @JsonProperty("dataCriacao")
    private Timestamp dataCriacao;
}