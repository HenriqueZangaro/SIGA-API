package com.siga.controller;

import com.siga.model.Operador;
import com.siga.service.OperadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operadores")
@CrossOrigin(origins = "*")
public class OperadorController {

    private final OperadorService operadorService;

    @Autowired
    public OperadorController(OperadorService operadorService) {
        this.operadorService = operadorService;
    }

    @GetMapping
    public ResponseEntity<List<Operador>> listarOperadores() {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/operadores");
            List<Operador> operadores = operadorService.buscarTodas();
            System.out.println("✅ Controller: Retornando " + operadores.size() + " operadores");
            return ResponseEntity.ok(operadores);
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar operadores: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operador> buscarOperador(@PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/operadores/" + id);
            Operador operador = operadorService.buscarPorId(id);
            System.out.println("✅ Controller: Retornando operador: " + operador.getNome());
            return ResponseEntity.ok(operador);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Operador não encontrado: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<List<Operador>> buscarOperadoresPorFazenda(@PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/operadores/fazenda/" + fazendaId);
            List<Operador> operadores = operadorService.buscarPorFazendaId(fazendaId);
            System.out.println("✅ Controller: Retornando " + operadores.size() + " operadores para fazendaId: " + fazendaId);
            return ResponseEntity.ok(operadores);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID da fazenda inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar operadores por fazenda: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}