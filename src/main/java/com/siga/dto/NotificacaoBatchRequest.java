package com.siga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para enviar notificações para múltiplos usuários
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoBatchRequest {
    
    @JsonProperty("userIds")
    private List<String> userIds; // Lista de UIDs dos destinatários
    
    @JsonProperty("titulo")
    private String titulo;
    
    @JsonProperty("mensagem")
    private String mensagem;
    
    @JsonProperty("tipo")
    private String tipo; // info, sucesso, alerta, erro
    
    @JsonProperty("categoria")
    private String categoria; // chamado, sistema, ponto, geral
    
    @JsonProperty("dados")
    private Map<String, Object> dados; // Dados extras (opcional)
}

