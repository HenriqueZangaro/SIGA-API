package com.siga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.google.cloud.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Maquina {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("nome")
    private String nome;
    
    @JsonProperty("fazendaIds")
    private List<String> fazendaIds;
    
    @JsonProperty("fazendaNomes")
    private List<String> fazendaNomes;
    
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    
    @JsonProperty("proprietarioNome")
    private String proprietarioNome;
    
    @JsonProperty("finalidade")
    private String finalidade;
    
    @JsonProperty("marca")
    private String marca;
    
    @JsonProperty("tipo")
    private String tipo;
    
    @JsonProperty("modelo")
    private String modelo;
    
    @JsonProperty("dataCriacao")
    private Timestamp dataCriacao;
    
    @JsonProperty("ultimaAtualizacao")
    private Timestamp ultimaAtualizacao;
    
    @JsonProperty("ultimaManutencaoHoras")
    private Long ultimaManutencaoHoras;
    
    @JsonProperty("proximaManutencaoHoras")
    private Long proximaManutencaoHoras;
    
    @JsonProperty("horasAtuais")
    private Long horasAtuais;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("intervaloManutencao")
    private Integer intervaloManutencao;
}