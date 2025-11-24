package com.siga.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ponto {
    @JsonProperty("id")
    private String id;
    
    // Campos obrigatórios
    @JsonProperty("operadorId")
    private String operadorId;
    
    @JsonProperty("operadorNome")
    private String operadorNome;
    
    @JsonProperty("userId")
    private String userId; // UID do Firebase Auth
    
    @JsonProperty("tipo")
    private String tipo; // 'entrada' ou 'saida'
    
    @JsonProperty("dataHora")
    private Timestamp dataHora; // Hora exata do registro
    
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    
    // Campos opcionais
    @JsonProperty("localizacao")
    private Localizacao localizacao;
    
    @JsonProperty("fazendaId")
    private String fazendaId;
    
    @JsonProperty("fazendaNome")
    private String fazendaNome;
    
    @JsonProperty("observacao")
    private String observacao;
    
    @JsonProperty("pontoEntradaId")
    private String pontoEntradaId; // ID do ponto de entrada (se for saída)
    
    @JsonProperty("duracaoMinutos")
    private Integer duracaoMinutos; // Calculado automaticamente na saída
    
    @JsonProperty("dataCriacao")
    private Timestamp dataCriacao;
    
    @JsonProperty("dispositivo")
    private String dispositivo; // Ex: "Android 12", "iOS 15"
    
    @JsonProperty("versaoApp")
    private String versaoApp; // Ex: "1.0.0"
    
    @JsonProperty("ultimaAtualizacao")
    private Timestamp ultimaAtualizacao; // Campo opcional do Firestore
    
    @JsonProperty("updatedAt")
    private Object updatedAt; // Campo alternativo (pode ser String ou Timestamp)
    
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
}

