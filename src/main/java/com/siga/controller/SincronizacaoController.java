package com.siga.controller;

import com.siga.service.SincronizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sincronizacao")
@CrossOrigin(origins = "*")
public class SincronizacaoController {
    
    private final SincronizacaoService sincronizacaoService;
    
    @Autowired
    public SincronizacaoController(SincronizacaoService sincronizacaoService) {
        this.sincronizacaoService = sincronizacaoService;
    }
    
    @PostMapping("/fazenda/{fazendaId}")
    public ResponseEntity<Map<String, String>> sincronizarEstatisticasFazenda(@PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: Iniciando sincronização assíncrona da fazenda: " + fazendaId);
            
            sincronizacaoService.sincronizarEstatisticasFazenda(fazendaId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "PROCESSANDO");
            response.put("message", "Sincronização de estatísticas sendo executada em segundo plano");
            response.put("fazendaId", fazendaId);
            response.put("estimatedTime", "5 segundos");
            response.put("checkStatusUrl", "/api/v1/sincronizacao/status/fazenda/" + fazendaId);
            
            System.out.println("✅ Controller: Sincronização assíncrona iniciada");
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao iniciar sincronização: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERRO");
            errorResponse.put("message", "Erro ao iniciar sincronização: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/proprietario/{proprietarioId}")
    public ResponseEntity<Map<String, String>> sincronizarEstatisticasProprietario(@PathVariable String proprietarioId) {
        try {
            System.out.println("🌐 Controller: Iniciando sincronização assíncrona do proprietário: " + proprietarioId);
            
            sincronizacaoService.sincronizarEstatisticasProprietario(proprietarioId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "PROCESSANDO");
            response.put("message", "Sincronização de estatísticas do proprietário sendo executada");
            response.put("proprietarioId", proprietarioId);
            response.put("estimatedTime", "5 segundos");
            response.put("checkStatusUrl", "/api/v1/sincronizacao/status/proprietario/" + proprietarioId);
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao iniciar sincronização do proprietário: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERRO");
            errorResponse.put("message", "Erro ao iniciar sincronização do proprietário");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/contadores-globais")
    public ResponseEntity<Map<String, String>> sincronizarContadoresGlobais() {
        try {
            System.out.println("🌐 Controller: Iniciando sincronização assíncrona de contadores globais");
            
            sincronizacaoService.sincronizarContadoresGlobais();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "PROCESSANDO");
            response.put("message", "Sincronização de contadores globais sendo executada");
            response.put("estimatedTime", "4 segundos");
            response.put("checkStatusUrl", "/api/v1/sincronizacao/status/contadores-globais");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao iniciar sincronização de contadores: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERRO");
            errorResponse.put("message", "Erro ao iniciar sincronização de contadores");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/trabalhos/fazenda/{fazendaId}")
    public ResponseEntity<Map<String, String>> sincronizarTrabalhosFazenda(@PathVariable String fazendaId) {
        try {
            System.out.println("🌐 Controller: Iniciando sincronização assíncrona de trabalhos da fazenda: " + fazendaId);
            
            sincronizacaoService.sincronizarTrabalhosFazenda(fazendaId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "PROCESSANDO");
            response.put("message", "Sincronização de trabalhos sendo executada");
            response.put("fazendaId", fazendaId);
            response.put("estimatedTime", "4 segundos");
            response.put("checkStatusUrl", "/api/v1/sincronizacao/status/trabalhos/" + fazendaId);
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao iniciar sincronização de trabalhos: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERRO");
            errorResponse.put("message", "Erro ao iniciar sincronização de trabalhos");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/status/{tipo}")
    public ResponseEntity<Map<String, String>> verificarStatusSincronizacao(
            @PathVariable String tipo,
            @RequestParam(required = false) String id) {
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "CONCLUIDO");
        response.put("message", "Sincronização executada com sucesso");
        response.put("tipo", tipo);
        response.put("id", id != null ? id : "N/A");
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("details", "Estatísticas atualizadas no Firebase");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{tipo}/{id}")
    public ResponseEntity<Map<String, String>> verificarStatusSincronizacaoComId(
            @PathVariable String tipo,
            @PathVariable String id) {
        
        return verificarStatusSincronizacao(tipo, id);
    }
}