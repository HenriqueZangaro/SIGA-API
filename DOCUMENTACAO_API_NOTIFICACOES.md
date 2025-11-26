# 📱 Documentação da API - Sistema de Notificações e Chamados

Este documento descreve exatamente o que precisa ser implementado na API Spring Boot (`SIGA-API`) para que o sistema de notificações e chamados funcione corretamente no aplicativo móvel.

---

## 📋 Índice

1. [Endpoints de Notificações](#1-endpoints-de-notificações)
2. [Endpoints de Chamados (Novos/Atualizações)](#2-endpoints-de-chamados-novosatualizações)
3. [Modelos de Dados](#3-modelos-de-dados)
4. [Regras de Negócio](#4-regras-de-negócio)
5. [Firebase Security Rules](#5-firebase-security-rules)

---

## 1. Endpoints de Notificações

### Criar Controller: `NotificacaoController.java`

```java
@RestController
@RequestMapping("/api/v1/notificacoes")
public class NotificacaoController {
    // Implementar endpoints abaixo
}
```

### 1.1. Listar Notificações do Usuário

```
GET /api/v1/notificacoes
Header: X-User-UID: {uid do usuário}
```

**Descrição:** Retorna todas as notificações do usuário autenticado, ordenadas por data (mais recentes primeiro).

**Resposta (200 OK):**
```json
[
  {
    "id": "notif_123",
    "userId": "uid_operador",
    "titulo": "Chamado Respondido",
    "mensagem": "Seu chamado 'Problema no motor' recebeu uma resposta.",
    "tipo": "info",
    "categoria": "chamado",
    "lida": false,
    "dados": {
      "chamadoId": "chamado_456"
    },
    "createdAt": "2025-11-26T10:30:00Z",
    "updatedAt": "2025-11-26T10:30:00Z"
  }
]
```

---

### 1.2. Listar Notificações Não Lidas

```
GET /api/v1/notificacoes/nao-lidas
Header: X-User-UID: {uid do usuário}
```

**Descrição:** Retorna apenas notificações não lidas do usuário.

**Resposta (200 OK):** Mesmo formato da listagem geral, filtrado por `lida: false`.

---

### 1.3. Contar Notificações Não Lidas

```
GET /api/v1/notificacoes/count
Header: X-User-UID: {uid do usuário}
```

**Resposta (200 OK):**
```json
{
  "count": 5
}
```

---

### 1.4. Criar Notificação

```
POST /api/v1/notificacoes
Header: X-User-UID: {uid do usuário}
Content-Type: application/json
```

**Body:**
```json
{
  "userId": "uid_destinatario",
  "titulo": "Novo Chamado",
  "mensagem": "Operador João abriu um chamado urgente.",
  "tipo": "alerta",
  "categoria": "chamado",
  "dados": {
    "chamadoId": "chamado_789",
    "prioridade": "urgente"
  }
}
```

**Campos:**
| Campo | Tipo | Obrigatório | Valores Permitidos |
|-------|------|-------------|-------------------|
| userId | string | Sim | UID do destinatário ou "admin" para todos admins |
| titulo | string | Sim | - |
| mensagem | string | Sim | - |
| tipo | string | Sim | "info", "sucesso", "alerta", "erro" |
| categoria | string | Sim | "chamado", "sistema", "ponto", "geral" |
| dados | object | Não | Dados extras (chamadoId, etc) |

**Regra Especial:** Se `userId === "admin"`, a API deve criar uma notificação para **todos os usuários com role "admin"**.

**Resposta (201 Created):**
```json
{
  "id": "notif_new_123",
  "userId": "uid_destinatario",
  "titulo": "Novo Chamado",
  "mensagem": "Operador João abriu um chamado urgente.",
  "tipo": "alerta",
  "categoria": "chamado",
  "lida": false,
  "dados": {
    "chamadoId": "chamado_789",
    "prioridade": "urgente"
  },
  "createdAt": "2025-11-26T14:00:00Z"
}
```

---

### 1.5. Marcar Notificação como Lida

```
PUT /api/v1/notificacoes/{id}/lida
Header: X-User-UID: {uid do usuário}
```

**Resposta (200 OK):**
```json
{
  "mensagem": "Notificação marcada como lida"
}
```

---

### 1.6. Marcar Todas como Lidas

```
PUT /api/v1/notificacoes/lidas
Header: X-User-UID: {uid do usuário}
```

**Descrição:** Marca todas as notificações do usuário como lidas.

**Resposta (200 OK):**
```json
{
  "mensagem": "Todas as notificações foram marcadas como lidas",
  "atualizadas": 5
}
```

---

### 1.7. Deletar Notificação

```
DELETE /api/v1/notificacoes/{id}
Header: X-User-UID: {uid do usuário}
```

**Resposta (200 OK):**
```json
{
  "mensagem": "Notificação deletada com sucesso"
}
```

---

### 1.8. Enviar para Múltiplos Usuários (Admin)

```
POST /api/v1/notificacoes/batch
Header: X-User-UID: {uid do admin}
Content-Type: application/json
```

**Body:**
```json
{
  "userIds": ["uid_1", "uid_2", "uid_3"],
  "titulo": "Manutenção Programada",
  "mensagem": "O sistema ficará indisponível das 22h às 23h.",
  "tipo": "alerta",
  "categoria": "sistema"
}
```

**Resposta (201 Created):**
```json
{
  "mensagem": "Notificações enviadas",
  "enviadas": 3
}
```

---

## 2. Endpoints de Chamados (Novos/Atualizações)

### 2.1. Adicionar Observação ao Chamado

```
POST /api/v1/chamados/{id}/observacoes
Header: X-User-UID: {uid do admin}
Content-Type: application/json
```

**Body:**
```json
{
  "observacao": "Estamos analisando o problema. Em breve enviaremos um técnico."
}
```

**Resposta (200 OK):**
```json
{
  "mensagem": "Observação adicionada com sucesso",
  "observacao": {
    "texto": "Estamos analisando o problema...",
    "autor": "Admin João",
    "autorId": "uid_admin",
    "data": "2025-11-26T15:00:00Z"
  }
}
```

**Estrutura da Observação no Chamado:**
```json
{
  "id": "chamado_123",
  "titulo": "Problema no motor",
  "observacoes": [
    {
      "texto": "Estamos analisando o problema...",
      "autor": "Admin João",
      "autorId": "uid_admin",
      "data": "2025-11-26T15:00:00Z"
    },
    {
      "texto": "Técnico será enviado amanhã às 8h.",
      "autor": "Admin Maria",
      "autorId": "uid_admin_2",
      "data": "2025-11-26T16:30:00Z"
    }
  ]
}
```

---

### 2.2. Atualizar Status do Chamado (Admin)

```
PUT /api/v1/chamados/{id}
Header: X-User-UID: {uid do admin}
Content-Type: application/json
```

**Body:**
```json
{
  "status": "em_andamento",
  "responsavelId": "uid_admin",
  "responsavelNome": "Admin João"
}
```

**Campos Atualizáveis:**
| Campo | Tipo | Descrição |
|-------|------|-----------|
| status | string | "aberto", "em_andamento", "resolvido", "cancelado" |
| responsavelId | string | UID do admin responsável |
| responsavelNome | string | Nome do admin responsável |
| prioridade | string | "baixa", "media", "alta", "urgente" |

---

## 3. Modelos de Dados

### 3.1. Notificacao (Firestore Collection: `notificacoes`)

```java
public class Notificacao {
    private String id;
    private String userId;           // UID do destinatário
    private String titulo;
    private String mensagem;
    private String tipo;             // info, sucesso, alerta, erro
    private String categoria;        // chamado, sistema, ponto, geral
    private boolean lida;
    private Map<String, Object> dados; // Dados extras (chamadoId, etc)
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
```

### 3.2. Chamado (Atualização)

Adicionar campo `observacoes` se ainda não existir:

```java
public class Chamado {
    // ... campos existentes ...
    
    private String responsavelId;    // UID do admin que assumiu
    private String responsavelNome;  // Nome do admin
    private List<Observacao> observacoes; // Lista de respostas/observações
}

public class Observacao {
    private String texto;
    private String autor;
    private String autorId;
    private Timestamp data;
}
```

---

## 4. Regras de Negócio

### 4.1. Fluxo de Notificações

```
┌─────────────────────────────────────────────────────────────────┐
│                    OPERADOR CRIA CHAMADO                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  API cria notificação para TODOS os admins (userId = "admin")   │
│  - tipo: "alerta" se urgente, "info" se não                     │
│  - categoria: "chamado"                                          │
│  - dados: { chamadoId, prioridade }                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ADMIN ASSUME CHAMADO                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  API cria notificação para o OPERADOR que criou                 │
│  - titulo: "Chamado em Atendimento"                              │
│  - tipo: "info"                                                  │
│  - dados: { chamadoId }                                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ADMIN RESPONDE CHAMADO                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  API cria notificação para o OPERADOR                           │
│  - titulo: "Chamado Respondido"                                  │
│  - tipo: "info"                                                  │
│  - dados: { chamadoId }                                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ADMIN RESOLVE CHAMADO                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  API cria notificação para o OPERADOR                           │
│  - titulo: "Chamado Resolvido"                                   │
│  - tipo: "sucesso"                                               │
│  - dados: { chamadoId }                                          │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2. Permissões por Role

| Ação | admin | user | operador |
|------|-------|------|----------|
| Ver todos os chamados | ✅ | ❌ | ❌ |
| Ver próprios chamados | ✅ | ❌ | ✅ |
| Criar chamado | ❌ | ❌ | ✅ |
| Responder chamado | ✅ | ❌ | ❌ |
| Assumir chamado | ✅ | ❌ | ❌ |
| Resolver chamado | ✅ | ❌ | ❌ |
| Ver notificações | ✅ | ✅ | ✅ |
| Enviar notificação batch | ✅ | ❌ | ❌ |

---

## 5. Firebase Security Rules

Adicione estas regras ao seu Firestore:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Notificações - usuário só vê as suas
    match /notificacoes/{notificacaoId} {
      allow read: if request.auth != null && 
                  resource.data.userId == request.auth.uid;
      allow create: if request.auth != null;
      allow update: if request.auth != null && 
                    resource.data.userId == request.auth.uid;
      allow delete: if request.auth != null && 
                    resource.data.userId == request.auth.uid;
    }
    
    // Chamados
    match /chamados/{chamadoId} {
      // Admin pode ver todos, operador só os seus
      allow read: if request.auth != null && (
        isAdmin() || 
        resource.data.userId == request.auth.uid ||
        resource.data.operadorId == request.auth.uid
      );
      // Operador pode criar
      allow create: if request.auth != null;
      // Admin pode atualizar qualquer, operador só os seus em status 'aberto'
      allow update: if request.auth != null && (
        isAdmin() ||
        (resource.data.userId == request.auth.uid && resource.data.status == 'aberto')
      );
      // Admin pode deletar qualquer, operador só os seus em status 'aberto'
      allow delete: if request.auth != null && (
        isAdmin() ||
        (resource.data.userId == request.auth.uid && resource.data.status == 'aberto')
      );
    }
    
    // Função auxiliar para verificar se é admin
    function isAdmin() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}
```

---

## 📝 Checklist de Implementação

### Backend (SIGA-API)

- [ ] Criar model `Notificacao.java`
- [ ] Criar model `Observacao.java` (para chamados)
- [ ] Criar repository `NotificacaoRepository.java`
- [ ] Criar service `NotificacaoService.java`
- [ ] Criar controller `NotificacaoController.java`
- [ ] Implementar `GET /api/v1/notificacoes`
- [ ] Implementar `GET /api/v1/notificacoes/nao-lidas`
- [ ] Implementar `GET /api/v1/notificacoes/count`
- [ ] Implementar `POST /api/v1/notificacoes`
- [ ] Implementar `PUT /api/v1/notificacoes/{id}/lida`
- [ ] Implementar `PUT /api/v1/notificacoes/lidas`
- [ ] Implementar `DELETE /api/v1/notificacoes/{id}`
- [ ] Implementar `POST /api/v1/notificacoes/batch`
- [ ] Atualizar `ChamadoController` para incluir `POST /{id}/observacoes`
- [ ] Atualizar model `Chamado` para incluir `observacoes`, `responsavelId`, `responsavelNome`
- [ ] Configurar CORS para permitir requisições do app

### Firebase

- [ ] Criar collection `notificacoes` no Firestore
- [ ] Atualizar Security Rules do Firestore
- [ ] Verificar índices necessários

### Testes

- [ ] Testar criação de notificação
- [ ] Testar listagem de notificações
- [ ] Testar marcação como lida
- [ ] Testar fluxo completo: criar chamado → notificar admin → responder → notificar operador

---

## 🚀 Exemplo de Implementação (Java Spring Boot)

### NotificacaoService.java

```java
@Service
public class NotificacaoService {
    
    @Autowired
    private Firestore firestore;
    
    public List<Notificacao> getByUserId(String userId) {
        return firestore.collection("notificacoes")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .get()
            .toObjects(Notificacao.class);
    }
    
    public Notificacao create(Notificacao notificacao) {
        // Se userId é "admin", buscar todos os admins e criar uma notificação para cada
        if ("admin".equals(notificacao.getUserId())) {
            List<User> admins = userService.getByRole("admin");
            for (User admin : admins) {
                Notificacao notifAdmin = new Notificacao(notificacao);
                notifAdmin.setUserId(admin.getUid());
                notifAdmin.setLida(false);
                notifAdmin.setCreatedAt(Timestamp.now());
                firestore.collection("notificacoes").add(notifAdmin);
            }
            return notificacao;
        }
        
        notificacao.setLida(false);
        notificacao.setCreatedAt(Timestamp.now());
        DocumentReference ref = firestore.collection("notificacoes").add(notificacao).get();
        notificacao.setId(ref.getId());
        return notificacao;
    }
    
    public void marcarComoLida(String id) {
        firestore.collection("notificacoes")
            .document(id)
            .update("lida", true, "updatedAt", Timestamp.now());
    }
}
```

---

**Última atualização:** 26/11/2025

