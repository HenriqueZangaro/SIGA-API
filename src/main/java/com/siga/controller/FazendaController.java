package com.siga.controller;

import com.siga.model.Fazenda;
import com.siga.service.FazendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fazendas")
@CrossOrigin(origins = "*")
public class FazendaController {

    private final FazendaService fazendaService;

    @Autowired
    public FazendaController(FazendaService fazendaService) {
        this.fazendaService = fazendaService;
    }

    @GetMapping
    public ResponseEntity<List<Fazenda>> listarFazendas() {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/fazendas");
            List<Fazenda> fazendas = fazendaService.buscarTodas();
            System.out.println("✅ Controller: Retornando " + fazendas.size() + " fazendas");
            return ResponseEntity.ok(fazendas);
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar fazendas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fazenda> buscarFazenda(@PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/fazendas/" + id);
            Fazenda fazenda = fazendaService.buscarPorId(id);
            System.out.println("✅ Controller: Retornando fazenda: " + fazenda.getNome());
            return ResponseEntity.ok(fazenda);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Fazenda não encontrada: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

