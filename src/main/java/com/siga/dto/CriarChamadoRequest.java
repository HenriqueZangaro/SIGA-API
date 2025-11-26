package com.siga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siga.model.Chamado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarChamadoRequest {
    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("tipo")
    private String tipo; // 'manutencao', 'problema', 'suporte', 'outro'

    @JsonProperty("prioridade")
    private String prioridade; // 'baixa', 'media', 'alta', 'urgente'

    @JsonProperty("localizacao")
    private Chamado.Localizacao localizacao;

    @JsonProperty("fazendaId")
    private String fazendaId;

    @JsonProperty("fazendaNome")
    private String fazendaNome;

    @JsonProperty("talhaoId")
    private String talhaoId;

    @JsonProperty("talhaoNome")
    private String talhaoNome;

    @JsonProperty("maquinaId")
    private String maquinaId;

    @JsonProperty("maquinaNome")
    private String maquinaNome;

    @JsonProperty("sincronizado")
    private Boolean sincronizado;
}

