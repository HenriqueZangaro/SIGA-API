package com.siga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para criar uma notificação
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarNotificacaoRequest {
    
    @JsonProperty("userId")
    private String userId; // UID do destinatário ou "admin" para todos admins
    
    @JsonProperty("titulo")
    private String titulo;
    
    @JsonProperty("mensagem")
    private String mensagem;
    
    @JsonProperty("tipo")
    private String tipo; // info, sucesso, alerta, erro
    
    @JsonProperty("categoria")
    private String categoria; // chamado, sistema, ponto, geral
    
    @JsonProperty("dados")
    private Map<String, Object> dados; // Dados extras (chamadoId, prioridade, etc)
}

