package com.siga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siga.model.Ponto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para resposta de status do operador
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusOperadorResponse {
    @JsonProperty("pontoAberto")
    private Ponto pontoAberto;
    
    @JsonProperty("podeRegistrarEntrada")
    private boolean podeRegistrarEntrada;
    
    @JsonProperty("podeRegistrarSaida")
    private boolean podeRegistrarSaida;
    
    @JsonProperty("pontosHoje")
    private List<Ponto> pontosHoje;
    
    @JsonProperty("horasTrabalhadasHoje")
    private double horasTrabalhadasHoje;
    
    @JsonProperty("totalRegistrosHoje")
    private int totalRegistrosHoje;
    
    @JsonProperty("ultimoPonto")
    private Ponto ultimoPonto;
}

