package com.siga.controller;

import com.siga.model.Maquina;
import com.siga.service.AuthService;
import com.siga.service.MaquinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/maquinas")
@CrossOrigin(origins = "*")
public class MaquinaController {

    private final MaquinaService maquinaService;
    private final AuthService authService;

    @Autowired
    public MaquinaController(MaquinaService maquinaService, AuthService authService) {
        this.maquinaService = maquinaService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/maquinas
     * Lista máquinas com filtro de segurança por proprietário
     */
    @GetMapping
    public ResponseEntity<?> listarMaquinas(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String proprietarioId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/maquinas");
            System.out.println("🔐 UID: " + uid);

            List<Maquina> maquinas;

            if (authService.isAdmin(uid)) {
                System.out.println("👑 Admin do site - Acesso total");
                
                if (proprietarioId != null && !proprietarioId.isEmpty()) {
                    maquinas = maquinaService.buscarPorProprietarioId(proprietarioId);
                } else {
                    maquinas = maquinaService.buscarTodas();
                }
            } else {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || userProprietarioId.isEmpty()) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                
                maquinas = maquinaService.buscarPorProprietarioId(userProprietarioId);
            }

            System.out.println("✅ Controller: Retornando " + maquinas.size() + " máquinas");
            return ResponseEntity.ok(maquinas);

        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar máquinas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/maquinas/{id}
     * Busca uma máquina específica com verificação de permissão
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarMaquina(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/maquinas/" + id);
            System.out.println("🔐 UID: " + uid);

            Maquina maquina = maquinaService.buscarPorId(id);

            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || !userProprietarioId.equals(maquina.getProprietarioId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a esta máquina"));
                }
            }

            System.out.println("✅ Controller: Retornando máquina: " + maquina.getNome());
            return ResponseEntity.ok(maquina);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/maquinas/fazenda/{fazendaId}
     * Busca máquinas de uma fazenda específica
     */
    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<?> buscarMaquinasPorFazenda(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/maquinas/fazenda/" + fazendaId);
            System.out.println("🔐 UID: " + uid);

            List<Maquina> maquinas = maquinaService.buscarPorFazendaId(fazendaId);
            
            // Filtrar por proprietário se não for admin
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                if (userProprietarioId == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                maquinas = maquinas.stream()
                        .filter(m -> userProprietarioId.equals(m.getProprietarioId()))
                        .toList();
            }

            System.out.println("✅ Controller: Retornando " + maquinas.size() + " máquinas para fazenda " + fazendaId);
            return ResponseEntity.ok(maquinas);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}
