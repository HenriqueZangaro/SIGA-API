package com.siga.controller;

import com.siga.model.Operador;
import com.siga.service.AuthService;
import com.siga.service.OperadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/operadores")
@CrossOrigin(origins = "*")
public class OperadorController {

    private final OperadorService operadorService;
    private final AuthService authService;

    @Autowired
    public OperadorController(OperadorService operadorService, AuthService authService) {
        this.operadorService = operadorService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/operadores
     * Lista operadores com filtro de segurança por proprietário
     */
    @GetMapping
    public ResponseEntity<?> listarOperadores(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String proprietarioId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/operadores");
            System.out.println("🔐 UID: " + uid);

            List<Operador> operadores;

            if (authService.isAdmin(uid)) {
                System.out.println("👑 Admin do site - Acesso total");
                
                if (proprietarioId != null && !proprietarioId.isEmpty()) {
                    operadores = operadorService.buscarPorProprietarioId(proprietarioId);
                } else {
                    operadores = operadorService.buscarTodas();
                }
            } else {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || userProprietarioId.isEmpty()) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                
                operadores = operadorService.buscarPorProprietarioId(userProprietarioId);
            }

            System.out.println("✅ Controller: Retornando " + operadores.size() + " operadores");
            return ResponseEntity.ok(operadores);

        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar operadores: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/operadores/{id}
     * Busca um operador específico com verificação de permissão
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarOperador(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/operadores/" + id);
            System.out.println("🔐 UID: " + uid);

            Operador operador = operadorService.buscarPorId(id);

            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || !userProprietarioId.equals(operador.getProprietarioId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a este operador"));
                }
            }

            System.out.println("✅ Controller: Retornando operador: " + operador.getNome());
            return ResponseEntity.ok(operador);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/operadores/fazenda/{fazendaId}
     * Busca operadores de uma fazenda específica
     */
    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<?> buscarOperadoresPorFazenda(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/operadores/fazenda/" + fazendaId);
            System.out.println("🔐 UID: " + uid);

            List<Operador> operadores = operadorService.buscarPorFazendaId(fazendaId);
            
            // Filtrar por proprietário se não for admin
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                operadores = operadores.stream()
                        .filter(o -> userProprietarioId.equals(o.getProprietarioId()))
                        .toList();
            }

            System.out.println("✅ Controller: Retornando " + operadores.size() + " operadores para fazenda " + fazendaId);
            return ResponseEntity.ok(operadores);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}
