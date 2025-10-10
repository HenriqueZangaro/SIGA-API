package com.siga.controller;

import com.siga.model.Safra;
import com.siga.service.SafraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/safras")
@CrossOrigin(origins = "*")
public class SafraController {

    private final SafraService safraService;

    @Autowired
    public SafraController(SafraService safraService) {
        this.safraService = safraService;
    }

    @GetMapping
    public ResponseEntity<List<Safra>> listarSafras() {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/safras");
            List<Safra> safras = safraService.buscarTodas();
            System.out.println("✅ Controller: Retornando " + safras.size() + " safras");
            return ResponseEntity.ok(safras);
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar safras: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Safra> buscarSafra(@PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/safras/" + id);
            Safra safra = safraService.buscarPorId(id);
            System.out.println("✅ Controller: Retornando safra: " + safra.getNome());
            return ResponseEntity.ok(safra);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Safra não encontrada: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<List<Safra>> buscarSafrasPorFazenda(@PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/safras/fazenda/" + fazendaId);
            List<Safra> safras = safraService.buscarPorFazendaId(fazendaId);
            System.out.println("✅ Controller: Retornando " + safras.size() + " safras para fazendaId: " + fazendaId);
            return ResponseEntity.ok(safras);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID da fazenda inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar safras por fazenda: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}