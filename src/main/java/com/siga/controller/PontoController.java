package com.siga.controller;

import com.google.cloud.Timestamp;
import com.siga.dto.EstatisticasPontosResponse;
import com.siga.dto.RegistroPontoRequest;
import com.siga.dto.StatusOperadorResponse;
import com.siga.model.Ponto;
import com.siga.service.AuthService;
import com.siga.service.PontoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para gerenciar registro de pontos de operadores
 */
@RestController
@RequestMapping("/api/v1/pontos")
@CrossOrigin(origins = "*")
public class PontoController {

    private final PontoService pontoService;
    private final AuthService authService;

    @Autowired
    public PontoController(PontoService pontoService, AuthService authService) {
        this.pontoService = pontoService;
        this.authService = authService;
    }

    /**
     * POST /api/v1/pontos/registrar
     * Registra entrada ou saída
     * Requer header: X-User-UID (UID do Firebase Auth)
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPonto(
            @RequestHeader("X-User-UID") String uid,
            @RequestBody RegistroPontoRequest request) {
        try {
            System.out.println("🌐 Controller: Recebida requisição POST /api/v1/pontos/registrar");
            System.out.println("🔐 UID: " + uid);
            System.out.println("📝 Tipo: " + request.getTipo());
            
            // Validar tipo
            if (!request.getTipo().equalsIgnoreCase("entrada") && !request.getTipo().equalsIgnoreCase("saida")) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Tipo inválido. Deve ser 'entrada' ou 'saida'"));
            }
            
            // Buscar informações do operador
            Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
            Map<String, Object> operadorData = (Map<String, Object>) operadorInfo.get("operador");
            
            String operadorId = (String) operadorData.get("id");
            String operadorNome = (String) operadorData.get("nome");
            String proprietarioId = (String) operadorData.get("proprietarioId");
            
            // Validar se pode registrar entrada/saída
            StatusOperadorResponse status = pontoService.getStatusOperador(operadorId);
            
            if (request.getTipo().equalsIgnoreCase("entrada") && !status.isPodeRegistrarEntrada()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Já existe um ponto de entrada aberto"));
            }
            
            if (request.getTipo().equalsIgnoreCase("saida") && !status.isPodeRegistrarSaida()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Não há ponto de entrada aberto para registrar saída"));
            }
            
            // Criar ponto
            Ponto ponto = new Ponto();
            ponto.setOperadorId(operadorId);
            ponto.setOperadorNome(operadorNome);
            ponto.setUserId(uid);
            ponto.setTipo(request.getTipo().toLowerCase());
            ponto.setDataHora(Timestamp.now());
            ponto.setProprietarioId(proprietarioId);
            ponto.setDispositivo(request.getDispositivo());
            ponto.setVersaoApp(request.getVersaoApp());
            ponto.setObservacao(request.getObservacao());
            ponto.setFazendaId(request.getFazendaId());
            
            // Adicionar localização se fornecida
            if (request.getLocalizacao() != null) {
                Ponto.Localizacao localizacao = new Ponto.Localizacao();
                localizacao.setLatitude(request.getLocalizacao().getLatitude());
                localizacao.setLongitude(request.getLocalizacao().getLongitude());
                localizacao.setAccuracy(request.getLocalizacao().getAccuracy());
                localizacao.setTimestamp(request.getLocalizacao().getTimestamp());
                ponto.setLocalizacao(localizacao);
            }
            
            // Registrar ponto
            String pontoId = pontoService.registrarPonto(ponto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", pontoId);
            response.put("tipo", ponto.getTipo());
            response.put("dataHora", ponto.getDataHora());
            response.put("mensagem", "Ponto registrado com sucesso");
            
            System.out.println("✅ Controller: Ponto registrado - ID: " + pontoId);
            
            return ResponseEntity.ok(response);
            
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
     * GET /api/v1/pontos/status
     * Retorna status atual do operador (ponto aberto, horas trabalhadas hoje, etc)
     * Admin pode especificar operadorId
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String operadorId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/pontos/status");
            System.out.println("🔐 UID: " + uid);
            
            String targetOperadorId = operadorId;
            
            // Se não especificou operadorId, buscar do usuário logado
            if (targetOperadorId == null || targetOperadorId.isEmpty()) {
                if (authService.isAdmin(uid)) {
                    return ResponseEntity.badRequest().body(Map.of("erro", "Admin deve especificar operadorId"));
                }
                
                Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
                Map<String, Object> operadorData = (Map<String, Object>) operadorInfo.get("operador");
                targetOperadorId = (String) operadorData.get("id");
            } else {
                // Se especificou operadorId, verificar se é admin
                if (!authService.isAdmin(uid)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Apenas admin pode consultar status de outros operadores"));
                }
            }
            
            // Buscar status
            StatusOperadorResponse status = pontoService.getStatusOperador(targetOperadorId);
            
            System.out.println("✅ Controller: Status retornado");
            
            return ResponseEntity.ok(status);
            
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
     * GET /api/v1/pontos/hoje
     * Retorna pontos de hoje do operador
     * Se for ADMIN, pode especificar operadorId, senão retorna todos de hoje
     */
    @GetMapping("/hoje")
    public ResponseEntity<?> getPontosHoje(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String operadorId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/pontos/hoje");
            System.out.println("🔐 UID: " + uid);
            
            // Se for admin e não especificou operadorId, retorna todos de hoje
            if (authService.isAdmin(uid)) {
                if (operadorId == null || operadorId.isEmpty()) {
                    System.out.println("👑 Admin detectado - buscando todos os pontos de hoje");
                    Calendar hoje = Calendar.getInstance();
                    hoje.set(Calendar.HOUR_OF_DAY, 0);
                    hoje.set(Calendar.MINUTE, 0);
                    hoje.set(Calendar.SECOND, 0);
                    hoje.set(Calendar.MILLISECOND, 0);
                    
                    Calendar amanha = Calendar.getInstance();
                    amanha.setTime(hoje.getTime());
                    amanha.add(Calendar.DAY_OF_MONTH, 1);
                    
                    List<Ponto> pontos = pontoService.getTodosPontos(hoje.getTime(), amanha.getTime());
                    System.out.println("✅ Controller: Retornando " + pontos.size() + " pontos de hoje (todos)");
                    return ResponseEntity.ok(pontos);
                }
                // Admin especificou operadorId
                List<Ponto> pontos = pontoService.getPontosHoje(operadorId);
                System.out.println("✅ Controller: Retornando " + pontos.size() + " pontos de hoje do operador " + operadorId);
                return ResponseEntity.ok(pontos);
            }
            
            // Se não for admin, buscar informações do operador logado
            Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
            Map<String, Object> operadorData = (Map<String, Object>) operadorInfo.get("operador");
            String operadorIdLogado = (String) operadorData.get("id");
            
            // Buscar pontos de hoje
            List<Ponto> pontos = pontoService.getPontosHoje(operadorIdLogado);
            
            System.out.println("✅ Controller: Retornando " + pontos.size() + " pontos de hoje");
            
            return ResponseEntity.ok(pontos);
            
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
     * GET /api/v1/pontos/historico
     * Retorna histórico de pontos do operador com filtro de data
     * Se for ADMIN, retorna todos os pontos de todos os operadores
     * Parâmetros opcionais: dataInicio, dataFim, operadorId (formato: yyyy-MM-dd)
     */
    @GetMapping("/historico")
    public ResponseEntity<?> getHistorico(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dataFim,
            @RequestParam(required = false) String operadorId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/pontos/historico");
            System.out.println("🔐 UID: " + uid);
            System.out.println("📅 Filtro: " + dataInicio + " - " + dataFim);
            
            // Verificar se é admin
            if (authService.isAdmin(uid)) {
                System.out.println("👑 Admin detectado - buscando todos os pontos");
                
                // Se admin especificou operadorId, buscar apenas desse operador
                if (operadorId != null && !operadorId.isEmpty()) {
                    List<Ponto> pontos = pontoService.getPontosByOperador(operadorId, dataInicio, dataFim);
                    System.out.println("✅ Controller: Retornando " + pontos.size() + " pontos do operador " + operadorId);
                    return ResponseEntity.ok(pontos);
                }
                
                // Se não especificou operadorId, buscar todos os pontos
                List<Ponto> todosPontos = pontoService.getTodosPontos(dataInicio, dataFim);
                System.out.println("✅ Controller: Retornando " + todosPontos.size() + " pontos (todos)");
                return ResponseEntity.ok(todosPontos);
            }
            
            // Se não for admin, buscar apenas os pontos do operador logado
            Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
            Map<String, Object> operadorData = (Map<String, Object>) operadorInfo.get("operador");
            String operadorIdLogado = (String) operadorData.get("id");
            
            // Buscar pontos
            List<Ponto> pontos = pontoService.getPontosByOperador(operadorIdLogado, dataInicio, dataFim);
            
            System.out.println("✅ Controller: Retornando " + pontos.size() + " pontos");
            
            return ResponseEntity.ok(pontos);
            
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
     * GET /api/v1/pontos/estatisticas
     * Retorna estatísticas de pontos do operador
     * Admin pode especificar operadorId
     * Parâmetros obrigatórios: dataInicio, dataFim (formato: yyyy-MM-dd)
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<?> getEstatisticas(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dataInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dataFim,
            @RequestParam(required = false) String operadorId) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/pontos/estatisticas");
            System.out.println("🔐 UID: " + uid);
            System.out.println("📅 Período: " + dataInicio + " - " + dataFim);
            
            String targetOperadorId = operadorId;
            
            // Se não especificou operadorId, buscar do usuário logado
            if (targetOperadorId == null || targetOperadorId.isEmpty()) {
                if (authService.isAdmin(uid)) {
                    return ResponseEntity.badRequest().body(Map.of("erro", "Admin deve especificar operadorId ou usar endpoint /admin/proprietario/{id}"));
                }
                
                Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
                Map<String, Object> operadorData = (Map<String, Object>) operadorInfo.get("operador");
                targetOperadorId = (String) operadorData.get("id");
            } else {
                // Se especificou operadorId, verificar se é admin
                if (!authService.isAdmin(uid)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Apenas admin pode consultar estatísticas de outros operadores"));
                }
            }
            
            // Buscar estatísticas
            EstatisticasPontosResponse estatisticas = pontoService.getEstatisticas(targetOperadorId, dataInicio, dataFim);
            
            System.out.println("✅ Controller: Estatísticas calculadas");
            
            return ResponseEntity.ok(estatisticas);
            
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
     * GET /api/v1/pontos/admin/proprietario/{proprietarioId}
     * Retorna pontos de um proprietário (apenas admin)
     * Parâmetros opcionais: dataInicio, dataFim (formato: yyyy-MM-dd)
     */
    @GetMapping("/admin/proprietario/{proprietarioId}")
    public ResponseEntity<?> getPontosByProprietario(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String proprietarioId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dataFim) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/pontos/admin/proprietario/" + proprietarioId);
            System.out.println("🔐 UID: " + uid);
            
            // Verificar se é admin ou tem permissão
            if (!authService.isAdmin(uid) && !authService.podeAcessarProprietario(uid, proprietarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Sem permissão para acessar estes dados"));
            }
            
            // Buscar pontos
            List<Ponto> pontos = pontoService.getPontosByProprietario(proprietarioId, dataInicio, dataFim);
            
            System.out.println("✅ Controller: Retornando " + pontos.size() + " pontos do proprietário");
            
            return ResponseEntity.ok(pontos);
            
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
     * PUT /api/v1/pontos/admin/{id}
     * Atualiza um ponto (apenas admin)
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updatePonto(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id,
            @RequestBody Ponto ponto) {
        try {
            System.out.println("🌐 Controller: Recebida requisição PUT /api/v1/pontos/admin/" + id);
            
            // Verificar se é admin
            if (!authService.isAdmin(uid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Apenas administradores podem atualizar pontos"));
            }
            
            pontoService.updatePonto(id, ponto);
            
            return ResponseEntity.ok(Map.of("mensagem", "Ponto atualizado com sucesso"));
            
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
     * DELETE /api/v1/pontos/admin/{id}
     * Deleta um ponto (apenas admin)
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deletePonto(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição DELETE /api/v1/pontos/admin/" + id);
            
            // Verificar se é admin
            if (!authService.isAdmin(uid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Apenas administradores podem deletar pontos"));
            }
            
            pontoService.deletePonto(id);
            
            return ResponseEntity.ok(Map.of("mensagem", "Ponto deletado com sucesso"));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Controller: Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Controller: Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno do servidor"));
        }
    }
}

