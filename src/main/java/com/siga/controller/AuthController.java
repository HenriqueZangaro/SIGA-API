package com.siga.controller;

import com.siga.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para autenticação
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * GET /api/v1/auth/me
     * Retorna informações do usuário logado
     * Requer header: X-User-UID (UID do Firebase Auth)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/auth/me");
            System.out.println("🔐 UID: " + uid);
            
            // Buscar informações do operador
            Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
            
            System.out.println("✅ Controller: Informações do operador retornadas");
            
            return ResponseEntity.ok(operadorInfo);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/auth/validate
     * Valida o token do usuário
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/auth/validate");
            System.out.println("🔐 UID: " + uid);
            
            // Tentar buscar informações (se der erro, token inválido)
            authService.getOperadorInfo(uid);
            
            System.out.println("✅ Controller: Token válido");
            
            return ResponseEntity.ok(Map.of(
                "valido", true,
                "mensagem", "Token válido"
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Token inválido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "valido", false,
                        "erro", e.getMessage()
                    ));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}

