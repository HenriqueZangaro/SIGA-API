package com.siga.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para autenticação de operador
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperadorAuth {
    private String email;
    private String senha;
}

