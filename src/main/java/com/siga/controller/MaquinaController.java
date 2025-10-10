package com.siga.controller;

import com.siga.model.Maquina;
import com.siga.service.MaquinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maquinas")
@CrossOrigin(origins = "*")
public class MaquinaController {

    private final MaquinaService maquinaService;

    @Autowired
    public MaquinaController(MaquinaService maquinaService) {
        this.maquinaService = maquinaService;
    }

    @GetMapping
    public ResponseEntity<List<Maquina>> listarMaquinas() {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/maquinas");
            List<Maquina> maquinas = maquinaService.buscarTodas();
            System.out.println("✅ Controller: Retornando " + maquinas.size() + " máquinas");
            return ResponseEntity.ok(maquinas);
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar máquinas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Maquina> buscarMaquina(@PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/maquinas/" + id);
            Maquina maquina = maquinaService.buscarPorId(id);
            System.out.println("✅ Controller: Retornando máquina: " + maquina.getNome());
            return ResponseEntity.ok(maquina);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Máquina não encontrada: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fazenda/{fazendaId}")
    public ResponseEntity<List<Maquina>> buscarMaquinasPorFazenda(@PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/maquinas/fazenda/" + fazendaId);
            List<Maquina> maquinas = maquinaService.buscarPorFazendaId(fazendaId);
            System.out.println("✅ Controller: Retornando " + maquinas.size() + " máquinas para fazendaId: " + fazendaId);
            return ResponseEntity.ok(maquinas);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID da fazenda inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao buscar máquinas por fazenda: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}