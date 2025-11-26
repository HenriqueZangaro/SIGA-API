package com.siga.controller;

import com.siga.dto.CriarNotificacaoRequest;
import com.siga.dto.NotificacaoBatchRequest;
import com.siga.model.Notificacao;
import com.siga.service.AuthService;
import com.siga.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final AuthService authService;

    @Autowired
    public NotificacaoController(NotificacaoService notificacaoService, AuthService authService) {
        this.notificacaoService = notificacaoService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/notificacoes
     * Lista todas as notificações do usuário autenticado
     */
    @GetMapping
    public ResponseEntity<?> listarNotificacoes(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/notificacoes");
            System.out.println("🔐 UID: " + uid);

            List<Notificacao> notificacoes = notificacaoService.getByUserId(uid);

            System.out.println("✅ Controller: Retornando " + notificacoes.size() + " notificações");
            return ResponseEntity.ok(notificacoes);

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/notificacoes/nao-lidas
     * Lista notificações não lidas do usuário
     */
    @GetMapping("/nao-lidas")
    public ResponseEntity<?> listarNaoLidas(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/notificacoes/nao-lidas");
            System.out.println("🔐 UID: " + uid);

            List<Notificacao> notificacoes = notificacaoService.getNaoLidasByUserId(uid);

            System.out.println("✅ Controller: Retornando " + notificacoes.size() + " notificações não lidas");
            return ResponseEntity.ok(notificacoes);

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * GET /api/v1/notificacoes/count
     * Conta notificações não lidas do usuário
     */
    @GetMapping("/count")
    public ResponseEntity<?> contarNaoLidas(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 Controller: GET /api/v1/notificacoes/count");
            System.out.println("🔐 UID: " + uid);

            long count = notificacaoService.countNaoLidas(uid);

            System.out.println("✅ Controller: " + count + " notificações não lidas");
            return ResponseEntity.ok(Map.of("count", count));

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * POST /api/v1/notificacoes
     * Cria uma nova notificação
     */
    @PostMapping
    public ResponseEntity<?> criarNotificacao(
            @RequestHeader("X-User-UID") String uid,
            @RequestBody CriarNotificacaoRequest request) {
        try {
            System.out.println("🌐 Controller: POST /api/v1/notificacoes");
            System.out.println("🔐 UID: " + uid);
            System.out.println("📝 Título: " + request.getTitulo());

            Notificacao notificacao = new Notificacao();
            notificacao.setUserId(request.getUserId());
            notificacao.setTitulo(request.getTitulo());
            notificacao.setMensagem(request.getMensagem());
            notificacao.setTipo(request.getTipo());
            notificacao.setCategoria(request.getCategoria());
            notificacao.setDados(request.getDados());

            Notificacao criada = notificacaoService.criar(notificacao);

            System.out.println("✅ Controller: Notificação criada");
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * PUT /api/v1/notificacoes/{id}/lida
     * Marca uma notificação como lida
     */
    @PutMapping("/{id}/lida")
    public ResponseEntity<?> marcarComoLida(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: PUT /api/v1/notificacoes/" + id + "/lida");
            System.out.println("🔐 UID: " + uid);

            notificacaoService.marcarComoLida(id, uid);

            System.out.println("✅ Controller: Notificação marcada como lida");
            return ResponseEntity.ok(Map.of("mensagem", "Notificação marcada como lida"));

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * PUT /api/v1/notificacoes/lidas
     * Marca todas as notificações do usuário como lidas
     */
    @PutMapping("/lidas")
    public ResponseEntity<?> marcarTodasComoLidas(@RequestHeader("X-User-UID") String uid) {
        try {
            System.out.println("🌐 Controller: PUT /api/v1/notificacoes/lidas");
            System.out.println("🔐 UID: " + uid);

            int atualizadas = notificacaoService.marcarTodasComoLidas(uid);

            System.out.println("✅ Controller: " + atualizadas + " notificações marcadas como lidas");
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Todas as notificações foram marcadas como lidas",
                    "atualizadas", atualizadas
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * DELETE /api/v1/notificacoes/{id}
     * Deleta uma notificação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarNotificacao(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: DELETE /api/v1/notificacoes/" + id);
            System.out.println("🔐 UID: " + uid);

            notificacaoService.deletar(id, uid);

            System.out.println("✅ Controller: Notificação deletada");
            return ResponseEntity.ok(Map.of("mensagem", "Notificação deletada com sucesso"));

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * POST /api/v1/notificacoes/batch
     * Envia notificações para múltiplos usuários (somente admin)
     */
    @PostMapping("/batch")
    public ResponseEntity<?> enviarBatch(
            @RequestHeader("X-User-UID") String uid,
            @RequestBody NotificacaoBatchRequest request) {
        try {
            System.out.println("🌐 Controller: POST /api/v1/notificacoes/batch");
            System.out.println("🔐 UID: " + uid);

            // Verificar se é admin
            if (!authService.isAdmin(uid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Apenas administradores podem enviar notificações em batch"));
            }

            Notificacao notificacao = new Notificacao();
            notificacao.setTitulo(request.getTitulo());
            notificacao.setMensagem(request.getMensagem());
            notificacao.setTipo(request.getTipo());
            notificacao.setCategoria(request.getCategoria());
            notificacao.setDados(request.getDados());

            int enviadas = notificacaoService.criarBatch(request.getUserIds(), notificacao);

            System.out.println("✅ Controller: " + enviadas + " notificações enviadas");
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensagem", "Notificações enviadas",
                    "enviadas", enviadas
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}
