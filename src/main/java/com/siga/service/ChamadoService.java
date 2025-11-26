package com.siga.service;

import com.google.cloud.Timestamp;
import com.siga.model.Chamado;
import com.siga.repository.ChamadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final NotificacaoService notificacaoService;

    @Autowired
    public ChamadoService(ChamadoRepository chamadoRepository, @Lazy NotificacaoService notificacaoService) {
        this.chamadoRepository = chamadoRepository;
        this.notificacaoService = notificacaoService;
    }

    /**
     * Cria um novo chamado
     */
    public String criarChamado(Chamado chamado) {
        System.out.println("🔧 Service: Criando novo chamado - Título: " + chamado.getTitulo());

        // Validações
        if (chamado.getTitulo() == null || chamado.getTitulo().trim().isEmpty()) {
            throw new RuntimeException("Título é obrigatório");
        }

        if (chamado.getDescricao() == null || chamado.getDescricao().trim().isEmpty()) {
            throw new RuntimeException("Descrição é obrigatória");
        }

        if (chamado.getTipo() == null || chamado.getTipo().trim().isEmpty()) {
            throw new RuntimeException("Tipo é obrigatório");
        }

        // Validar tipo
        List<String> tiposValidos = List.of("manutencao", "problema", "suporte", "outro");
        if (!tiposValidos.contains(chamado.getTipo().toLowerCase())) {
            throw new RuntimeException("Tipo inválido. Use: manutencao, problema, suporte ou outro");
        }

        if (chamado.getPrioridade() == null || chamado.getPrioridade().trim().isEmpty()) {
            throw new RuntimeException("Prioridade é obrigatória");
        }

        // Validar prioridade
        List<String> prioridadesValidas = List.of("baixa", "media", "alta", "urgente");
        if (!prioridadesValidas.contains(chamado.getPrioridade().toLowerCase())) {
            throw new RuntimeException("Prioridade inválida. Use: baixa, media, alta ou urgente");
        }

        // Definir status padrão como 'aberto'
        if (chamado.getStatus() == null || chamado.getStatus().trim().isEmpty()) {
            chamado.setStatus("aberto");
        }

        // Definir dataHoraRegistro como agora se não fornecido (modo online)
        if (chamado.getDataHoraRegistro() == null) {
            chamado.setDataHoraRegistro(Timestamp.now());
        }

        // Definir dataHoraEnvio como agora
        if (chamado.getDataHoraEnvio() == null) {
            chamado.setDataHoraEnvio(Timestamp.now());
        }

        // Inicializar lista de fotos vazia se não fornecida
        if (chamado.getFotos() == null) {
            chamado.setFotos(new ArrayList<>());
        }

        // Inicializar lista de observações vazia
        if (chamado.getObservacoes() == null) {
            chamado.setObservacoes(new ArrayList<>());
        }

        // Definir sincronizado como true se não fornecido
        if (chamado.getSincronizado() == null) {
            chamado.setSincronizado(true);
        }

        String chamadoId = chamadoRepository.criarChamado(chamado);
        System.out.println("✅ Service: Chamado criado com sucesso - ID: " + chamadoId);

        // Notificar todos os admins sobre o novo chamado
        try {
            notificacaoService.notificarNovoGhamado(
                chamado.getOperadorNome(),
                chamadoId,
                chamado.getTitulo(),
                chamado.getPrioridade()
            );
        } catch (Exception e) {
            System.err.println("⚠️ Service: Erro ao enviar notificação (não crítico): " + e.getMessage());
        }

        return chamadoId;
    }

    /**
     * Busca um chamado por ID
     */
    public Chamado getChamadoById(String id) {
        System.out.println("🔍 Service: Buscando chamado - ID: " + id);
        return chamadoRepository.findById(id);
    }

    /**
     * Busca chamados do operador
     */
    public List<Chamado> getChamadosByOperador(String operadorId, String status, String tipo, String prioridade) {
        System.out.println("🔍 Service: Buscando chamados do operador - ID: " + operadorId);
        return chamadoRepository.findByOperadorId(operadorId, status, tipo, prioridade);
    }

    /**
     * Busca chamados do proprietário (admin)
     */
    public List<Chamado> getChamadosByProprietario(String proprietarioId, String status, String tipo, String prioridade) {
        System.out.println("🔍 Service: Buscando chamados do proprietário - ID: " + proprietarioId);
        return chamadoRepository.findByProprietarioId(proprietarioId, status, tipo, prioridade);
    }

    /**
     * Busca todos os chamados (admin)
     */
    public List<Chamado> getTodosChamados(String status, String tipo, String prioridade) {
        System.out.println("🔍 Service: Buscando todos os chamados");
        return chamadoRepository.findAll(status, tipo, prioridade);
    }

    /**
     * Atualiza um chamado
     */
    public void atualizarChamado(String id, Chamado chamadoUpdate) {
        System.out.println("🔧 Service: Atualizando chamado - ID: " + id);

        // Validar que o chamado existe
        Chamado chamadoExistente = chamadoRepository.findById(id);
        if (chamadoExistente == null) {
            throw new RuntimeException("Chamado não encontrado");
        }

        String statusAnterior = chamadoExistente.getStatus();
        String novoStatus = chamadoUpdate.getStatus();

        // Validar status se fornecido
        if (novoStatus != null) {
            List<String> statusValidos = List.of("aberto", "em_andamento", "resolvido", "cancelado");
            if (!statusValidos.contains(novoStatus.toLowerCase())) {
                throw new RuntimeException("Status inválido. Use: aberto, em_andamento, resolvido ou cancelado");
            }
        }

        // Validar prioridade se fornecida
        if (chamadoUpdate.getPrioridade() != null) {
            List<String> prioridadesValidas = List.of("baixa", "media", "alta", "urgente");
            if (!prioridadesValidas.contains(chamadoUpdate.getPrioridade().toLowerCase())) {
                throw new RuntimeException("Prioridade inválida. Use: baixa, media, alta ou urgente");
            }
        }

        chamadoRepository.updateChamado(id, chamadoUpdate);
        System.out.println("✅ Service: Chamado atualizado com sucesso");

        // Enviar notificações baseadas na mudança de status
        try {
            if (novoStatus != null && !novoStatus.equals(statusAnterior)) {
                String operadorUserId = chamadoExistente.getUserId();
                String titulo = chamadoExistente.getTitulo();

                switch (novoStatus.toLowerCase()) {
                    case "em_andamento":
                        // Chamado foi assumido
                        String adminNome = chamadoUpdate.getResponsavelNome() != null ? 
                            chamadoUpdate.getResponsavelNome() : "Admin";
                        notificacaoService.notificarChamadoAssumido(operadorUserId, id, titulo, adminNome);
                        break;
                    case "resolvido":
                        // Chamado foi resolvido
                        notificacaoService.notificarChamadoResolvido(operadorUserId, id, titulo);
                        break;
                    case "cancelado":
                        // Chamado foi cancelado
                        notificacaoService.notificarChamadoCancelado(operadorUserId, id, titulo);
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Service: Erro ao enviar notificação (não crítico): " + e.getMessage());
        }
    }

    /**
     * Adiciona uma observação ao chamado
     */
    public void adicionarObservacao(String id, String texto, String autorNome, String autorId) {
        System.out.println("💬 Service: Adicionando observação ao chamado - ID: " + id);

        if (texto == null || texto.trim().isEmpty()) {
            throw new RuntimeException("Texto da observação é obrigatório");
        }

        // Buscar chamado existente
        Chamado chamado = chamadoRepository.findById(id);
        if (chamado == null) {
            throw new RuntimeException("Chamado não encontrado");
        }

        // Criar nova observação
        Chamado.Observacao observacao = new Chamado.Observacao();
        observacao.setTexto(texto);
        observacao.setAutor(autorNome);
        observacao.setAutorId(autorId);
        observacao.setData(Timestamp.now());

        // Adicionar à lista de observações
        List<Chamado.Observacao> observacoes = chamado.getObservacoes();
        if (observacoes == null) {
            observacoes = new ArrayList<>();
        }
        observacoes.add(observacao);

        // Atualizar chamado
        Chamado chamadoUpdate = new Chamado();
        chamadoUpdate.setObservacoes(observacoes);
        chamadoRepository.updateChamado(id, chamadoUpdate);

        System.out.println("✅ Service: Observação adicionada com sucesso");

        // Notificar operador sobre a resposta (se o autor não for o operador)
        try {
            if (!autorId.equals(chamado.getUserId())) {
                notificacaoService.notificarChamadoRespondido(
                    chamado.getUserId(),
                    id,
                    chamado.getTitulo()
                );
            }
        } catch (Exception e) {
            System.err.println("⚠️ Service: Erro ao enviar notificação (não crítico): " + e.getMessage());
        }
    }

    /**
     * Adiciona uma foto ao chamado
     */
    public void adicionarFoto(String id, String fotoUrl) {
        System.out.println("📷 Service: Adicionando foto ao chamado - ID: " + id);

        if (fotoUrl == null || fotoUrl.trim().isEmpty()) {
            throw new RuntimeException("URL da foto é obrigatória");
        }

        // Validar que o chamado existe
        Chamado chamado = chamadoRepository.findById(id);
        if (chamado == null) {
            throw new RuntimeException("Chamado não encontrado");
        }

        chamadoRepository.adicionarFoto(id, fotoUrl);
        System.out.println("✅ Service: Foto adicionada com sucesso");
    }

    /**
     * Deleta um chamado
     */
    public void deletarChamado(String id, String operadorId) {
        System.out.println("🗑️ Service: Deletando chamado - ID: " + id);

        // Validar que o chamado existe
        Chamado chamado = chamadoRepository.findById(id);
        if (chamado == null) {
            throw new RuntimeException("Chamado não encontrado");
        }

        // Validar que o operador é o criador
        if (!chamado.getOperadorId().equals(operadorId)) {
            throw new RuntimeException("Apenas o criador do chamado pode deletá-lo");
        }

        // Validar que o status é 'aberto'
        if (!"aberto".equalsIgnoreCase(chamado.getStatus())) {
            throw new RuntimeException("Apenas chamados com status 'aberto' podem ser deletados");
        }

        chamadoRepository.deleteChamado(id);
        System.out.println("✅ Service: Chamado deletado com sucesso");
    }

    /**
     * Deleta um chamado (admin - sem restrições)
     */
    public void deletarChamadoAdmin(String id) {
        System.out.println("🗑️ Service: Deletando chamado (admin) - ID: " + id);

        // Validar que o chamado existe
        Chamado chamado = chamadoRepository.findById(id);
        if (chamado == null) {
            throw new RuntimeException("Chamado não encontrado");
        }

        chamadoRepository.deleteChamado(id);
        System.out.println("✅ Service: Chamado deletado com sucesso (admin)");
    }
}

