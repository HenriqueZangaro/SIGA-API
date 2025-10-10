package com.siga.controller;

import com.siga.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {
    
    private final NotificacaoService notificacaoService;
    
    @Autowired
    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }
    
    @PostMapping("/trabalho/{trabalhoId}")
    public ResponseEntity<Map<String, String>> notificarNovoTrabalho(
            @PathVariable String trabalhoId,
            @RequestParam(required = false) String proprietarioId) {
        
        try {
            System.out.println("🌐 Controller: Iniciando notificação assíncrona para trabalho: " + trabalhoId);
            
            String proprietario = proprietarioId != null ? proprietarioId : "MqfPVwIC7ayojtQ1HfoM";
            
            notificacaoService.notificarNovoTrabalho(proprietario, trabalhoId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "PROCESSANDO");
            response.put("message", "Notificação sendo enviada em segundo plano");
            response.put("trabalhoId", trabalhoId);
            response.put("proprietarioId", proprietario);
            response.put("estimatedTime", "5 segundos");
            response.put("checkStatusUrl", "/api/v1/notificacoes/status/trabalho/" + trabalhoId);
            
            System.out.println("✅ Controller: Notificação assíncrona iniciada");
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao iniciar notificação: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERRO");
            errorResponse.put("message", "Erro ao iniciar notificação: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/trabalho/{trabalhoId}/atualizacao")
    public ResponseEntity<Map<String, String>> notificarAtualizacaoTrabalho(
            @PathVariable String trabalhoId,
            @RequestParam String novoStatus,
            @RequestParam(required = false) String proprietarioId) {
        
        try {
            System.out.println("🌐 Controller: Iniciando notificação de atualização para trabalho: " + trabalhoId);
            
            String proprietario = proprietarioId != null ? proprietarioId : "MqfPVwIC7ayojtQ1HfoM";
            
            notificacaoService.notificarAtualizacaoTrabalho(proprietario, trabalhoId, novoStatus);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "PROCESSANDO");
            response.put("message", "Notificação de atualização sendo enviada");
            response.put("trabalhoId", trabalhoId);
            response.put("novoStatus", novoStatus);
            response.put("proprietarioId", proprietario);
            response.put("estimatedTime", "3 segundos");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao iniciar notificação de atualização: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERRO");
            errorResponse.put("message", "Erro ao iniciar notificação de atualização");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/maquina/{maquinaId}/manutencao")
    public ResponseEntity<Map<String, String>> notificarManutencaoMaquina(
            @PathVariable String maquinaId,
            @RequestParam String tipoManutencao,
            @RequestParam(required = false) String proprietarioId) {
        
        try {
            System.out.println("🌐 Controller: Iniciando notificação de manutenção para máquina: " + maquinaId);
            
            String proprietario = proprietarioId != null ? proprietarioId : "MqfPVwIC7ayojtQ1HfoM";
            
            notificacaoService.notificarManutencaoMaquina(proprietario, maquinaId, tipoManutencao);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "PROCESSANDO");
            response.put("message", "Notificação de manutenção sendo enviada");
            response.put("maquinaId", maquinaId);
            response.put("tipoManutencao", tipoManutencao);
            response.put("proprietarioId", proprietario);
            response.put("estimatedTime", "2 segundos");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro ao iniciar notificação de manutenção: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERRO");
            errorResponse.put("message", "Erro ao iniciar notificação de manutenção");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/status/{tipo}/{id}")
    public ResponseEntity<Map<String, String>> verificarStatusNotificacao(
            @PathVariable String tipo,
            @PathVariable String id) {
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "CONCLUIDO");
        response.put("message", "Notificação enviada com sucesso");
        response.put("tipo", tipo);
        response.put("id", id);
        response.put("timestamp", java.time.Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }
}