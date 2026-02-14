package com.siga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta do endpoint GET /users/me
 * Retorna dados completos do usuário logado baseado na estrutura real do Firestore
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMeResponse {
    
    // Campos básicos do userProfiles
    @JsonProperty("uid")
    private String uid;
    
    @JsonProperty("nomeCompleto")
    private String nomeCompleto;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("telefone")
    private String telefone;
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("permissao")
    private String permissao;
    
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    
    @JsonProperty("proprietarioNome")
    private String proprietarioNome;
    
    @JsonProperty("operadorId")
    private String operadorId;
    
    @JsonProperty("photoURL")
    private String photoURL;
    
    @JsonProperty("bio")
    private String bio;
    
    @JsonProperty("status")
    private String status;
    
    // Campos específicos para proprietários (role "user")
    @JsonProperty("documento")
    private String documento; // CPF ou CNPJ formatado
    
    @JsonProperty("tipo")
    private String tipo; // "PF" ou "PJ"
    
    @JsonProperty("endereco")
    private String endereco; // Endereço completo em uma string
    
    // Campos específicos para operadores (role "operador")
    @JsonProperty("cpf")
    private String cpf; // CPF formatado (apenas para operadores)
    
    // Arrays para operadores (mantidos como Object para flexibilidade)
    @JsonProperty("especialidades")
    private Object especialidades; // Array de strings para operadores
    
    @JsonProperty("fazendaIds")
    private Object fazendaIds; // Array de IDs das fazendas
    
    @JsonProperty("fazendaNomes")
    private Object fazendaNomes; // Array de nomes das fazendas
}