package com.siga.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile {
    @JsonProperty("uid")
    private String uid; // UID do Firebase Auth
    
    @JsonProperty("displayName")
    private String displayName;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("photoURL")
    private String photoURL;
    
    @JsonProperty("role")
    private String role; // 'admin', 'user' ou 'operador'
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("bio")
    private String bio;
    
    // Campos para associação com proprietários
    @JsonProperty("permissao")
    private String permissao; // 'dono', 'admin', 'editor', 'visualizador'
    
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    
    // Campos específicos para operadores
    @JsonProperty("operadorId")
    private String operadorId; // Vinculação com a collection operadores
    
    @JsonProperty("mustChangePassword")
    private Boolean mustChangePassword; // Forçar troca de senha no primeiro login
    
    @JsonProperty("ultimaAtualizacao")
    private Object ultimaAtualizacao; // Pode ser String ou Timestamp
    
    @JsonProperty("createdAt")
    private Object createdAt; // Pode ser String ou Timestamp
    
    @JsonProperty("updatedAt")
    private Object updatedAt; // Pode ser String ou Timestamp
}

