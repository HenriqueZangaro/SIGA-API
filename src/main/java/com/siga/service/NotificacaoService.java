package com.siga.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificacaoService {
    
    @Async("taskExecutor")
    public CompletableFuture<Void> notificarNovoTrabalho(String proprietarioId, String trabalhoId) {
        try {
            System.out.println("📧 [Async] Iniciando notificação para proprietário: " + proprietarioId);
            
            Thread.sleep(1000);
            System.out.println("📧 [Async] Dados do proprietário carregados");
            
            Thread.sleep(2000);
            System.out.println("📧 [Async] Email enviado com sucesso");
            
            Thread.sleep(1000);
            System.out.println("📧 [Async] Notificação registrada no sistema");
            
            System.out.println("✅ [Async] Notificação concluída para trabalho: " + trabalhoId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ [Async] Notificação interrompida: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ [Async] Erro na notificação: " + e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Async("taskExecutor")
    public CompletableFuture<Void> notificarAtualizacaoTrabalho(String proprietarioId, String trabalhoId, String novoStatus) {
        try {
            System.out.println("📧 [Async] Notificando atualização de status: " + novoStatus);
            
            Thread.sleep(2000);
            System.out.println("📧 [Async] Notificação de atualização enviada");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ [Async] Notificação de atualização interrompida");
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Async("taskExecutor")
    public CompletableFuture<Void> notificarManutencaoMaquina(String proprietarioId, String maquinaId, String tipoManutencao) {
        try {
            System.out.println("📧 [Async] Notificando manutenção: " + tipoManutencao);
            
            Thread.sleep(1500);
            System.out.println("📧 [Async] Notificação de manutenção enviada");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ [Async] Notificação de manutenção interrompida");
        }
        
        return CompletableFuture.completedFuture(null);
    }
}