package com.siga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estatísticas de pontos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasPontosResponse {
    @JsonProperty("totalPontos")
    private int totalPontos;
    
    @JsonProperty("totalEntradas")
    private int totalEntradas;
    
    @JsonProperty("totalSaidas")
    private int totalSaidas;
    
    @JsonProperty("horasTrabalhadas")
    private double horasTrabalhadas;
    
    @JsonProperty("diasTrabalhados")
    private int diasTrabalhados;
    
    @JsonProperty("mediaHorasDia")
    private double mediaHorasDia;
}

