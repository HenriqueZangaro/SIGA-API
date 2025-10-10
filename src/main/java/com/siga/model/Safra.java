package com.siga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.cloud.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Safra {
    @JsonProperty("id")
    private String id;
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("fazendaId")
    private String fazendaId;
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    @JsonProperty("proprietarioNome")
    private String proprietarioNome;
    @JsonProperty("cultura")
    private String cultura;
    @JsonProperty("dataPlantio")
    private Timestamp dataPlantio;
    @JsonProperty("dataColheita")
    private Timestamp dataColheita;
    @JsonProperty("status")
    private String status;
    @JsonProperty("observacoes")
    private String observacoes;
    @JsonProperty("dataCriacao")
    private Timestamp dataCriacao;
    @JsonProperty("ultimaAtualizacao")
    private Timestamp ultimaAtualizacao;
    @JsonProperty("fazendaNomes")
    private List<String> fazendaNomes;
    @JsonProperty("fazendaIds")
    private List<String> fazendaIds;
    @JsonProperty("descricao")
    private String descricao;
}