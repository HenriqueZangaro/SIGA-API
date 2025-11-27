package com.siga.controller;

import com.siga.model.Talhao;
import com.siga.service.AuthService;
import com.siga.service.TalhaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/talhoes")
@CrossOrigin(origins = "*")
public class TalhaoController {

    private final TalhaoService talhaoService;
    private final AuthService authService;

    @Autowired
    public TalhaoController(TalhaoService talhaoService, AuthService authService) {
        this.talhaoService = talhaoService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/talhoes
     * Lista talhões com filtro de segurança por proprietário
     */
    @GetMapping
    public ResponseEntity<?> listarTalhoes(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String proprietarioId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/talhoes");
            System.out.println("🔐 UID: " + uid);

            List<Talhao> talhoes;

            if (authService.isAdmin(uid)) {
                System.out.println("👑 Admin do site - Acesso total");
                
                if (proprietarioId != null && !proprietarioId.isEmpty()) {
                    talhoes = talhaoService.buscarPorProprietarioId(proprietarioId);
                } else {
                    talhoes = talhaoService.buscarTodas();
                }
            } else {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || userProprietarioId.isEmpty()) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                
                talhoes = talhaoService.buscarPorProprietarioId(userProprietarioId);
            }

            System.out.println("✅ Controller: Retornando " + talhoes.size() + " talhões");
            return ResponseEntity.ok(talhoes);

        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar talhões: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/talhoes/{id}
     * Busca um talhão específico com verificação de permissão
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarTalhao(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/talhoes/" + id);
            System.out.println("🔐 UID: " + uid);

            Talhao talhao = talhaoService.buscarPorId(id);

            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || !userProprietarioId.equals(talhao.getProprietarioId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a este talhão"));
                }
            }

            System.out.println("✅ Controller: Retornando talhão: " + talhao.getNome());
            return ResponseEntity.ok(talhao);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/talhoes/fazenda/{fazendaId}
     * Busca talhões de uma fazenda específica
     */
    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<?> buscarTalhoesPorFazenda(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/talhoes/fazenda/" + fazendaId);
            System.out.println("🔐 UID: " + uid);

            // Verificar se o usuário tem acesso à fazenda
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                // A validação será feita pelo resultado - se não tiver acesso, retorna vazio
            }

            List<Talhao> talhoes = talhaoService.buscarPorFazendaId(fazendaId);
            
            // Filtrar por proprietário se não for admin
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                talhoes = talhoes.stream()
                        .filter(t -> userProprietarioId.equals(t.getProprietarioId()))
                        .toList();
            }

            System.out.println("✅ Controller: Retornando " + talhoes.size() + " talhões para fazenda " + fazendaId);
            return ResponseEntity.ok(talhoes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}
