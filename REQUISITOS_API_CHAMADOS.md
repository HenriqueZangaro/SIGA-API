# 📋 Requisitos da API para Sistema de Chamados

## ✅ Status Atual

A API já foi implementada conforme `VALIDACAO_CHAMADOS_PROPRIETARIO.md` (agora consolidado no README). Este documento resume o que a API precisa fazer para suportar a nova funcionalidade onde **usuários e operadores** podem criar chamados.

---

## 🎯 Funcionalidades Necessárias

### 1. POST `/api/v1/chamados` - Criar Chamado

**Quem pode criar:**
- ✅ Operadores (`role = 'operador'`)
- ✅ Usuários (`role = 'user'`) - **NOVO**
- ✅ Admin do site (`role = 'admin'`) - não recomendado

**Validações necessárias:**

```java
@PostMapping
public ResponseEntity<?> createChamado(
    @RequestHeader("X-User-UID") String userUid,
    @RequestBody CriarChamadoRequest request
) {
    // 1. Buscar perfil do usuário
    UserProfile userProfile = authService.getUserProfile(userUid);
    if (userProfile == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("erro", "Usuário não encontrado"));
    }

    // 2. Validar que o usuário tem proprietarioId
    String userProprietarioId = userProfile.getProprietarioId();
    if (userProprietarioId == null || userProprietarioId.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("erro", "Usuário não possui proprietário associado"));
    }

    // 3. Aceitar criação de usuários (role = 'user' ou 'operador')
    // Não precisa validar role específico, apenas que tem proprietarioId

    // 4. Definir proprietarioId no chamado (sempre do usuário)
    request.setProprietarioId(userProprietarioId);

    // 5. Preencher dados do criador
    if (userProfile.getRole().equals("operador")) {
        request.setOperadorId(userProfile.getOperadorId());
        request.setOperadorNome(userProfile.getDisplayName());
    } else if (userProfile.getRole().equals("user")) {
        // Para usuários, usar userId como operadorId (compatibilidade)
        request.setOperadorId(userProfile.getUserId());
        request.setOperadorNome(userProfile.getDisplayName());
    }

    // 6. Criar chamado
    Chamado chamado = chamadoService.create(request, userUid, userProfile);
    
    // 7. Enviar notificações
    notificacaoService.notificarNovoChamado(chamado);
    
    return ResponseEntity.ok(chamado);
}
```

**Campos do Request:**
```json
{
  "titulo": "string",
  "descricao": "string",
  "tipo": "manutencao" | "problema" | "suporte" | "outro",
  "prioridade": "baixa" | "media" | "alta" | "urgente",
  "localizacao": {
    "latitude": number,
    "longitude": number,
    "accuracy": number
  },
  "fazendaId": "string (opcional)",
  "talhaoId": "string (opcional)",
  "maquinaId": "string (opcional)"
}
```

**NOTA:** A API **NÃO** deve aceitar `proprietarioId` no body. Sempre usa o `proprietarioId` do usuário autenticado.

---

### 2. GET `/api/v1/chamados` - Listar Chamados

**Validações necessárias:**

```java
@GetMapping
public ResponseEntity<List<Chamado>> getAllChamados(
    @RequestHeader("X-User-UID") String userUid,
    @RequestParam(required = false) String proprietarioId,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String tipo,
    @RequestParam(required = false) String prioridade
) {
    // 1. Buscar perfil do usuário
    UserProfile userProfile = authService.getUserProfile(userUid);
    
    // 2. Admin do site: vê todos ou filtra
    if (userProfile.getRole().equals("admin")) {
        if (proprietarioId != null && !proprietarioId.isEmpty()) {
            return ResponseEntity.ok(
                chamadoService.findByProprietarioId(proprietarioId, status, tipo, prioridade)
            );
        }
        return ResponseEntity.ok(
            chamadoService.findAll(status, tipo, prioridade)
        );
    }
    
    // 3. User/Operador: SEMPRE filtrar pelo proprietarioId do usuário
    String userProprietarioId = userProfile.getProprietarioId();
    
    if (userProprietarioId == null || userProprietarioId.isEmpty()) {
        // Sem proprietário - retornar vazio (não erro)
        return ResponseEntity.ok(Collections.emptyList());
    }
    
    // 4. Validar se está tentando ver outro proprietário
    if (proprietarioId != null && !proprietarioId.equals(userProprietarioId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Collections.emptyList());
    }
    
    // 5. Retornar chamados do proprietário do usuário
    return ResponseEntity.ok(
        chamadoService.findByProprietarioId(userProprietarioId, status, tipo, prioridade)
    );
}
```

**Comportamento:**
- **Admin do site**: Vê todos os chamados (pode filtrar por `proprietarioId`)
- **Admin do proprietário**: Vê apenas chamados do seu proprietário
- **User/Operador**: Vê apenas chamados do seu proprietário

---

### 3. Sistema de Notificações

**Quando criar chamado, notificar:**

