package com.siga.controller;

import com.google.cloud.Timestamp;
import com.siga.dto.*;
import com.siga.model.Chamado;
import com.siga.model.UserProfile;
import com.siga.repository.UserProfileRepository;
import com.siga.service.AuthService;
import com.siga.service.ChamadoService;
import com.siga.service.FotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chamados")
@CrossOrigin(origins = "*")
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final FotoService fotoService;
    private final AuthService authService;
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public ChamadoController(
            ChamadoService chamadoService,
            FotoService fotoService,
            AuthService authService,
            UserProfileRepository userProfileRepository) {
        this.chamadoService = chamadoService;
        this.fotoService = fotoService;
        this.authService = authService;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * POST /api/v1/chamados
     * Cria um novo chamado
     * Valida que o proprietarioId do usuário está correto
     */
    @PostMapping
    public ResponseEntity<?> criarChamado(
            @RequestHeader("X-User-UID") String uid,
            @RequestBody CriarChamadoRequest request) {
        try {
            System.out.println("🌐 Controller: Recebida requisição POST /api/v1/chamados");
            System.out.println("🔐 UID: " + uid);
            System.out.println("📝 Título: " + request.getTitulo());

            // Buscar perfil do usuário
            UserProfile userProfile = authService.getUserProfile(uid);
            if (userProfile == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não encontrado"));
            }

            // Obter proprietarioId do usuário
            String userProprietarioId = authService.getProprietarioId(uid);
            
            if (userProprietarioId == null || userProprietarioId.isEmpty()) {
                System.out.println("⚠️ Controller: Usuário sem proprietário associado");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Usuário não possui proprietário associado. Entre em contato com o administrador."));
            }

            System.out.println("👤 Controller: UserProfile - role: " + userProfile.getRole() + ", proprietarioId: " + userProprietarioId);

            // Obter informações do operador (se for operador) ou usar dados do user
            String operadorId = null;
            String operadorNome = null;
            
            if ("operador".equalsIgnoreCase(userProfile.getRole())) {
                Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
                @SuppressWarnings("unchecked")
                Map<String, Object> operadorData = (Map<String, Object>) operadorInfo.get("operador");
                operadorId = (String) operadorData.get("id");
                operadorNome = (String) operadorData.get("nome");
            } else if ("user".equalsIgnoreCase(userProfile.getRole())) {
                // User comum pode criar chamado também
                // Para compatibilidade, usar userId como operadorId
                operadorId = uid; // Usar UID como operadorId para compatibilidade
                operadorNome = userProfile.getDisplayName() != null ? userProfile.getDisplayName() : userProfile.getEmail();
            } else if ("admin".equalsIgnoreCase(userProfile.getRole())) {
                // Admin pode criar, mas não recomendado
                operadorNome = userProfile.getDisplayName() != null ? userProfile.getDisplayName() : "Administrador";
            }

            // Criar chamado
            Chamado chamado = new Chamado();
            chamado.setOperadorId(operadorId);
            chamado.setOperadorNome(operadorNome);
            chamado.setUserId(uid);
            chamado.setTitulo(request.getTitulo());
            chamado.setDescricao(request.getDescricao());
            chamado.setTipo(request.getTipo());
            chamado.setPrioridade(request.getPrioridade());
            chamado.setStatus("aberto");
            chamado.setDataHoraRegistro(Timestamp.now());
            chamado.setDataHoraEnvio(Timestamp.now());
            chamado.setProprietarioId(userProprietarioId); // SEMPRE usar o do usuário
            chamado.setLocalizacao(request.getLocalizacao());
            chamado.setFazendaId(request.getFazendaId());
            chamado.setFazendaNome(request.getFazendaNome());
            chamado.setTalhaoId(request.getTalhaoId());
            chamado.setTalhaoNome(request.getTalhaoNome());
            chamado.setMaquinaId(request.getMaquinaId());
            chamado.setMaquinaNome(request.getMaquinaNome());
            chamado.setSincronizado(request.getSincronizado() != null ? request.getSincronizado() : true);

            // Validar no service
            String chamadoId = chamadoService.criarChamado(chamado, uid, userProfile);

            Map<String, Object> response = new HashMap<>();
            response.put("id", chamadoId);
            response.put("titulo", chamado.getTitulo());
            response.put("status", chamado.getStatus());
            response.put("dataCriacao", chamado.getDataHoraRegistro());
            response.put("mensagem", "Chamado criado com sucesso");

            System.out.println("✅ Controller: Chamado criado - ID: " + chamadoId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

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
     * GET /api/v1/chamados
     * Lista chamados com filtro por proprietarioId
     * - Admin: vê todos (ou filtra por proprietarioId se informado)
     * - User/Operador: vê apenas do seu proprietário
     */
    @GetMapping
    public ResponseEntity<?> getChamados(
            @RequestHeader("X-User-UID") String uid,
            @RequestParam(required = false) String proprietarioId,
            @RequestParam(required = false) String operadorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String prioridade) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/chamados");
            System.out.println("🔐 UID: " + uid);

            List<Chamado> chamados;

            // Admin do site tem acesso total
            if (authService.isAdmin(uid)) {
                System.out.println("👑 Admin do site - Acesso total");
                
                // Admin pode filtrar por proprietarioId ou operadorId
                if (proprietarioId != null && !proprietarioId.isEmpty()) {
                    chamados = chamadoService.getChamadosByProprietario(proprietarioId, status, tipo, prioridade);
                    System.out.println("✅ Controller: Retornando " + chamados.size() + " chamados do proprietário " + proprietarioId);
                } else if (operadorId != null && !operadorId.isEmpty()) {
                    chamados = chamadoService.getChamadosByOperador(operadorId, status, tipo, prioridade);
                    System.out.println("✅ Controller: Retornando " + chamados.size() + " chamados do operador " + operadorId);
                } else {
                    chamados = chamadoService.getTodosChamados(status, tipo, prioridade);
                    System.out.println("✅ Controller: Retornando " + chamados.size() + " chamados (todos)");
                }
            } else {
                // User ou Operador: filtrar por proprietarioId do usuário
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || userProprietarioId.isEmpty()) {
                    System.out.println("⚠️ Controller: Usuário sem proprietário associado");
                    return ResponseEntity.ok(Collections.emptyList());
                }
                
                // Validar se o usuário está tentando ver outro proprietário
                if (proprietarioId != null && !proprietarioId.equals(userProprietarioId)) {
                    System.out.println("⚠️ Controller: Tentativa de acessar outro proprietário");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Sem permissão para acessar chamados de outro proprietário"));
                }
                
                System.out.println("🔍 Controller: Filtrando por proprietarioId: " + userProprietarioId);
                
                // Se for operador, pode filtrar por seus próprios chamados
                if (authService.isOperador(uid) && operadorId != null && !operadorId.isEmpty()) {
                    Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> operadorData = (Map<String, Object>) operadorInfo.get("operador");
                    String authenticatedOperadorId = (String) operadorData.get("id");
                    
                    // Validar que está tentando ver seus próprios chamados
                    if (!operadorId.equals(authenticatedOperadorId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("erro", "Acesso negado"));
                    }
                    
                    chamados = chamadoService.getChamadosByOperador(authenticatedOperadorId, status, tipo, prioridade);
                } else {
                    // Buscar todos os chamados do proprietário
                    chamados = chamadoService.getChamadosByProprietario(userProprietarioId, status, tipo, prioridade);
                }
                
                System.out.println("✅ Controller: Retornando " + chamados.size() + " chamados do proprietário");
            }

            return ResponseEntity.ok(chamados);

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
     * GET /api/v1/chamados/{id}
     * Busca um chamado específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getChamado(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/chamados/" + id);
            System.out.println("🔐 UID: " + uid);

            Chamado chamado = chamadoService.getChamadoById(id);

            // Verificar permissão por proprietarioId
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Usuário sem proprietário associado"));
                }
                
                // Verificar se o chamado pertence ao proprietário do usuário
                if (chamado.getProprietarioId() == null || !chamado.getProprietarioId().equals(userProprietarioId)) {
                    System.out.println("⚠️ Controller: Acesso negado - proprietário diferente");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a este chamado"));
                }
            }

            System.out.println("✅ Controller: Chamado encontrado - ID: " + id);
            return ResponseEntity.ok(chamado);

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
     * PUT /api/v1/chamados/{id}
     * Atualiza um chamado (usado por admins)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarChamado(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id,
            @RequestBody AtualizarChamadoRequest request) {
        try {
            System.out.println("🌐 Controller: Recebida requisição PUT /api/v1/chamados/" + id);
            System.out.println("🔐 UID: " + uid);

            if (!authService.isAdmin(uid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Apenas administradores podem atualizar chamados"));
            }

            Chamado chamadoUpdate = new Chamado();
            chamadoUpdate.setStatus(request.getStatus());
            chamadoUpdate.setResponsavelId(request.getResponsavelId());
            chamadoUpdate.setResponsavelNome(request.getResponsavelNome());
            chamadoUpdate.setPrioridade(request.getPrioridade());

            chamadoService.atualizarChamado(id, chamadoUpdate);

            System.out.println("✅ Controller: Chamado atualizado com sucesso");
            return ResponseEntity.ok(Map.of("mensagem", "Chamado atualizado com sucesso"));

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
     * POST /api/v1/chamados/{id}/observacoes
     * Adiciona uma observação ao chamado
     */
    @PostMapping("/{id}/observacoes")
    public ResponseEntity<?> adicionarObservacao(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id,
            @RequestBody AdicionarObservacaoRequest request) {
        try {
            System.out.println("🌐 Controller: Recebida requisição POST /api/v1/chamados/" + id + "/observacoes");
            System.out.println("🔐 UID: " + uid);

            UserProfile userProfile = userProfileRepository.findByUid(uid);
            if (userProfile == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não encontrado"));
            }

            // Verificar permissão por proprietarioId (admin pode sempre, outros só se for do seu proprietário)
            if (!authService.isAdmin(uid)) {
                Chamado chamado = chamadoService.getChamadoById(id);
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null || 
                    chamado.getProprietarioId() == null || 
                    !chamado.getProprietarioId().equals(userProprietarioId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a este chamado"));
                }
            }

            String autorNome = userProfile.getDisplayName() != null ? userProfile.getDisplayName() : userProfile.getEmail();

            chamadoService.adicionarObservacao(id, request.getObservacao(), autorNome, uid);

            System.out.println("✅ Controller: Observação adicionada com sucesso");
            return ResponseEntity.ok(Map.of("mensagem", "Observação adicionada com sucesso"));

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
     * POST /api/v1/chamados/{id}/fotos
     * Faz upload de uma foto para o chamado
     */
    @PostMapping("/{id}/fotos")
    public ResponseEntity<?> uploadFoto(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id,
            @RequestParam("foto") MultipartFile file) {
        try {
            System.out.println("🌐 Controller: Recebida requisição POST /api/v1/chamados/" + id + "/fotos");
            System.out.println("🔐 UID: " + uid);

            // Validar que o chamado existe
            Chamado chamado = chamadoService.getChamadoById(id);

            // Verificar permissão por proprietarioId
            if (!authService.isAdmin(uid)) {
                String userProprietarioId = authService.getProprietarioId(uid);
                
                if (userProprietarioId == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Usuário sem proprietário associado"));
                }
                
                // Verificar se o chamado pertence ao proprietário do usuário
                if (chamado.getProprietarioId() == null || !chamado.getProprietarioId().equals(userProprietarioId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("erro", "Acesso negado a este chamado"));
                }
            }

            // Fazer upload da foto
            String fotoUrl = fotoService.uploadFoto(id, file);

            // Adicionar URL ao chamado
            chamadoService.adicionarFoto(id, fotoUrl);

            FotoUploadResponse response = new FotoUploadResponse();
            response.setUrl(fotoUrl);
            response.setFotoId(UUID.randomUUID().toString());

            System.out.println("✅ Controller: Foto adicionada ao chamado - URL: " + fotoUrl);
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
     * DELETE /api/v1/chamados/{id}
     * Deleta um chamado (operador pode deletar apenas se status = aberto)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarChamado(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String id) {
        try {
            System.out.println("🌐 Controller: Recebida requisição DELETE /api/v1/chamados/" + id);
            System.out.println("🔐 UID: " + uid);

            if (authService.isAdmin(uid)) {
                // Admin pode deletar qualquer chamado
                chamadoService.deletarChamadoAdmin(id);
            } else if (authService.isOperador(uid)) {
                // Operador só pode deletar seu próprio chamado com status 'aberto'
                Map<String, Object> operadorInfo = authService.getOperadorInfo(uid);
                @SuppressWarnings("unchecked")
                Map<String, Object> operadorData = (Map<String, Object>) operadorInfo.get("operador");
                String operadorId = (String) operadorData.get("id");
                
                chamadoService.deletarChamado(id, operadorId);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Acesso negado"));
            }

            System.out.println("✅ Controller: Chamado deletado com sucesso");
            return ResponseEntity.ok(Map.of("mensagem", "Chamado deletado com sucesso"));

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
     * GET /api/v1/chamados/admin/proprietario/{proprietarioId}
     * Busca chamados de um proprietário (admin)
     */
    @GetMapping("/admin/proprietario/{proprietarioId}")
    public ResponseEntity<?> getChamadosByProprietario(
            @RequestHeader("X-User-UID") String uid,
            @PathVariable String proprietarioId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String prioridade) {
        try {
            System.out.println("🌐 Controller: Recebida requisição GET /api/v1/chamados/admin/proprietario/" + proprietarioId);
            System.out.println("🔐 UID: " + uid);

            if (!authService.isAdmin(uid) && !authService.podeAcessarProprietario(uid, proprietarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Sem permissão para acessar estes dados"));
            }

            List<Chamado> chamados = chamadoService.getChamadosByProprietario(proprietarioId, status, tipo, prioridade);

            System.out.println("✅ Controller: Retornando " + chamados.size() + " chamados do proprietário");
            return ResponseEntity.ok(chamados);

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

