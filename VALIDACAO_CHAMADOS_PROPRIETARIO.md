# 🔒 Validação de Chamados por Proprietário - API SIGA

Este documento descreve a implementação completa de validação e filtro de chamados por `proprietarioId`, garantindo que usuários só vejam e criem chamados do seu proprietário.

---

## ✅ STATUS: IMPLEMENTADO

Todas as validações foram aplicadas com sucesso.

---

## 📋 Índice

1. [Estrutura de Chamados](#1-estrutura-de-chamados)
2. [Regras de Negócio](#2-regras-de-negócio)
3. [Validações Implementadas](#3-validações-implementadas)
4. [Endpoints Atualizados](#4-endpoints-atualizados)
5. [Fluxo de Dados](#5-fluxo-de-dados)
6. [Exemplos de Uso](#6-exemplos-de-uso)
7. [Tratamento de Erros](#7-tratamento-de-erros)

---

## 1. Estrutura de Chamados

### Campos Obrigatórios

Todos os chamados **DEVEM** ter:

```java
{
  // Identificação do criador
  userId: string;              // UID do Firebase Auth
  operadorId: string;          // ID do operador (se role = 'operador')
  operadorNome: string;        // Nome do operador
  
  // Proprietário (OBRIGATÓRIO)
  proprietarioId: string;      // ID do proprietário - SEMPRE deve existir
  
  // Dados do chamado
  titulo: string;
  descricao: string;
  tipo: 'manutencao' | 'problema' | 'suporte' | 'outro';
  prioridade: 'baixa' | 'media' | 'alta' | 'urgente';
  status: 'aberto' | 'em_andamento' | 'resolvido' | 'cancelado';
  
  // Timestamps
  dataHoraRegistro: Timestamp;
  dataHoraEnvio: Timestamp;
}
```

### Campo Crítico: `proprietarioId`

- **Sempre obrigatório** ao criar chamado
- **Sempre validado** ao buscar/atualizar chamado
- **Nunca pode ser alterado** pelo usuário (só pelo sistema)

---

## 2. Regras de Negócio

### 2.1. Criação de Chamado

| Role | Comportamento |
|------|---------------|
| `admin` | Pode criar chamado para qualquer proprietário (não recomendado) |
| `user` | Só pode criar chamado para **SEU proprietário** |
| `operador` | Só pode criar chamado para o **proprietário do seu operador** |

**Fluxo:**
```
1. Usuário envia requisição POST /api/v1/chamados
2. API busca UserProfile do usuário
3. API identifica proprietarioId do usuário
4. API valida que proprietarioId está correto
5. API cria chamado com proprietarioId do usuário
```

### 2.2. Busca de Chamados

| Role | Comportamento |
|------|---------------|
| `admin` | Vê **TODOS** (pode filtrar com `?proprietarioId=xxx`) |
| `user` | Vê apenas do **SEU proprietário** |
| `operador` | Vê apenas do **proprietário do seu operador** |

**Fluxo:**
```
1. Usuário envia requisição GET /api/v1/chamados
2. API identifica proprietarioId do usuário
3. API busca chamados WHERE proprietarioId = userProprietarioId
4. API retorna apenas chamados permitidos
```

### 2.3. Acesso a Chamado Específico

| Role | Comportamento |
|------|---------------|
| `admin` | Pode acessar **qualquer** chamado |
| `user/operador` | Só pode acessar se `chamado.proprietarioId == userProprietarioId` |

---

## 3. Validações Implementadas

### 3.1. POST /api/v1/chamados

**Validações:**
1. ✅ Usuário existe
2. ✅ Usuário tem `proprietarioId` associado
3. ✅ `proprietarioId` do chamado é o mesmo do usuário
4. ✅ Campos obrigatórios preenchidos
5. ✅ Tipos e prioridades válidos

**Código:**
```java
// 1. Buscar perfil do usuário
UserProfile userProfile = authService.getUserProfile(uid);
if (userProfile == null) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("erro", "Usuário não encontrado"));
}

// 2. Obter proprietarioId do usuário
String userProprietarioId = authService.getProprietarioId(uid);

if (userProprietarioId == null || userProprietarioId.isEmpty()) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("erro", "Usuário não possui proprietário associado"));
}

// 3. Definir proprietarioId no chamado (sempre do usuário)
chamado.setProprietarioId(userProprietarioId);

// 4. Validar no service
chamadoService.criarChamado(chamado, uid, userProfile);
```

### 3.2. GET /api/v1/chamados

**Validações:**
1. ✅ Admin: vê todos ou filtra por `proprietarioId`
2. ✅ User/Operador: filtra automaticamente por `proprietarioId` do usuário
3. ✅ Bloqueia tentativa de ver outro proprietário (403)

**Código:**
```java
if (authService.isAdmin(uid)) {
    // Admin: vê tudo ou filtra
    if (proprietarioId != null && !proprietarioId.isEmpty()) {
        chamados = chamadoService.getChamadosByProprietario(proprietarioId, ...);
    } else {
        chamados = chamadoService.getTodosChamados(...);
    }
} else {
    // User/Operador: filtrar por proprietarioId do usuário
    String userProprietarioId = authService.getProprietarioId(uid);
    
    if (userProprietarioId == null) {
        return ResponseEntity.ok(Collections.emptyList());
    }
    
    // Validar se está tentando ver outro proprietário
    if (proprietarioId != null && !proprietarioId.equals(userProprietarioId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", "Sem permissão para acessar chamados de outro proprietário"));
    }
    
    chamados = chamadoService.getChamadosByProprietario(userProprietarioId, ...);
}
```

### 3.3. GET /api/v1/chamados/{id}

**Validações:**
1. ✅ Chamado existe
2. ✅ Admin: acesso liberado
3. ✅ User/Operador: só se `chamado.proprietarioId == userProprietarioId`

**Código:**
```java
Chamado chamado = chamadoService.getChamadoById(id);

if (!authService.isAdmin(uid)) {
    String userProprietarioId = authService.getProprietarioId(uid);
    
    if (userProprietarioId == null) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", "Usuário sem proprietário associado"));
    }
    
    if (chamado.getProprietarioId() == null || 
        !chamado.getProprietarioId().equals(userProprietarioId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", "Acesso negado a este chamado"));
    }
}
```

### 3.4. Service: criarChamado()

**Validações adicionais:**
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

---

## 4. Endpoints Atualizados

### 4.1. POST /api/v1/chamados

**Antes:**
- ❌ Não validava `proprietarioId`
- ❌ Aceitava qualquer `proprietarioId` do request

**Depois:**
- ✅ Valida que usuário tem `proprietarioId`
- ✅ Sempre usa `proprietarioId` do usuário (ignora do request)
- ✅ Valida no service

### 4.2. GET /api/v1/chamados

**Antes:**
- ❌ Operador via apenas seus chamados
- ❌ User não tinha acesso
- ❌ Não filtrava por `proprietarioId`

**Depois:**
- ✅ Admin: vê todos ou filtra por `proprietarioId`
- ✅ User/Operador: vê todos do seu proprietário
- ✅ Bloqueia tentativa de ver outro proprietário

### 4.3. GET /api/v1/chamados/{id}

**Antes:**
- ❌ Validava apenas por `operadorId`
- ❌ User não tinha acesso

**Depois:**
- ✅ Valida por `proprietarioId`
- ✅ User pode acessar chamados do seu proprietário
- ✅ Operador pode acessar chamados do proprietário do seu operador

### 4.4. POST /api/v1/chamados/{id}/observacoes

**Antes:**
- ❌ Não validava permissão

**Depois:**
- ✅ Valida por `proprietarioId` antes de adicionar observação

### 4.5. POST /api/v1/chamados/{id}/fotos

**Antes:**
- ❌ Validava apenas por `operadorId`

**Depois:**
- ✅ Valida por `proprietarioId`

---

## 5. Fluxo de Dados

### 5.1. Criar Chamado

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO DE CRIAÇÃO                          │
│                                                              │
│  1. App envia POST /api/v1/chamados                         │
│     Body: { titulo, descricao, tipo, prioridade, ... }      │
│     Header: X-User-UID: uid_usuario                         │
│                                                              │
│  2. Controller busca UserProfile                             │
│     → role: "user" ou "operador"                            │
│     → proprietarioId: "CCnyN3MpHq5XRtnl8VFV"                 │
│                                                              │
│  3. Controller valida proprietarioId                         │
│     → Se null: retorna 400 Bad Request                       │
│     → Se válido: continua                                    │
│                                                              │
│  4. Controller define proprietarioId no chamado              │
│     chamado.setProprietarioId(userProprietarioId)            │
│                                                              │
│  5. Service valida novamente                                 │
│     → Verifica que proprietarioId está definido              │
│     → Verifica que usuário pertence ao proprietário          │
│                                                              │
│  6. Repository salva no Firestore                            │
│     → Chamado criado com proprietarioId correto              │
│                                                              │
│  7. Notificação enviada para admins                          │
└─────────────────────────────────────────────────────────────┘
```

### 5.2. Buscar Chamados

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO DE BUSCA                             │
│                                                              │
│  1. App envia GET /api/v1/chamados                           │
│     Header: X-User-UID: uid_usuario                          │
│                                                              │
│  2. Controller identifica role                                │
│     → Se admin: busca todos ou filtra                        │
│     → Se user/operador: filtra por proprietarioId            │
│                                                              │
│  3. Controller obtém proprietarioId do usuário                │
│     userProprietarioId = authService.getProprietarioId(uid)  │
│                                                              │
│  4. Service busca chamados                                   │
│     chamadoRepository.findByProprietarioId(userProprietarioId)│
│                                                              │
│  5. Repository faz query no Firestore                        │
│     WHERE proprietarioId = userProprietarioId                │
│                                                              │
│  6. Retorna apenas chamados do proprietário                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Exemplos de Uso

### 6.1. User - Criar Chamado

```http
POST /api/v1/chamados
X-User-UID: uid_do_usuario_danilela
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
3. ✅ Chamado criado

**Resposta:**
```json
{
  "id": "chamado_123",
  "titulo": "Problema no motor",
  "status": "aberto",
  "proprietarioId": "CCnyN3MpHq5XRtnl8VFV",
  "mensagem": "Chamado criado com sucesso"
}
```

### 6.2. User - Buscar Chamados

```http
GET /api/v1/chamados
X-User-UID: uid_do_usuario_danilela
```

**Processamento:**
1. API identifica: `proprietarioId = "CCnyN3MpHq5XRtnl8VFV"` (Danilela)
2. API busca: `WHERE proprietarioId = "CCnyN3MpHq5XRtnl8VFV"`

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

### 6.3. User - Tentar Ver Outro Proprietário (Bloqueado)

```http
GET /api/v1/chamados?proprietarioId=OUTRO_PROPRIETARIO_ID
X-User-UID: uid_do_usuario_danilela
```

**Resposta:**
```json
{
  "erro": "Sem permissão para acessar chamados de outro proprietário"
}
```
**Status:** `403 Forbidden`

### 6.4. Admin - Ver Todos

```http
GET /api/v1/chamados
X-User-UID: uid_do_admin
```

**Resposta:** Todos os chamados

### 6.5. Admin - Filtrar por Proprietário

```http
GET /api/v1/chamados?proprietarioId=CCnyN3MpHq5XRtnl8VFV
X-User-UID: uid_do_admin
```

**Resposta:** Apenas chamados de Danilela

---

## 7. Tratamento de Erros

### 7.1. Erros Comuns

| Erro | Causa | Solução |
|------|-------|---------|
| `403 Forbidden` | Tentando acessar outro proprietário | Usar apenas seu próprio `proprietarioId` |
| `400 Bad Request - Usuário não possui proprietário associado` | UserProfile sem `proprietarioId` | Associar usuário a um proprietário no Firebase |
| `400 Bad Request - proprietarioId é obrigatório` | Chamado criado sem `proprietarioId` | API sempre define automaticamente |
| `403 Forbidden - Acesso negado a este chamado` | Chamado de outro proprietário | Verificar `proprietarioId` do chamado |

### 7.2. Logs Esperados

**Criação bem-sucedida:**
```
🌐 Controller: Recebida requisição POST /api/v1/chamados
🔐 UID: uid_usuario
👤 Controller: UserProfile - role: user, proprietarioId: CCnyN3MpHq5XRtnl8VFV
✅ Service: Validações passadas - proprietarioId: CCnyN3MpHq5XRtnl8VFV
✅ Service: Chamado criado com sucesso - ID: chamado_123
✅ Controller: Chamado criado - ID: chamado_123
```

**Busca bem-sucedida:**
```
🌐 Controller: Recebida requisição GET /api/v1/chamados
🔐 UID: uid_usuario
🔍 Controller: Filtrando por proprietarioId: CCnyN3MpHq5XRtnl8VFV
✅ Controller: Retornando 5 chamados do proprietário
```

**Tentativa de acesso negado:**
```
🌐 Controller: Recebida requisição GET /api/v1/chamados
🔐 UID: uid_usuario
⚠️ Controller: Tentativa de acessar outro proprietário
```

---

## 📊 Resumo de Segurança

| Antes | Depois |
|-------|--------|
| ❌ Não validava `proprietarioId` | ✅ Sempre valida `proprietarioId` |
| ❌ User não tinha acesso | ✅ User vê chamados do seu proprietário |
| ❌ Operador via apenas seus chamados | ✅ Operador vê todos do seu proprietário |
| ❌ Podia criar chamado para outro proprietário | ✅ Sempre cria para seu próprio proprietário |
| ❌ Validação apenas por `operadorId` | ✅ Validação por `proprietarioId` |

---

## 🚀 Próximos Passos

1. **Migração de Chamados Antigos** (se necessário):
   - Criar script para adicionar `proprietarioId` em chamados antigos
   - Usar `operador.proprietarioId` como fonte

2. **Testes:**
   - Testar com user comum
   - Testar com operador
   - Testar com admin
   - Testar tentativas de acesso negado

3. **Monitoramento:**
   - Verificar logs de validação
   - Verificar erros 403
   - Verificar chamados sem `proprietarioId`

---

*Documento gerado em 27/11/2025 para o projeto SIGA*

