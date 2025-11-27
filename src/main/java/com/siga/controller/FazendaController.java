package com.siga.controller;

import com.siga.model.Fazenda;
import com.siga.service.AuthService;
import com.siga.service.FazendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fazendas")
@CrossOrigin(origins = "*")
public class FazendaController {

    private final FazendaService fazendaService;
    private final AuthService authService;

    @Autowired
    public FazendaController(FazendaService fazendaService, AuthService authService) {
        this.fazendaService = fazendaService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/fazendas
     * Lista fazendas com filtro de segurança por proprietário
     * - Admin: vê todas (ou filtra por proprietarioId se informado)
     * - User/Operador: vê apenas do seu proprietário
     */
    @GetMapping
    public ResponseEntity<?> listarFazendas(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String proprietarioId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/fazendas");
            System.out.println("🔐 UID: " + uid);

            List<Fazenda> fazendas;

            // Admin do site tem acesso total
            if (authService.isAdmin(uid)) {
                System.out.println("👑 Admin do site - Acesso total");
                
                // Admin pode filtrar opcionalmente por proprietarioId
                if (proprietarioId != null && !proprietarioId.isEmpty()) {
                    fazendas = fazendaService.buscarPorProprietarioId(proprietarioId);
                    System.out.println("🔍 Filtrando por proprietarioId: " + proprietarioId);
                } else {
                    fazendas = fazendaService.buscarTodas();
                }
            } else {
                // User ou Operador: filtrar por proprietarioId do usuário
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || userProprietarioId.isEmpty()) {
                    System.out.println("⚠️ Usuário sem proprietário associado");
                    return ResponseEntity.ok(Collections.emptyList());
                }
                
                System.out.println("🔍 Filtrando por proprietarioId do usuário: " + userProprietarioId);
                fazendas = fazendaService.buscarPorProprietarioId(userProprietarioId);
            }

            System.out.println("✅ Controller: Retornando " + fazendas.size() + " fazendas");
            return ResponseEntity.ok(fazendas);

        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar fazendas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/fazendas/{id}
     * Busca uma fazenda específica com verificação de permissão
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarFazenda(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/fazendas/" + id);
            System.out.println("🔐 UID: " + uid);

            Fazenda fazenda = fazendaService.buscarPorId(id);

            // Verificar permissão (admin ou mesmo proprietário)
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || !userProprietarioId.equals(fazenda.getProprietarioId())) {
                    System.out.println("⚠️ Acesso negado - proprietário diferente");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a esta fazenda"));
                }
            }

            System.out.println("✅ Controller: Retornando fazenda: " + fazenda.getNome());
            return ResponseEntity.ok(fazenda);

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Fazenda não encontrada: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}
