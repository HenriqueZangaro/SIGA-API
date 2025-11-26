package com.siga.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Chamado {
    @JsonProperty("id")
    private String id;

    // Campos obrigatórios
    @JsonProperty("operadorId")
    private String operadorId;

    @JsonProperty("operadorNome")
    private String operadorNome;

    @JsonProperty("userId")
    private String userId; // UID do Firebase Auth

    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("tipo")
    private String tipo; // 'manutencao', 'problema', 'suporte', 'outro'

    @JsonProperty("prioridade")
    private String prioridade; // 'baixa', 'media', 'alta', 'urgente'

    @JsonProperty("status")
    private String status; // 'aberto', 'em_andamento', 'resolvido', 'cancelado'

    @JsonProperty("dataHoraRegistro")
    private Timestamp dataHoraRegistro; // Quando operador criou

    @JsonProperty("proprietarioId")
    private String proprietarioId;

    // Campos opcionais
    @JsonProperty("dataHoraEnvio")
    private Timestamp dataHoraEnvio; // Quando foi enviado ao servidor

    @JsonProperty("localizacao")
    private Localizacao localizacao;

    @JsonProperty("fotos")
    private List<String> fotos; // URLs das fotos

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

    @JsonProperty("responsavelId")
    private String responsavelId; // Admin que assumiu o chamado

    @JsonProperty("responsavelNome")
    private String responsavelNome;

    @JsonProperty("observacoes")
    private List<Observacao> observacoes; // Comentários sobre o chamado

    @JsonProperty("sincronizado")
    private Boolean sincronizado; // Se foi sincronizado (modo offline)

    @JsonProperty("dataCriacao")
    private Object dataCriacao;

    @JsonProperty("ultimaAtualizacao")
    private Object ultimaAtualizacao;

    @JsonProperty("updatedAt")
    private Object updatedAt;

    // Classe interna para localização
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Localizacao {
        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("accuracy")
        private Double accuracy; // Precisão em metros (opcional)

        @JsonProperty("timestamp")
        private Long timestamp; // Timestamp da captura (opcional)
    }

    // Classe interna para observações
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Observacao {
        @JsonProperty("texto")
        private String texto;

        @JsonProperty("autor")
        private String autor;

        @JsonProperty("autorId")
        private String autorId;

        @JsonProperty("data")
        private Timestamp data;
    }
}

