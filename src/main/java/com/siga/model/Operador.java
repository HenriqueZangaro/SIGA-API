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
public class Operador {
    @JsonProperty("id")
    private String id;
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("telefone")
    private String telefone;
    @JsonProperty("email")
    private String email;
    @JsonProperty("endereco")
    private String endereco;
    @JsonProperty("fazendaIds")
    private List<String> fazendaIds;
    @JsonProperty("fazendaNomes")
    private List<String> fazendaNomes;
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    @JsonProperty("proprietarioNome")
    private String proprietarioNome;
    @JsonProperty("status")
    private String status;
    @JsonProperty("dataCriacao")
    private Timestamp dataCriacao;
    @JsonProperty("ultimaAtualizacao")
    private Timestamp ultimaAtualizacao;
    @JsonProperty("especialidades")
    private List<String> especialidades;
    @JsonProperty("cpf")
    private String cpf;
    @JsonProperty("dataCadastro")
    private Timestamp dataCadastro;
}