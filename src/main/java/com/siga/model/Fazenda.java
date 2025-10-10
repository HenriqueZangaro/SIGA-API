package com.siga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fazenda {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("nome")
    private String nome;
    
    @JsonProperty("localizacao")
    private String localizacao;
    
    @JsonProperty("area")
    private Double area;
    
    @JsonProperty("proprietario")
    private String proprietario;
    
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    
    private Integer qtdTalhoes;
    private Timestamp dataCriacao;
    private Timestamp ultimaAtualizacao;
}