```java
public void notificarNovoChamado(Chamado chamado) {
    String proprietarioId = chamado.getProprietarioId();
    
    // 1. Notificar Admin do Site (todos os admins)
    List<UserProfile> siteAdmins = userService.findByRole("admin");
    for (UserProfile admin : siteAdmins) {
        notificacaoService.criarNotificacao(
            admin.getUserId(),
            "Novo Chamado",
            "Novo chamado: " + chamado.getTitulo(),
            "chamado",
            Map.of("chamadoId", chamado.getId())
        );
    }
    
    // 2. Notificar Admin do Proprietário (permissao = 'admin' ou 'dono')
    List<UserProfile> proprietarioAdmins = userService.findByProprietarioIdAndPermissao(
        proprietarioId,
        Arrays.asList("admin", "dono")
    );
    for (UserProfile admin : proprietarioAdmins) {
        notificacaoService.criarNotificacao(
            admin.getUserId(),
            "Novo Chamado",
            "Novo chamado no seu proprietário: " + chamado.getTitulo(),
            "chamado",
            Map.of("chamadoId", chamado.getId(), "proprietarioId", proprietarioId)
        );
    }
}
```

**Quem recebe notificações:**
- ✅ **Admin do Site**: Recebe notificação de **todos** os chamados
- ✅ **Admin do Proprietário**: Recebe notificação apenas de chamados do **seu proprietário**
- ✅ **Dono do Proprietário**: Recebe notificação apenas de chamados do **seu proprietário**

---

## 🔍 Validações Importantes

### 1. ProprietarioId é Obrigatório

```java
// NO SERVICE
public Chamado create(CriarChamadoRequest request, String userUid, UserProfile userProfile) {
    // VALIDAÇÃO CRÍTICA
    if (request.getProprietarioId() == null || request.getProprietarioId().trim().isEmpty()) {
        throw new IllegalArgumentException("proprietarioId é obrigatório");
    }
    
    // VALIDAÇÃO CRÍTICA: Validar que o usuário pertence ao proprietário
    String userProprietarioId = userProfile.getProprietarioId();
    if (!request.getProprietarioId().equals(userProprietarioId)) {
        throw new SecurityException("Usuário não pode criar chamado para outro proprietário");
    }
    
    // Criar chamado...
}
```

### 2. Aceitar Usuários (não apenas operadores)

**ANTES:**
```java
// ❌ Só aceitava operadores
if (!userProfile.getRole().equals("operador")) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("erro", "Apenas operadores podem criar chamados"));
}
```

**DEPOIS:**
```java
// ✅ Aceita user e operador
if (!userProfile.getRole().equals("operador") && !userProfile.getRole().equals("user")) {
    // Admin pode criar, mas não recomendado
    if (!userProfile.getRole().equals("admin")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("erro", "Apenas usuários e operadores podem criar chamados"));
    }
}
```

---

## 📊 Resumo das Mudanças Necessárias

| Item | Status | Descrição |
|------|--------|-----------|
| Aceitar criação de usuários | ⚠️ Verificar | API deve aceitar `role = 'user'` |
| Validar proprietarioId | ✅ Já implementado | API valida e define automaticamente |
| Filtrar por proprietarioId | ✅ Já implementado | API filtra automaticamente |
| Notificar admins corretos | ⚠️ Verificar | Notificar admin do site e admin do proprietário |
| Preencher operadorId para users | ⚠️ Verificar | Para compatibilidade, usar userId como operadorId |

---

## 🧪 Testes Necessários

### 1. Teste: User cria chamado
```
POST /api/v1/chamados
X-User-UID: uid_user
Body: { titulo, descricao, tipo, prioridade }

Esperado:
- ✅ Chamado criado com proprietarioId do usuário
- ✅ Notificação enviada para admin do site
- ✅ Notificação enviada para admin do proprietário
```

### 2. Teste: Operador cria chamado
```
POST /api/v1/chamados
X-User-UID: uid_operador
Body: { titulo, descricao, tipo, prioridade }

Esperado:
- ✅ Chamado criado com proprietarioId do operador
- ✅ operadorId preenchido corretamente
- ✅ Notificações enviadas
```

### 3. Teste: User vê apenas seus chamados
```
GET /api/v1/chamados
X-User-UID: uid_user

Esperado:
- ✅ Apenas chamados do proprietário do usuário
- ✅ Não vê chamados de outros proprietários
```

### 4. Teste: Admin do proprietário vê apenas seus chamados
```
GET /api/v1/chamados
X-User-UID: uid_admin_proprietario

Esperado:
- ✅ Apenas chamados do seu proprietário
- ✅ Não vê chamados de outros proprietários
```

### 5. Teste: Admin do site vê todos
```
GET /api/v1/chamados
X-User-UID: uid_admin_site

Esperado:
- ✅ Todos os chamados
- ✅ Pode filtrar por proprietarioId
```

---

## ✅ Checklist de Implementação

- [ ] Verificar se API aceita criação de chamados por usuários (`role = 'user'`)
- [ ] Verificar se API preenche `operadorId` corretamente para usuários
- [ ] Verificar se notificações são enviadas para:
  - [ ] Admin do site (todos os chamados)
  - [ ] Admin do proprietário (apenas do seu proprietário)
  - [ ] Dono do proprietário (apenas do seu proprietário)
- [ ] Testar criação de chamado por usuário
- [ ] Testar criação de chamado por operador
- [ ] Testar visualização de chamados por diferentes roles

---

*Documento criado em 27/11/2025*

