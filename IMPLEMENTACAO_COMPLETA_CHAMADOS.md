# ✅ Implementação Completa - Sistema de Chamados API SIGA

Este documento descreve **TUDO** que foi implementado na API para o sistema de chamados, incluindo todas as validações, filtros e notificações.

---

## 📋 Índice

1. [Status da Implementação](#1-status-da-implementação)
2. [Funcionalidades Implementadas](#2-funcionalidades-implementadas)
3. [Validações e Segurança](#3-validações-e-segurança)
4. [Sistema de Notificações](#4-sistema-de-notificações)
5. [Endpoints Detalhados](#5-endpoints-detalhados)
6. [Fluxos de Dados](#6-fluxos-de-dados)
7. [Exemplos de Uso](#7-exemplos-de-uso)
8. [Arquivos Modificados](#8-arquivos-modificados)

---

## 1. Status da Implementação

| Funcionalidade | Status | Observação |
|----------------|--------|------------|
| ✅ Criação de chamados por usuários (`role = 'user'`) | **IMPLEMENTADO** | Usuários podem criar chamados |
| ✅ Criação de chamados por operadores (`role = 'operador'`) | **IMPLEMENTADO** | Operadores podem criar chamados |
| ✅ Validação de `proprietarioId` | **IMPLEMENTADO** | Sempre usa o do usuário autenticado |
| ✅ Filtro por `proprietarioId` na busca | **IMPLEMENTADO** | User/Operador vê apenas do seu proprietário |
| ✅ Notificação para Admin do Site | **IMPLEMENTADO** | Todos os admins recebem notificação |
| ✅ Notificação para Admin/Dono do Proprietário | **IMPLEMENTADO** | Admins/donos recebem notificação do seu proprietário |
| ✅ Preenchimento de `operadorId` para users | **IMPLEMENTADO** | Usa `uid` como `operadorId` para compatibilidade |
| ✅ Validação de acesso por `proprietarioId` | **IMPLEMENTADO** | Bloqueia acesso a chamados de outro proprietário |

---

## 2. Funcionalidades Implementadas

### 2.1. Criação de Chamados

**Quem pode criar:**
- ✅ **Usuários** (`role = 'user'`)
- ✅ **Operadores** (`role = 'operador'`)
- ✅ **Admin do site** (`role = 'admin'`) - não recomendado, mas permitido

**Validações aplicadas:**
1. ✅ Usuário deve existir no sistema
2. ✅ Usuário deve ter `proprietarioId` associado
3. ✅ `proprietarioId` do chamado é sempre o do usuário (ignora request)
4. ✅ Campos obrigatórios validados (título, descrição, tipo, prioridade)
5. ✅ Tipos e prioridades validados

**Comportamento:**
- **User**: `operadorId` = `uid` (para compatibilidade)
- **Operador**: `operadorId` = ID do operador vinculado
- **Admin**: Pode criar, mas não recomendado

### 2.2. Busca de Chamados

**Comportamento por Role:**

| Role | Comportamento |
|------|---------------|
| `admin` | Vê **TODOS** os chamados (pode filtrar com `?proprietarioId=xxx`) |
| `user` | Vê apenas chamados do **SEU proprietário** |
| `operador` | Vê apenas chamados do **proprietário do seu operador** |

**Filtros disponíveis:**
- `?proprietarioId=xxx` - Filtrar por proprietário (admin apenas)
- `?operadorId=xxx` - Filtrar por operador
- `?status=xxx` - Filtrar por status
- `?tipo=xxx` - Filtrar por tipo
- `?prioridade=xxx` - Filtrar por prioridade

### 2.3. Acesso a Chamado Específico

**Validação:**
- ✅ Admin: acesso liberado
- ✅ User/Operador: só se `chamado.proprietarioId == userProprietarioId`
- ✅ Bloqueia acesso a chamados de outro proprietário (403)

---

## 3. Validações e Segurança

### 3.1. POST /api/v1/chamados

**Validações implementadas:**

```java
// 1. Usuário existe
UserProfile userProfile = authService.getUserProfile(uid);
if (userProfile == null) {
    return 401 Unauthorized
}

// 2. Usuário tem proprietarioId
String userProprietarioId = authService.getProprietarioId(uid);
if (userProprietarioId == null || userProprietarioId.isEmpty()) {
    return 400 Bad Request - "Usuário não possui proprietário associado"
}

// 3. Definir proprietarioId no chamado (sempre do usuário)
chamado.setProprietarioId(userProprietarioId);

// 4. Preencher operadorId corretamente
if (role == "operador") {
    operadorId = operador.id;
} else if (role == "user") {
    operadorId = uid; // Compatibilidade
}

// 5. Validar no service
chamadoService.criarChamado(chamado, uid, userProfile);
```

**Validações no Service:**

```java
// VALIDAÇÃO CRÍTICA: proprietarioId é obrigatório
if (chamado.getProprietarioId() == null || chamado.getProprietarioId().trim().isEmpty()) {
    throw new RuntimeException("proprietarioId é obrigatório");
}

// VALIDAÇÃO CRÍTICA: Validar que o usuário pertence ao proprietário
if ("user".equalsIgnoreCase(userProfile.getRole())) {
    if (userProprietarioId == null || 
        !chamado.getProprietarioId().equals(userProprietarioId)) {
        throw new RuntimeException("Usuário não pode criar chamado para outro proprietário");
    }
}
```

### 3.2. GET /api/v1/chamados

**Validações implementadas:**

```java
if (authService.isAdmin(uid)) {
    // Admin: vê todos ou filtra
    if (proprietarioId != null) {
        chamados = chamadoService.getChamadosByProprietario(proprietarioId, ...);
    } else {
        chamados = chamadoService.getTodosChamados(...);
    }
} else {
    // User/Operador: filtrar por proprietarioId do usuário
    String userProprietarioId = authService.getProprietarioId(uid);
    
    // Validar se está tentando ver outro proprietário
    if (proprietarioId != null && !proprietarioId.equals(userProprietarioId)) {
        return 403 Forbidden
    }
    
    chamados = chamadoService.getChamadosByProprietario(userProprietarioId, ...);
}
```

### 3.3. GET /api/v1/chamados/{id}

**Validações implementadas:**

```java
Chamado chamado = chamadoService.getChamadoById(id);

if (!authService.isAdmin(uid)) {
    String userProprietarioId = authService.getProprietarioId(uid);
    
    if (chamado.getProprietarioId() == null || 
        !chamado.getProprietarioId().equals(userProprietarioId)) {
        return 403 Forbidden - "Acesso negado a este chamado"
    }
}
```

### 3.4. POST /api/v1/chamados/{id}/observacoes

**Validações implementadas:**

```java
// Verificar permissão por proprietarioId
if (!authService.isAdmin(uid)) {
    Chamado chamado = chamadoService.getChamadoById(id);
    String userProprietarioId = authService.getProprietarioId(uid);
    
    if (chamado.getProprietarioId() == null || 
        !chamado.getProprietarioId().equals(userProprietarioId)) {
        return 403 Forbidden
    }
}
```

### 3.5. POST /api/v1/chamados/{id}/fotos

**Validações implementadas:**

```java
// Verificar permissão por proprietarioId
if (!authService.isAdmin(uid)) {
    String userProprietarioId = authService.getProprietarioId(uid);
    
    if (chamado.getProprietarioId() == null || 
        !chamado.getProprietarioId().equals(userProprietarioId)) {
        return 403 Forbidden
    }
}
```

---

## 4. Sistema de Notificações

### 4.1. Notificação de Novo Chamado

**Implementação:**

```java
public void notificarNovoGhamado(String operadorNome, String chamadoId, 
                                 String titulo, String prioridade, 
                                 String proprietarioId) {
    // 1. Notificar Admin do Site (todos os admins com role = 'admin')
    List<UserProfile> siteAdmins = userProfileRepository.findAllByRole("admin");
    for (UserProfile admin : siteAdmins) {
        // Criar notificação para cada admin
    }
    
    // 2. Notificar Admin/Dono do Proprietário (permissao = 'admin' ou 'dono')
    if (proprietarioId != null) {
        List<UserProfile> proprietarioAdmins = 
            userProfileRepository.findByProprietarioIdAndPermissao(
                proprietarioId, 
                List.of("admin", "dono")
            );
        for (UserProfile admin : proprietarioAdmins) {
            // Criar notificação para cada admin/dono
        }
    }
}
```

**Quem recebe notificações:**

| Tipo | Quem Recebe | Quando |
|------|-------------|--------|
| **Admin do Site** | Todos os usuários com `role = 'admin'` | **Sempre** (todos os chamados) |
| **Admin do Proprietário** | Usuários com `proprietarioId = X` e `permissao = 'admin'` | Apenas chamados do **seu proprietário** |
| **Dono do Proprietário** | Usuários com `proprietarioId = X` e `permissao = 'dono'` | Apenas chamados do **seu proprietário** |

**Dados da notificação:**

```json
{
  "titulo": "Novo Chamado" ou "Novo Chamado URGENTE",
  "mensagem": "Operador {nome} abriu um chamado: {titulo}",
  "tipo": "info" ou "alerta" (se urgente),
  "categoria": "chamado",
  "dados": {
    "chamadoId": "xxx",
    "prioridade": "alta",
    "proprietarioId": "xxx"
  }
}
```

### 4.2. Métodos de Notificação Implementados

| Método | Descrição | Status |
|--------|-----------|--------|
| `notificarNovoGhamado()` | Notifica quando chamado é criado | ✅ Implementado |
| `notificarChamadoAssumido()` | Notifica quando chamado é assumido | ✅ Implementado |
| `notificarChamadoRespondido()` | Notifica quando chamado recebe resposta | ✅ Implementado |
| `notificarChamadoResolvido()` | Notifica quando chamado é resolvido | ✅ Implementado |
| `notificarChamadoCancelado()` | Notifica quando chamado é cancelado | ✅ Implementado |

---

## 5. Endpoints Detalhados

### 5.1. POST /api/v1/chamados

**Criar novo chamado**

**Headers:**
```
X-User-UID: {uid_do_usuario}
Content-Type: application/json
```

**Body:**
```json
{
  "titulo": "Problema no motor",
  "descricao": "Motor não está ligando",
  "tipo": "problema",
  "prioridade": "alta",
  "localizacao": {
    "latitude": -23.5505,
    "longitude": -46.6333,
    "accuracy": 10
  },
  "fazendaId": "fazenda_123",
  "talhaoId": "talhao_456",
  "maquinaId": "maquina_789"
}
```

**Resposta (201 Created):**
```json
{
  "id": "chamado_123",
  "titulo": "Problema no motor",
  "status": "aberto",
  "dataCriacao": "2025-11-27T10:00:00Z",
  "mensagem": "Chamado criado com sucesso"
}
```

**Validações:**
- ✅ Usuário existe
- ✅ Usuário tem `proprietarioId`
- ✅ Campos obrigatórios preenchidos
- ✅ Tipos e prioridades válidos
- ✅ `proprietarioId` definido automaticamente

**Notificações enviadas:**
- ✅ Admin do site
- ✅ Admin/Dono do proprietário

---

### 5.2. GET /api/v1/chamados

**Listar chamados**

**Headers:**
```
X-User-UID: {uid_do_usuario}
```

**Query Parameters (opcionais):**
- `proprietarioId` - Filtrar por proprietário (admin apenas)
- `operadorId` - Filtrar por operador
- `status` - Filtrar por status
- `tipo` - Filtrar por tipo
- `prioridade` - Filtrar por prioridade

**Exemplos:**

**Admin - Ver todos:**
```http
GET /api/v1/chamados
X-User-UID: uid_admin
```

**Admin - Filtrar por proprietário:**
```http
GET /api/v1/chamados?proprietarioId=CCnyN3MpHq5XRtnl8VFV
X-User-UID: uid_admin
```

**User/Operador - Ver seus chamados:**
```http
GET /api/v1/chamados
X-User-UID: uid_user
→ Retorna apenas chamados do proprietário do usuário
```

**User/Operador - Tentar ver outro proprietário (bloqueado):**
```http
GET /api/v1/chamados?proprietarioId=OUTRO_ID
X-User-UID: uid_user
→ 403 Forbidden
```

**Resposta (200 OK):**
```json
[
  {
    "id": "chamado_123",
    "titulo": "Problema no motor",
    "descricao": "Motor não está ligando",
    "tipo": "problema",
    "prioridade": "alta",
    "status": "aberto",
    "proprietarioId": "CCnyN3MpHq5XRtnl8VFV",
    "operadorId": "operador_456",
    "operadorNome": "João Silva",
    "dataHoraRegistro": "2025-11-27T10:00:00Z"
  }
]
```

---

### 5.3. GET /api/v1/chamados/{id}

**Buscar chamado específico**

**Headers:**
```
X-User-UID: {uid_do_usuario}
```

**Validações:**
- ✅ Chamado existe
- ✅ Admin: acesso liberado
- ✅ User/Operador: só se `chamado.proprietarioId == userProprietarioId`

**Resposta (200 OK):**
```json
{
  "id": "chamado_123",
  "titulo": "Problema no motor",
  "descricao": "Motor não está ligando",
  "tipo": "problema",
  "prioridade": "alta",
  "status": "aberto",
  "proprietarioId": "CCnyN3MpHq5XRtnl8VFV",
  "operadorId": "operador_456",
  "operadorNome": "João Silva",
  "dataHoraRegistro": "2025-11-27T10:00:00Z",
  "fotos": [],
  "observacoes": []
}
```

**Erro (403 Forbidden):**
```json
{
  "erro": "Acesso negado a este chamado"
}
```

---

### 5.4. PUT /api/v1/chamados/{id}

**Atualizar chamado**

**Headers:**
```
X-User-UID: {uid_do_usuario}
Content-Type: application/json
```

**Body:**
```json
{
  "status": "em_andamento",
  "responsavelId": "admin_123",
  "responsavelNome": "Admin",
  "prioridade": "urgente"
}
```

**Validações:**
- ✅ Chamado existe
- ✅ Admin: pode atualizar qualquer chamado
- ✅ User/Operador: só se `chamado.proprietarioId == userProprietarioId`

---

### 5.5. POST /api/v1/chamados/{id}/observacoes

**Adicionar observação**

**Headers:**
```
X-User-UID: {uid_do_usuario}
Content-Type: application/json
```

**Body:**
```json
{
  "texto": "Verificado o problema, será resolvido em breve"
}
```

**Validações:**
- ✅ Chamado existe
- ✅ Admin: pode adicionar observação
- ✅ User/Operador: só se `chamado.proprietarioId == userProprietarioId`

---

### 5.6. POST /api/v1/chamados/{id}/fotos

**Adicionar foto**

**Headers:**
```
X-User-UID: {uid_do_usuario}
Content-Type: multipart/form-data
```

**Body:**
```
file: [arquivo de imagem]
```

**Validações:**
- ✅ Chamado existe
- ✅ Admin: pode adicionar foto
- ✅ User/Operador: só se `chamado.proprietarioId == userProprietarioId`

**Resposta (200 OK):**
```json
{
  "url": "https://i.ibb.co/xxx/foto.jpg",
  "fotoId": "foto_123"
}
```

---

### 5.7. DELETE /api/v1/chamados/{id}

**Deletar chamado**

**Headers:**
```
X-User-UID: {uid_do_usuario}
```

**Validações:**
- ✅ Chamado existe
- ✅ Admin: pode deletar qualquer chamado
- ✅ User/Operador: só se `chamado.proprietarioId == userProprietarioId`

---

## 6. Fluxos de Dados

### 6.1. Criar Chamado (User)

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO DE CRIAÇÃO (USER)                   │
│                                                              │
│  1. App envia POST /api/v1/chamados                          │
│     Header: X-User-UID: uid_user                             │
│     Body: { titulo, descricao, tipo, prioridade }           │
│                                                              │
│  2. Controller busca UserProfile                             │
│     → role: "user"                                            │
│     → proprietarioId: "CCnyN3MpHq5XRtnl8VFV"                  │
│                                                              │
│  3. Controller valida proprietarioId                        │
│     → Se null: retorna 400                                   │
│     → Se válido: continua                                     │
│                                                              │
│  4. Controller preenche dados                               │
│     → operadorId = uid (compatibilidade)                     │
│     → operadorNome = displayName ou email                     │
│     → proprietarioId = userProprietarioId                    │
│                                                              │
│  5. Service valida novamente                                │
│     → Verifica proprietarioId                                │
│     → Verifica que usuário pertence ao proprietário           │
│                                                              │
│  6. Repository salva no Firestore                            │
│     → Chamado criado com proprietarioId correto              │
│                                                              │
│  7. Notificações enviadas                                   │
│     → Admin do site (todos)                                  │
│     → Admin/Dono do proprietário (apenas do proprietário)   │
│                                                              │
│  8. Retorna resposta 201 Created                            │
└─────────────────────────────────────────────────────────────┘
```

### 6.2. Criar Chamado (Operador)

```
┌─────────────────────────────────────────────────────────────┐
│                 FLUXO DE CRIAÇÃO (OPERADOR)                   │
│                                                              │
│  1. App envia POST /api/v1/chamados                          │
│     Header: X-User-UID: uid_operador                         │
│                                                              │
│  2. Controller busca UserProfile                             │
│     → role: "operador"                                        │
│     → operadorId: "operador_123"                              │
│                                                              │
│  3. Controller busca Operador                               │
│     → operador.proprietarioId: "CCnyN3MpHq5XRtnl8VFV"         │
│                                                              │
│  4. Controller preenche dados                               │
│     → operadorId = operador.id                               │
│     → operadorNome = operador.nome                           │
│     → proprietarioId = operador.proprietarioId               │
│                                                              │
│  5. Service valida e salva                                  │
│                                                              │
│  6. Notificações enviadas                                   │
│     → Admin do site                                          │
│     → Admin/Dono do proprietário                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.3. Buscar Chamados (User)

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO DE BUSCA (USER)                     │
│                                                              │
│  1. App envia GET /api/v1/chamados                            │
│     Header: X-User-UID: uid_user                             │
│                                                              │
│  2. Controller identifica role                               │
│     → role: "user"                                            │
│                                                              │
│  3. Controller obtém proprietarioId                          │
│     → userProprietarioId = "CCnyN3MpHq5XRtnl8VFV"            │
│                                                              │
│  4. Service busca chamados                                   │
│     → chamadoRepository.findByProprietarioId(userProprietarioId)│
│                                                              │
│  5. Repository faz query no Firestore                        │
│     → WHERE proprietarioId = "CCnyN3MpHq5XRtnl8VFV"          │
│                                                              │
│  6. Retorna apenas chamados do proprietário                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. Exemplos de Uso

### 7.1. User - Criar Chamado

```http
POST /api/v1/chamados
X-User-UID: uid_user_danilela
Content-Type: application/json

{
  "titulo": "Problema no motor",
  "descricao": "Motor não está ligando",
  "tipo": "problema",
  "prioridade": "alta"
}
```

**Processamento:**
1. API identifica: `proprietarioId = "CCnyN3MpHq5XRtnl8VFV"` (Danilela)
2. API cria chamado com `proprietarioId = "CCnyN3MpHq5XRtnl8VFV"`
3. API define `operadorId = uid_user_danilela` (compatibilidade)
4. ✅ Chamado criado
5. ✅ Notificações enviadas

**Resposta:**
```json
{
  "id": "chamado_123",
  "titulo": "Problema no motor",
  "status": "aberto",
  "mensagem": "Chamado criado com sucesso"
}
```

---

### 7.2. Operador - Criar Chamado

```http
POST /api/v1/chamados
X-User-UID: uid_operador_joao
Content-Type: application/json

{
  "titulo": "Falha na colheitadeira",
  "descricao": "Colheitadeira parou de funcionar",
  "tipo": "problema",
  "prioridade": "urgente"
}
```

**Processamento:**
1. API identifica: `role = "operador"`
2. API busca operador: `operador.proprietarioId = "CCnyN3MpHq5XRtnl8VFV"`
3. API cria chamado com `proprietarioId = "CCnyN3MpHq5XRtnl8VFV"`
4. API define `operadorId = operador.id`
5. ✅ Chamado criado
6. ✅ Notificações enviadas

---

### 7.3. User - Buscar Chamados

```http
GET /api/v1/chamados
X-User-UID: uid_user_danilela
```

**Resposta:**
```json
[
  {
    "id": "chamado_123",
    "titulo": "Problema no motor",
    "proprietarioId": "CCnyN3MpHq5XRtnl8VFV",
    "status": "aberto"
  }
]
```

**Nota:** Retorna apenas chamados do proprietário "CCnyN3MpHq5XRtnl8VFV"

---

### 7.4. Admin - Ver Todos

```http
GET /api/v1/chamados
X-User-UID: uid_admin
```

**Resposta:** Todos os chamados de todos os proprietários

---

### 7.5. Admin - Filtrar por Proprietário

```http
GET /api/v1/chamados?proprietarioId=CCnyN3MpHq5XRtnl8VFV
X-User-UID: uid_admin
```

**Resposta:** Apenas chamados de Danilela

---

### 7.6. User - Tentar Ver Outro Proprietário (Bloqueado)

```http
GET /api/v1/chamados?proprietarioId=OUTRO_PROPRIETARIO_ID
X-User-UID: uid_user_danilela
```

**Resposta:**
```json
{
  "erro": "Sem permissão para acessar chamados de outro proprietário"
}
```
**Status:** `403 Forbidden`

---

## 8. Arquivos Modificados

### 8.1. Controller

**Arquivo:** `src/main/java/com/siga/controller/ChamadoController.java`

**Mudanças:**
- ✅ Aceita criação por usuários (`role = 'user'`)
- ✅ Preenche `operadorId` corretamente para users (usa `uid`)
- ✅ Valida `proprietarioId` em todos os endpoints
- ✅ Filtra por `proprietarioId` na busca
- ✅ Valida acesso por `proprietarioId` em GET, PUT, POST observações, POST fotos

---

### 8.2. Service

**Arquivo:** `src/main/java/com/siga/service/ChamadoService.java`

**Mudanças:**
- ✅ Método `criarChamado()` atualizado para receber `userUid` e `userProfile`
- ✅ Validação dupla de `proprietarioId`
- ✅ Validação de que usuário pertence ao proprietário
- ✅ Passa `proprietarioId` para notificação

---

### 8.3. Notificação Service

**Arquivo:** `src/main/java/com/siga/service/NotificacaoService.java`

**Mudanças:**
- ✅ Método `notificarNovoGhamado()` atualizado para receber `proprietarioId`
- ✅ Notifica Admin do Site (todos os admins)
- ✅ Notifica Admin/Dono do Proprietário (apenas do proprietário)
- ✅ Logs detalhados de notificações

---

### 8.4. Repository

**Arquivo:** `src/main/java/com/siga/repository/UserProfileRepository.java`

**Mudanças:**
- ✅ Método `findAllByRole()` atualizado para definir `uid` corretamente
- ✅ **NOVO:** Método `findByProprietarioIdAndPermissao()` para buscar admins/donos do proprietário

---

## 📊 Resumo Final

| Requisito | Status | Implementação |
|-----------|--------|-----------------|
| Aceitar criação por users | ✅ | Controller aceita `role = 'user'` |
| Preencher operadorId para users | ✅ | Usa `uid` como `operadorId` |
| Validar proprietarioId | ✅ | Validação dupla (Controller + Service) |
| Filtrar por proprietarioId | ✅ | Filtro automático para user/operador |
| Notificar admin do site | ✅ | Todos os admins recebem notificação |
| Notificar admin do proprietário | ✅ | Admins/donos recebem notificação do seu proprietário |
| Bloquear acesso a outro proprietário | ✅ | Retorna 403 Forbidden |

---

## ✅ Checklist de Testes

- [x] Testar criação de chamado por user
- [x] Testar criação de chamado por operador
- [x] Testar busca de chamados por user (deve ver apenas do seu proprietário)
- [x] Testar busca de chamados por operador (deve ver apenas do seu proprietário)
- [x] Testar busca de chamados por admin (deve ver todos)
- [x] Testar tentativa de ver outro proprietário (deve bloquear)
- [x] Testar notificações para admin do site
- [x] Testar notificações para admin do proprietário
- [x] Testar acesso a chamado específico (validação por proprietarioId)
- [x] Testar adicionar observação (validação por proprietarioId)
- [x] Testar adicionar foto (validação por proprietarioId)

---

*Documento criado em 27/11/2025 - Implementação completa e funcional*

