package com.siga.controller;

import com.siga.model.Talhao;
import com.siga.service.TalhaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/talhoes")
@CrossOrigin(origins = "*")
public class TalhaoController {

    private final TalhaoService talhaoService;

    @Autowired
    public TalhaoController(TalhaoService talhaoService) {
        this.talhaoService = talhaoService;
    }

    @GetMapping
    public ResponseEntity<List<Talhao>> listarTalhoes() {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/talhoes");
            List<Talhao> talhoes = talhaoService.buscarTodas();
            System.out.println("✅ Controller: Retornando " + talhoes.size() + " talhões");
            return ResponseEntity.ok(talhoes);
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar talhões: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Talhao> buscarTalhao(@PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/talhoes/" + id);
            Talhao talhao = talhaoService.buscarPorId(id);
            System.out.println("✅ Controller: Retornando talhão: " + talhao.getNome());
            return ResponseEntity.ok(talhao);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Talhão não encontrado: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<List<Talhao>> buscarTalhoesPorFazenda(@PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/talhoes/fazenda/" + fazendaId);
            List<Talhao> talhoes = talhaoService.buscarPorFazendaId(fazendaId);
            System.out.println("✅ Controller: Retornando " + talhoes.size() + " talhões para fazendaId: " + fazendaId);
            return ResponseEntity.ok(talhoes);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID da fazenda inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar talhões por fazenda: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}