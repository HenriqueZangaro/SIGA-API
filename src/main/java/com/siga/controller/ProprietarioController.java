package com.siga.controller;

import com.siga.model.Proprietario;
import com.siga.service.ProprietarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proprietarios")
@CrossOrigin(origins = "*")
public class ProprietarioController {

    private final ProprietarioService proprietarioService;

    @Autowired
    public ProprietarioController(ProprietarioService proprietarioService) {
        this.proprietarioService = proprietarioService;
    }

    @GetMapping
    public ResponseEntity<List<Proprietario>> listarProprietarios() {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/proprietarios");
            List<Proprietario> proprietarios = proprietarioService.buscarTodos();
            System.out.println("✅ Controller: Retornando " + proprietarios.size() + " proprietários");
            return ResponseEntity.ok(proprietarios);
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao listar proprietários: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proprietario> buscarProprietario(@PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/proprietarios/" + id);
            Proprietario proprietario = proprietarioService.buscarPorId(id);
            System.out.println("✅ Controller: Retornando proprietário: " + proprietario.getNome());
            return ResponseEntity.ok(proprietario);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Proprietário não encontrado: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<Proprietario> buscarProprietarioPorDocumento(@PathVariable String documento) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/proprietarios/documento/" + documento);
            Proprietario proprietario = proprietarioService.buscarPorDocumento(documento);
            System.out.println("✅ Controller: Retornando proprietário: " + proprietario.getNome());
            return ResponseEntity.ok(proprietario);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: Documento inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Proprietário não encontrado: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}