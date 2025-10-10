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
public class Proprietario {
    @JsonProperty("id")
    private String id;
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("documento")
    private String documento;
    @JsonProperty("telefone")
    private String telefone;
    @JsonProperty("email")
    private String email;
    @JsonProperty("endereco")
    private String endereco;
    @JsonProperty("tipo")
    private String tipo;
    @JsonProperty("status")
    private String status;
    @JsonProperty("cidade")
    private String cidade;
    @JsonProperty("estado")
    private String estado;
    @JsonProperty("cep")
    private String cep;
    @JsonProperty("fazendaIds")
    private List<String> fazendaIds;
    @JsonProperty("fazendaNomes")
    private List<String> fazendaNomes;
    @JsonProperty("qtdFazendas")
    private Integer qtdFazendas;
    @JsonProperty("areaTotal")
    private Double areaTotal;
    @JsonProperty("dataCriacao")
    private Timestamp dataCriacao;
    @JsonProperty("ultimaAtualizacao")
    private Timestamp ultimaAtualizacao;
}