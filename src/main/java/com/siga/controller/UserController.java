package com.siga.controller;

import com.siga.dto.UserMeResponse;
import com.siga.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para operações relacionadas aos usuários
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/v1/users/me
     * Retorna dados completos do usuário logado
     * Requer header: X-User-UID (UID do Firebase Auth)
     * 
     * Funciona para todos os tipos de usuário:
     * - admin: Dados básicos + acesso total
     * - user: Dados do UserProfile + dados do Proprietario
     * - operador: Dados do UserProfile + dados do Operador + dados do Proprietario
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 UserController: Recebida requisição GET /api/v1/users/me");
            System.out.println("🔐 UID: " + uid);
            
            // Validar se UID foi fornecido
            if (uid == null || uid.trim().isEmpty()) {
                System.err.println("❌ UserController: UID não fornecido no header X-User-UID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Header X-User-UID é obrigatório"));
            }
            
            // Buscar dados completos do usuário
            UserMeResponse userData = userService.getUserMeData(uid);
            
            System.out.println("✅ UserController: Dados do usuário retornados com sucesso");
            System.out.println("👤 Usuário: " + userData.getNomeCompleto() + " (" + userData.getRole() + ")");
            
            return ResponseEntity.ok(userData);
            
        } catch (IllegalArgumentException e) {
            System.err.println("❌ UserController: Erro de validação: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("❌ UserController: Erro de negócio: " + e.getMessage());
            
            // Mapear erros específicos para códigos HTTP apropriados
            String errorMessage = e.getMessage();
            if (errorMessage.contains("não encontrado") || errorMessage.contains("não encontrados")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", errorMessage));
            } else if (errorMessage.contains("Erro interno")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("erro", "Erro interno ao buscar dados"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", errorMessage));
            }
        } catch (Exception e) {
            System.err.println("❌ UserController: Erro interno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/users/profile
     * Retorna apenas o UserProfile básico (sem dados enriquecidos)
     * Útil para casos onde só precisa dos dados básicos do Firebase
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 UserController: Recebida requisição GET /api/v1/users/profile");
            System.out.println("🔐 UID: " + uid);
            
            // Validar se UID foi fornecido
            if (uid == null || uid.trim().isEmpty()) {
                System.err.println("❌ UserController: UID não fornecido no header X-User-UID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Header X-User-UID é obrigatório"));
            }
            
            // Buscar apenas o UserProfile
            var userProfile = userService.getUserProfile(uid);
            
            if (userProfile == null) {
                System.err.println("❌ UserController: UserProfile não encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Usuário não encontrado"));
            }
            
            System.out.println("✅ UserController: UserProfile retornado com sucesso");
            
            return ResponseEntity.ok(userProfile);
            
        } catch (Exception e) {
            System.err.println("❌ UserController: Erro interno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/users/exists
     * Verifica se o usuário existe no sistema
     * Útil para validações rápidas
     */
    @GetMapping("/exists")
    public ResponseEntity<?> userExists(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 UserController: Recebida requisição GET /api/v1/users/exists");
            System.out.println("🔐 UID: " + uid);
            
            // Validar se UID foi fornecido
            if (uid == null || uid.trim().isEmpty()) {
                System.err.println("❌ UserController: UID não fornecido no header X-User-UID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Header X-User-UID é obrigatório"));
            }
            
            boolean exists = userService.userExists(uid);
            
            System.out.println("✅ UserController: Verificação de existência concluída: " + exists);
            
            return ResponseEntity.ok(Map.of(
                "exists", exists,
                "uid", uid
            ));
            
        } catch (Exception e) {
            System.err.println("❌ UserController: Erro interno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}