package com.siga.controller;

import com.siga.model.Trabalho;
import com.siga.service.TrabalhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trabalhos")
@CrossOrigin(origins = "*")
public class TrabalhoController {

    private final TrabalhoService trabalhoService;

    @Autowired
    public TrabalhoController(TrabalhoService trabalhoService) {
        this.trabalhoService = trabalhoService;
    }

    @GetMapping
    public ResponseEntity<List<Trabalho>> listarTrabalhos() {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/trabalhos");
            List<Trabalho> trabalhos = trabalhoService.buscarTodas();
            System.out.println("✅ Controller: Retornando " + trabalhos.size() + " trabalhos");
            return ResponseEntity.ok(trabalhos);
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar trabalhos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trabalho> buscarTrabalho(@PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/trabalhos/" + id);
            Trabalho trabalho = trabalhoService.buscarPorId(id);
            System.out.println("✅ Controller: Retornando trabalho: " + trabalho.getTipoTrabalho() + " - " + trabalho.getFazendaNome());
            return ResponseEntity.ok(trabalho);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Trabalho não encontrado: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<List<Trabalho>> buscarTrabalhosPorFazenda(@PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/trabalhos/fazenda/" + fazendaId);
            List<Trabalho> trabalhos = trabalhoService.buscarPorFazendaId(fazendaId);
            System.out.println("✅ Controller: Retornando " + trabalhos.size() + " trabalhos para fazendaId: " + fazendaId);
            return ResponseEntity.ok(trabalhos);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID da fazenda inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar trabalhos por fazenda: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/talhao/{talhaoId}")
    public ResponseEntity<List<Trabalho>> buscarTrabalhosPorTalhao(@PathVariable String talhaoId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/trabalhos/talhao/" + talhaoId);
            List<Trabalho> trabalhos = trabalhoService.buscarPorTalhaoId(talhaoId);
            System.out.println("✅ Controller: Retornando " + trabalhos.size() + " trabalhos para talhaoId: " + talhaoId);
            return ResponseEntity.ok(trabalhos);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID do talhão inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar trabalhos por talhão: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/maquina/{maquinaId}")
    public ResponseEntity<List<Trabalho>> buscarTrabalhosPorMaquina(@PathVariable String maquinaId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/trabalhos/maquina/" + maquinaId);
            List<Trabalho> trabalhos = trabalhoService.buscarPorMaquinaId(maquinaId);
            System.out.println("✅ Controller: Retornando " + trabalhos.size() + " trabalhos para maquinaId: " + maquinaId);
            return ResponseEntity.ok(trabalhos);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID da máquina inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar trabalhos por máquina: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/operador/{operadorId}")
    public ResponseEntity<List<Trabalho>> buscarTrabalhosPorOperador(@PathVariable String operadorId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/trabalhos/operador/" + operadorId);
            List<Trabalho> trabalhos = trabalhoService.buscarPorOperadorId(operadorId);
            System.out.println("✅ Controller: Retornando " + trabalhos.size() + " trabalhos para operadorId: " + operadorId);
            return ResponseEntity.ok(trabalhos);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID do operador inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar trabalhos por operador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/safra/{safraId}")
    public ResponseEntity<List<Trabalho>> buscarTrabalhosPorSafra(@PathVariable String safraId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/trabalhos/safra/" + safraId);
            List<Trabalho> trabalhos = trabalhoService.buscarPorSafraId(safraId);
            System.out.println("✅ Controller: Retornando " + trabalhos.size() + " trabalhos para safraId: " + safraId);
            return ResponseEntity.ok(trabalhos);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID da safra inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar trabalhos por safra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}