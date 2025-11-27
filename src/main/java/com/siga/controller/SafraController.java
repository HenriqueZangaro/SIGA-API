package com.siga.controller;

import com.siga.model.Safra;
import com.siga.service.AuthService;
import com.siga.service.SafraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/safras")
@CrossOrigin(origins = "*")
public class SafraController {

    private final SafraService safraService;
    private final AuthService authService;

    @Autowired
    public SafraController(SafraService safraService, AuthService authService) {
        this.safraService = safraService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/safras
     * Lista safras com filtro de segurança por proprietário
     */
    @GetMapping
    public ResponseEntity<?> listarSafras(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String proprietarioId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/safras");
            System.out.println("🔐 UID: " + uid);

            List<Safra> safras;

            if (authService.isAdmin(uid)) {
                System.out.println("👑 Admin do site - Acesso total");
                
                if (proprietarioId != null && !proprietarioId.isEmpty()) {
                    safras = safraService.buscarPorProprietarioId(proprietarioId);
                } else {
                    safras = safraService.buscarTodas();
                }
            } else {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || userProprietarioId.isEmpty()) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                
                safras = safraService.buscarPorProprietarioId(userProprietarioId);
            }

            System.out.println("✅ Controller: Retornando " + safras.size() + " safras");
            return ResponseEntity.ok(safras);

        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar safras: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/safras/{id}
     * Busca uma safra específica com verificação de permissão
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarSafra(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/safras/" + id);
            System.out.println("🔐 UID: " + uid);

            Safra safra = safraService.buscarPorId(id);

            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || !userProprietarioId.equals(safra.getProprietarioId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a esta safra"));
                }
            }

            System.out.println("✅ Controller: Retornando safra: " + safra.getNome());
            return ResponseEntity.ok(safra);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/safras/fazenda/{fazendaId}
     * Busca safras de uma fazenda específica
     */
    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<?> buscarSafrasPorFazenda(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/safras/fazenda/" + fazendaId);
            System.out.println("🔐 UID: " + uid);

            List<Safra> safras = safraService.buscarPorFazendaId(fazendaId);
            
            // Filtrar por proprietário se não for admin
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                safras = safras.stream()
                        .filter(s -> userProprietarioId.equals(s.getProprietarioId()))
                        .toList();
            }

            System.out.println("✅ Controller: Retornando " + safras.size() + " safras para fazenda " + fazendaId);
            return ResponseEntity.ok(safras);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}
