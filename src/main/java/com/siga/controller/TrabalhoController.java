package com.siga.controller;

import com.siga.model.Trabalho;
import com.siga.service.AuthService;
import com.siga.service.TrabalhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trabalhos")
@CrossOrigin(origins = "*")
public class TrabalhoController {

    private final TrabalhoService trabalhoService;
    private final AuthService authService;

    @Autowired
    public TrabalhoController(TrabalhoService trabalhoService, AuthService authService) {
        this.trabalhoService = trabalhoService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/trabalhos
     * Lista trabalhos com filtro de segurança por proprietário
     */
    @GetMapping
    public ResponseEntity<?> listarTrabalhos(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String proprietarioId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/trabalhos");
            System.out.println("🔐 UID: " + uid);

            List<Trabalho> trabalhos;

            if (authService.isAdmin(uid)) {
                System.out.println("👑 Admin do site - Acesso total");
                
                if (proprietarioId != null && !proprietarioId.isEmpty()) {
                    trabalhos = trabalhoService.buscarPorProprietarioId(proprietarioId);
                } else {
                    trabalhos = trabalhoService.buscarTodas();
                }
            } else {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || userProprietarioId.isEmpty()) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                
                trabalhos = trabalhoService.buscarPorProprietarioId(userProprietarioId);
            }

            System.out.println("✅ Controller: Retornando " + trabalhos.size() + " trabalhos");
            return ResponseEntity.ok(trabalhos);

        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar trabalhos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/trabalhos/{id}
     * Busca um trabalho específico com verificação de permissão
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarTrabalho(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/trabalhos/" + id);
            System.out.println("🔐 UID: " + uid);

            Trabalho trabalho = trabalhoService.buscarPorId(id);

            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || !userProprietarioId.equals(trabalho.getProprietarioId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a este trabalho"));
                }
            }

            System.out.println("✅ Controller: Retornando trabalho");
            return ResponseEntity.ok(trabalho);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/trabalhos/fazenda/{fazendaId}
     */
    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<?> buscarTrabalhosPorFazenda(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/trabalhos/fazenda/" + fazendaId);

            List<Trabalho> trabalhos = trabalhoService.buscarPorFazendaId(fazendaId);
            
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                trabalhos = trabalhos.stream()
                        .filter(t -> userProprietarioId.equals(t.getProprietarioId()))
                        .toList();
            }

            return ResponseEntity.ok(trabalhos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/trabalhos/talhao/{talhaoId}
     */
    @GetMapping("/talhao/{talhaoId}")
    public ResponseEntity<?> buscarTrabalhosPorTalhao(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String talhaoId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/trabalhos/talhao/" + talhaoId);

            List<Trabalho> trabalhos = trabalhoService.buscarPorTalhaoId(talhaoId);
            
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                trabalhos = trabalhos.stream()
                        .filter(t -> userProprietarioId.equals(t.getProprietarioId()))
                        .toList();
            }

            return ResponseEntity.ok(trabalhos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/trabalhos/maquina/{maquinaId}
     */
    @GetMapping("/maquina/{maquinaId}")
    public ResponseEntity<?> buscarTrabalhosPorMaquina(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String maquinaId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/trabalhos/maquina/" + maquinaId);

            List<Trabalho> trabalhos = trabalhoService.buscarPorMaquinaId(maquinaId);
            
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                trabalhos = trabalhos.stream()
                        .filter(t -> userProprietarioId.equals(t.getProprietarioId()))
                        .toList();
            }

            return ResponseEntity.ok(trabalhos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/trabalhos/operador/{operadorId}
     */
    @GetMapping("/operador/{operadorId}")
    public ResponseEntity<?> buscarTrabalhosPorOperador(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String operadorId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/trabalhos/operador/" + operadorId);

            List<Trabalho> trabalhos = trabalhoService.buscarPorOperadorId(operadorId);
            
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                trabalhos = trabalhos.stream()
                        .filter(t -> userProprietarioId.equals(t.getProprietarioId()))
                        .toList();
            }

            return ResponseEntity.ok(trabalhos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/trabalhos/safra/{safraId}
     */
    @GetMapping("/safra/{safraId}")
    public ResponseEntity<?> buscarTrabalhosPorSafra(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String safraId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/trabalhos/safra/" + safraId);

            List<Trabalho> trabalhos = trabalhoService.buscarPorSafraId(safraId);
            
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                trabalhos = trabalhos.stream()
                        .filter(t -> userProprietarioId.equals(t.getProprietarioId()))
                        .toList();
            }

            return ResponseEntity.ok(trabalhos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}
