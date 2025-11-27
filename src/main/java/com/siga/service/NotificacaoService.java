package com.siga.service;

import com.siga.model.Notificacao;
import com.siga.model.UserProfile;
import com.siga.repository.NotificacaoRepository;
import com.siga.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public NotificacaoService(NotificacaoRepository notificacaoRepository, 
                              UserProfileRepository userProfileRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Busca todas as notificações de um usuário
     */
    public List<Notificacao> getByUserId(String userId) {
        System.out.println("🔔 Service: Buscando notificações do usuário: " + userId);
        return notificacaoRepository.findByUserId(userId);
    }

    /**
     * Busca notificações não lidas de um usuário
     */
    public List<Notificacao> getNaoLidasByUserId(String userId) {
        System.out.println("🔔 Service: Buscando notificações não lidas do usuário: " + userId);
        return notificacaoRepository.findNaoLidasByUserId(userId);
    }

    /**
     * Conta notificações não lidas de um usuário
     */
    public long countNaoLidas(String userId) {
        System.out.println("🔔 Service: Contando notificações não lidas do usuário: " + userId);
        return notificacaoRepository.countNaoLidasByUserId(userId);
    }

    /**
     * Cria uma nova notificação
     * Se userId for "admin", cria notificação para todos os admins
     */
    public Notificacao criar(Notificacao notificacao) {
        System.out.println("🔔 Service: Criando notificação - Título: " + notificacao.getTitulo());

        // Validações
        if (notificacao.getTitulo() == null || notificacao.getTitulo().trim().isEmpty()) {
            throw new RuntimeException("Título é obrigatório");
        }

        if (notificacao.getMensagem() == null || notificacao.getMensagem().trim().isEmpty()) {
            throw new RuntimeException("Mensagem é obrigatória");
        }

        // Validar tipo
        List<String> tiposValidos = List.of("info", "sucesso", "alerta", "erro");
        if (notificacao.getTipo() == null || !tiposValidos.contains(notificacao.getTipo().toLowerCase())) {
            throw new RuntimeException("Tipo inválido. Use: info, sucesso, alerta ou erro");
        }

        // Validar categoria
        List<String> categoriasValidas = List.of("chamado", "sistema", "ponto", "geral");
        if (notificacao.getCategoria() == null || !categoriasValidas.contains(notificacao.getCategoria().toLowerCase())) {
            throw new RuntimeException("Categoria inválida. Use: chamado, sistema, ponto ou geral");
        }

        // Se userId for "admin", criar notificação para todos os admins
        if ("admin".equalsIgnoreCase(notificacao.getUserId())) {
            return criarParaTodosAdmins(notificacao);
        }

        // Criar notificação para usuário específico
        String id = notificacaoRepository.criar(notificacao);
        notificacao.setId(id);
        notificacao.setLida(false);
        
        System.out.println("✅ Service: Notificação criada - ID: " + id);
        return notificacao;
    }

    /**
     * Cria notificação para todos os usuários com role "admin"
     */
    private Notificacao criarParaTodosAdmins(Notificacao notificacaoOriginal) {
        System.out.println("🔔 Service: Criando notificação para todos os admins");

        // Buscar todos os admins
        List<UserProfile> admins = userProfileRepository.findAllByRole("admin");
        System.out.println("🔔 Service: Encontrados " + admins.size() + " admins");

        for (UserProfile admin : admins) {
            Notificacao notifAdmin = new Notificacao(notificacaoOriginal);
            notifAdmin.setUserId(admin.getUid());
            notificacaoRepository.criar(notifAdmin);
        }

        System.out.println("✅ Service: Notificações criadas para " + admins.size() + " admins");
        return notificacaoOriginal;
    }

    /**
     * Cria notificações para múltiplos usuários (batch)
     */
    public int criarBatch(List<String> userIds, Notificacao notificacao) {
        System.out.println("🔔 Service: Criando notificações em batch para " + userIds.size() + " usuários");

        int enviadas = 0;
        for (String userId : userIds) {
            try {
                Notificacao notifUsuario = new Notificacao(notificacao);
                notifUsuario.setUserId(userId);
                notificacaoRepository.criar(notifUsuario);
                enviadas++;
            } catch (Exception e) {
                System.err.println("❌ Erro ao criar notificação para " + userId + ": " + e.getMessage());
            }
        }

        System.out.println("✅ Service: " + enviadas + " notificações criadas");
        return enviadas;
    }

    /**
     * Marca uma notificação como lida
     */
    public void marcarComoLida(String id, String userId) {
        System.out.println("🔔 Service: Marcando notificação como lida - ID: " + id);

        // Verificar se a notificação existe e pertence ao usuário
        Notificacao notificacao = notificacaoRepository.findById(id);
        if (notificacao == null) {
            throw new RuntimeException("Notificação não encontrada");
        }

        if (!notificacao.getUserId().equals(userId)) {
            throw new RuntimeException("Notificação não pertence ao usuário");
        }

        notificacaoRepository.marcarComoLida(id);
        System.out.println("✅ Service: Notificação marcada como lida");
    }

    /**
     * Marca todas as notificações do usuário como lidas
     */
    public int marcarTodasComoLidas(String userId) {
        System.out.println("🔔 Service: Marcando todas as notificações como lidas - Usuário: " + userId);
        int atualizadas = notificacaoRepository.marcarTodasComoLidas(userId);
        System.out.println("✅ Service: " + atualizadas + " notificações marcadas como lidas");
        return atualizadas;
    }

    /**
     * Deleta uma notificação
     */
    public void deletar(String id, String userId) {
        System.out.println("🔔 Service: Deletando notificação - ID: " + id);

        // Verificar se a notificação existe e pertence ao usuário
        Notificacao notificacao = notificacaoRepository.findById(id);
        if (notificacao == null) {
            throw new RuntimeException("Notificação não encontrada");
        }

        if (!notificacao.getUserId().equals(userId)) {
            throw new RuntimeException("Notificação não pertence ao usuário");
        }

        notificacaoRepository.delete(id);
        System.out.println("✅ Service: Notificação deletada");
    }

    // ==================== MÉTODOS AUXILIARES PARA CHAMADOS ====================

    /**
     * Notifica admins quando um chamado é criado
     * Notifica:
     * 1. Admin do site (todos os chamados)
     * 2. Admin/Dono do proprietário (apenas chamados do seu proprietário)
     */
    public void notificarNovoGhamado(String operadorNome, String chamadoId, String titulo, String prioridade, String proprietarioId) {
        System.out.println("🔔 Service: Notificando admins sobre novo chamado");
        System.out.println("   📋 Chamado: " + titulo);
        System.out.println("   👤 Criado por: " + operadorNome);
        System.out.println("   🏢 Proprietário: " + proprietarioId);

        // 1. Notificar Admin do Site (todos os admins com role = 'admin')
        try {
            List<UserProfile> siteAdmins = userProfileRepository.findAllByRole("admin");
            System.out.println("   👑 Encontrados " + siteAdmins.size() + " admins do site");
            
            for (UserProfile admin : siteAdmins) {
                Notificacao notifAdmin = new Notificacao();
                notifAdmin.setUserId(admin.getUid());
                notifAdmin.setTitulo("Novo Chamado" + ("urgente".equalsIgnoreCase(prioridade) ? " URGENTE" : ""));
                notifAdmin.setMensagem("Operador " + operadorNome + " abriu um chamado: " + titulo);
                notifAdmin.setTipo("urgente".equalsIgnoreCase(prioridade) ? "alerta" : "info");
                notifAdmin.setCategoria("chamado");
                
                Map<String, Object> dados = new HashMap<>();
                dados.put("chamadoId", chamadoId);
                dados.put("prioridade", prioridade);
                dados.put("proprietarioId", proprietarioId);
                notifAdmin.setDados(dados);
                
                notificacaoRepository.criar(notifAdmin);
            }
            
            System.out.println("   ✅ Notificações enviadas para " + siteAdmins.size() + " admins do site");
        } catch (Exception e) {
            System.err.println("   ⚠️ Erro ao notificar admins do site: " + e.getMessage());
        }

        // 2. Notificar Admin/Dono do Proprietário (permissao = 'admin' ou 'dono')
        if (proprietarioId != null && !proprietarioId.isEmpty()) {
            try {
                List<String> permissoes = List.of("admin", "dono");
                List<UserProfile> proprietarioAdmins = userProfileRepository.findByProprietarioIdAndPermissao(proprietarioId, permissoes);
                System.out.println("   👥 Encontrados " + proprietarioAdmins.size() + " admins/donos do proprietário");
                
                for (UserProfile admin : proprietarioAdmins) {
                    Notificacao notifAdmin = new Notificacao();
                    notifAdmin.setUserId(admin.getUid());
                    notifAdmin.setTitulo("Novo Chamado no Seu Proprietário");
                    notifAdmin.setMensagem("Operador " + operadorNome + " abriu um chamado: " + titulo);
                    notifAdmin.setTipo("urgente".equalsIgnoreCase(prioridade) ? "alerta" : "info");
                    notifAdmin.setCategoria("chamado");
                    
                    Map<String, Object> dados = new HashMap<>();
                    dados.put("chamadoId", chamadoId);
                    dados.put("prioridade", prioridade);
                    dados.put("proprietarioId", proprietarioId);
                    notifAdmin.setDados(dados);
                    
                    notificacaoRepository.criar(notifAdmin);
                }
                
                System.out.println("   ✅ Notificações enviadas para " + proprietarioAdmins.size() + " admins/donos do proprietário");
            } catch (Exception e) {
                System.err.println("   ⚠️ Erro ao notificar admins do proprietário: " + e.getMessage());
            }
        }
    }

    /**
     * Notifica operador quando seu chamado é assumido
     */
    public void notificarChamadoAssumido(String operadorUserId, String chamadoId, String titulo, String adminNome) {
        System.out.println("🔔 Service: Notificando operador que chamado foi assumido");

        Notificacao notificacao = new Notificacao();
        notificacao.setUserId(operadorUserId);
        notificacao.setTitulo("Chamado em Atendimento");
        notificacao.setMensagem("Seu chamado '" + titulo + "' está sendo atendido por " + adminNome);
        notificacao.setTipo("info");
        notificacao.setCategoria("chamado");

        Map<String, Object> dados = new HashMap<>();
        dados.put("chamadoId", chamadoId);
        notificacao.setDados(dados);

        criar(notificacao);
    }

    /**
     * Notifica operador quando seu chamado recebe uma resposta
     */
    public void notificarChamadoRespondido(String operadorUserId, String chamadoId, String titulo) {
        System.out.println("🔔 Service: Notificando operador sobre resposta no chamado");

        Notificacao notificacao = new Notificacao();
        notificacao.setUserId(operadorUserId);
        notificacao.setTitulo("Chamado Respondido");
        notificacao.setMensagem("Seu chamado '" + titulo + "' recebeu uma resposta.");
        notificacao.setTipo("info");
        notificacao.setCategoria("chamado");

        Map<String, Object> dados = new HashMap<>();
        dados.put("chamadoId", chamadoId);
        notificacao.setDados(dados);

        criar(notificacao);
    }

    /**
     * Notifica operador quando seu chamado é resolvido
     */
    public void notificarChamadoResolvido(String operadorUserId, String chamadoId, String titulo) {
        System.out.println("🔔 Service: Notificando operador que chamado foi resolvido");

        Notificacao notificacao = new Notificacao();
        notificacao.setUserId(operadorUserId);
        notificacao.setTitulo("Chamado Resolvido");
        notificacao.setMensagem("Seu chamado '" + titulo + "' foi resolvido!");
        notificacao.setTipo("sucesso");
        notificacao.setCategoria("chamado");

        Map<String, Object> dados = new HashMap<>();
        dados.put("chamadoId", chamadoId);
        notificacao.setDados(dados);

        criar(notificacao);
    }

    /**
     * Notifica operador quando seu chamado é cancelado
     */
    public void notificarChamadoCancelado(String operadorUserId, String chamadoId, String titulo) {
        System.out.println("🔔 Service: Notificando operador que chamado foi cancelado");

        Notificacao notificacao = new Notificacao();
        notificacao.setUserId(operadorUserId);
        notificacao.setTitulo("Chamado Cancelado");
        notificacao.setMensagem("Seu chamado '" + titulo + "' foi cancelado.");
        notificacao.setTipo("alerta");
        notificacao.setCategoria("chamado");

        Map<String, Object> dados = new HashMap<>();
        dados.put("chamadoId", chamadoId);
        notificacao.setDados(dados);

        criar(notificacao);
    }
}
