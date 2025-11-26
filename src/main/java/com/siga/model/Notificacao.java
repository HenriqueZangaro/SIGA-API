package com.siga.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Modelo de Notificação
 * 
 * Tipos: info, sucesso, alerta, erro
 * Categorias: chamado, sistema, ponto, geral
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Notificacao {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("userId")
    private String userId; // UID do destinatário
    
    @JsonProperty("titulo")
    private String titulo;
    
    @JsonProperty("mensagem")
    private String mensagem;
    
    @JsonProperty("tipo")
    private String tipo; // info, sucesso, alerta, erro
    
    @JsonProperty("categoria")
    private String categoria; // chamado, sistema, ponto, geral
    
    @JsonProperty("lida")
    private Boolean lida;
    
    @JsonProperty("dados")
    private Map<String, Object> dados; // Dados extras (chamadoId, prioridade, etc)
    
    @JsonProperty("createdAt")
    private Object createdAt;
    
    @JsonProperty("updatedAt")
    private Object updatedAt;
    
    /**
     * Construtor de cópia para criar notificações para múltiplos usuários
     */
    public Notificacao(Notificacao other) {
        this.titulo = other.titulo;
        this.mensagem = other.mensagem;
        this.tipo = other.tipo;
        this.categoria = other.categoria;
        this.dados = other.dados;
        this.lida = false;
    }
}

