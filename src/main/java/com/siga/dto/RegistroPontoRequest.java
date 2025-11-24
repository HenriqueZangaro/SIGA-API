package com.siga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de registro de ponto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPontoRequest {
    @JsonProperty("tipo")
    private String tipo; // 'entrada' ou 'saida'
    
    @JsonProperty("localizacao")
    private LocalizacaoDTO localizacao;
    
    @JsonProperty("fazendaId")
    private String fazendaId;
    
    @JsonProperty("observacao")
    private String observacao;
    
    @JsonProperty("dispositivo")
    private String dispositivo;
    
    @JsonProperty("versaoApp")
    private String versaoApp;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalizacaoDTO {
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
        
        @JsonProperty("accuracy")
        private Double accuracy;
        
        @JsonProperty("timestamp")
        private Long timestamp;
    }
}

