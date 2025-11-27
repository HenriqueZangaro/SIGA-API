# 🔒 Segurança: Filtro por Proprietário - API SIGA

Este documento descreve a implementação de segurança que filtra dados por `proprietarioId`, garantindo que usuários só vejam dados do seu proprietário.

---

## ✅ STATUS: IMPLEMENTADO

Todas as alterações foram aplicadas com sucesso.

---

## 📋 Índice

1. [Resumo da Implementação](#1-resumo-da-implementação)
2. [Regras de Acesso por Role](#2-regras-de-acesso-por-role)
3. [Endpoints Protegidos](#3-endpoints-protegidos)
4. [Arquivos Modificados](#4-arquivos-modificados)
5. [Como Funciona](#5-como-funciona)
6. [Exemplos de Uso](#6-exemplos-de-uso)
7. [Testes](#7-testes)

---

## 1. Resumo da Implementação

### Antes (INSEGURO)
```
App → GET /api/v1/fazendas → API retorna TODAS as fazendas → Frontend filtra
                                      ⚠️ DADOS EXPOSTOS
```

### Depois (SEGURO)
```
App → GET /api/v1/fazendas → API filtra por proprietarioId → Retorna apenas dados permitidos
         (com X-User-UID)                ✅ DADOS PROTEGIDOS
```

---

## 2. Regras de Acesso por Role

| Role | Comportamento | Parâmetro Opcional |
|------|---------------|-------------------|
| `admin` | Vê **TODOS** os dados | Pode filtrar com `?proprietarioId=xxx` |
| `user` | Vê apenas dados do **SEU proprietário** | - |
| `operador` | Vê apenas dados do **proprietário do seu operador** | - |

### Fluxo de Decisão

```
┌─────────────────────────────────────────────────────────────┐
│                    REQUISIÇÃO RECEBIDA                       │
│                    Header: X-User-UID                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌─────────────────────────────┐
              │  Buscar UserProfile no      │
              │  Firebase (collection: users)│
              └─────────────────────────────┘
                              │
                              ▼
              ┌─────────────────────────────┐
              │  Verificar role do usuário  │
              └─────────────────────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
         ▼                    ▼                    ▼
    ┌─────────┐         ┌──────────┐         ┌──────────┐
    │  admin  │         │   user   │         │ operador │
    └─────────┘         └──────────┘         └──────────┘
         │                    │                    │
         ▼                    ▼                    ▼
    Acesso total         Filtra por           Busca operador
    (ou filtra se        proprietarioId       e filtra por
    ?proprietarioId)     do perfil            proprietarioId
```

---

## 3. Endpoints Protegidos

### 3.1. Fazendas

| Método | Endpoint | Admin | User/Operador |
|--------|----------|-------|---------------|
| `GET` | `/api/v1/fazendas` | Todas (ou filtradas) | Só do proprietário |
| `GET` | `/api/v1/fazendas/{id}` | Qualquer | Só se for do proprietário |

### 3.2. Talhões

| Método | Endpoint | Admin | User/Operador |
|--------|----------|-------|---------------|
| `GET` | `/api/v1/talhoes` | Todos (ou filtrados) | Só do proprietário |
| `GET` | `/api/v1/talhoes/{id}` | Qualquer | Só se for do proprietário |
| `GET` | `/api/v1/talhoes/fazenda/{fazendaId}` | Todos | Filtrados |

### 3.3. Máquinas

| Método | Endpoint | Admin | User/Operador |
|--------|----------|-------|---------------|
| `GET` | `/api/v1/maquinas` | Todas (ou filtradas) | Só do proprietário |
| `GET` | `/api/v1/maquinas/{id}` | Qualquer | Só se for do proprietário |
| `GET` | `/api/v1/maquinas/fazenda/{fazendaId}` | Todas | Filtradas |

### 3.4. Operadores

| Método | Endpoint | Admin | User/Operador |
|--------|----------|-------|---------------|
| `GET` | `/api/v1/operadores` | Todos (ou filtrados) | Só do proprietário |
| `GET` | `/api/v1/operadores/{id}` | Qualquer | Só se for do proprietário |
| `GET` | `/api/v1/operadores/fazenda/{fazendaId}` | Todos | Filtrados |

### 3.5. Trabalhos

| Método | Endpoint | Admin | User/Operador |
|--------|----------|-------|---------------|
| `GET` | `/api/v1/trabalhos` | Todos (ou filtrados) | Só do proprietário |
| `GET` | `/api/v1/trabalhos/{id}` | Qualquer | Só se for do proprietário |
| `GET` | `/api/v1/trabalhos/fazenda/{fazendaId}` | Todos | Filtrados |
| `GET` | `/api/v1/trabalhos/talhao/{talhaoId}` | Todos | Filtrados |
| `GET` | `/api/v1/trabalhos/maquina/{maquinaId}` | Todos | Filtrados |
| `GET` | `/api/v1/trabalhos/operador/{operadorId}` | Todos | Filtrados |
| `GET` | `/api/v1/trabalhos/safra/{safraId}` | Todos | Filtrados |

### 3.6. Safras

| Método | Endpoint | Admin | User/Operador |
|--------|----------|-------|---------------|
| `GET` | `/api/v1/safras` | Todas (ou filtradas) | Só do proprietário |
| `GET` | `/api/v1/safras/{id}` | Qualquer | Só se for do proprietário |
| `GET` | `/api/v1/safras/fazenda/{fazendaId}` | Todas | Filtradas |

---

## 4. Arquivos Modificados

### 4.1. Service (AuthService.java)

Adicionados métodos:
- `getProprietarioId(String uid)` - Retorna o proprietarioId do usuário
- `isUser(String uid)` - Verifica se é user
- `getUserProfile(String uid)` - Obtém o perfil do usuário

```java
// AuthService.java - Método principal

public String getProprietarioId(String uid) {
    UserProfile userProfile = userProfileRepository.findByUid(uid);
    
    if (userProfile == null) {
        return null;
    }
    
    // Admin tem acesso a tudo
    if ("admin".equalsIgnoreCase(userProfile.getRole())) {
        return null; // null significa acesso total
    }
    
    // User: retorna o proprietarioId do perfil
    if ("user".equalsIgnoreCase(userProfile.getRole())) {
        return userProfile.getProprietarioId();
    }
    
    // Operador: busca o proprietarioId do operador
    if ("operador".equalsIgnoreCase(userProfile.getRole())) {
        if (userProfile.getOperadorId() != null) {
            Operador operador = operadorRepository.findById(userProfile.getOperadorId());
            if (operador != null) {
                return operador.getProprietarioId();
            }
        }
    }
    
    return null;
}
```

### 4.2. Repositories

Adicionado método `findByProprietarioId(String proprietarioId)` em:
- `FazendaRepository.java`
- `TalhaoRepository.java`
- `MaquinaRepository.java`
- `OperadorRepository.java`
- `TrabalhoRepository.java`
- `SafraRepository.java`

```java
// Exemplo - FazendaRepository.java

public List<Fazenda> findByProprietarioId(String proprietarioId) {
    List<QueryDocumentSnapshot> documents = firestore.collection("fazendas")
            .whereEqualTo("proprietarioId", proprietarioId)
            .get()
            .get()
            .getDocuments();
    // ... conversão para objetos
}
```

### 4.3. Services

Adicionado método `buscarPorProprietarioId(String proprietarioId)` em:
- `FazendaService.java`
- `TalhaoService.java`
- `MaquinaService.java`
- `OperadorService.java`
- `TrabalhoService.java`
- `SafraService.java`

### 4.4. Controllers

Todos os controllers foram atualizados para:
1. Receber header `X-User-UID` obrigatório
2. Verificar role do usuário
3. Filtrar dados por proprietarioId (exceto admin)

```java
// Exemplo - FazendaController.java

@GetMapping
public ResponseEntity<?> listarFazendas(
        @RequestHeader("X-User-UID") String uid,
        @RequestParam(required = false) String proprietarioId) {
    
    List<Fazenda> fazendas;

    if (authService.isAdmin(uid)) {
        // Admin: vê tudo ou filtra opcionalmente
        if (proprietarioId != null && !proprietarioId.isEmpty()) {
            fazendas = fazendaService.buscarPorProprietarioId(proprietarioId);
        } else {
            fazendas = fazendaService.buscarTodas();
        }
    } else {
        // User/Operador: filtra por proprietarioId do usuário
        String userProprietarioId = authService.getProprietarioId(uid);
        
        if (userProprietarioId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        fazendas = fazendaService.buscarPorProprietarioId(userProprietarioId);
    }

    return ResponseEntity.ok(fazendas);
}
```

### 4.5. Models

Adicionado campo `proprietarioId` em:
- `Trabalho.java`

---

## 5. Como Funciona

### 5.1. Requisição do App

O app envia o UID do usuário no header:

```javascript
// Exemplo no React Native
const response = await fetch(`${API_URL}/fazendas`, {
    headers: {
        'X-User-UID': user.uid,
        'Content-Type': 'application/json'
    }
});
```

### 5.2. Processamento na API

1. **Recebe UID** do header `X-User-UID`
2. **Busca UserProfile** no Firebase (collection `users`)
3. **Verifica role** do usuário
4. **Determina proprietarioId**:
   - Admin: null (acesso total)
   - User: `userProfile.proprietarioId`
   - Operador: `operador.proprietarioId`
5. **Filtra dados** no Firestore com `whereEqualTo("proprietarioId", ...)`
6. **Retorna** apenas dados permitidos

### 5.3. Resposta

```json
// Usuário "user" de "Danilela"
// Antes: 10 fazendas (todas)
// Depois: 1 fazenda (só de Danilela)

[
  {
    "id": "abc123",
    "nome": "Fazenda Teste",
    "proprietarioId": "CCnyN3MpHq5XRtnl8VFV"
  }
]
```

---

## 6. Exemplos de Uso

### 6.1. Admin - Ver Todas as Fazendas

```http
GET /api/v1/fazendas
X-User-UID: uid_do_admin
```

**Resposta:** Todas as fazendas

### 6.2. Admin - Filtrar por Proprietário

```http
GET /api/v1/fazendas?proprietarioId=CCnyN3MpHq5XRtnl8VFV
X-User-UID: uid_do_admin
```

**Resposta:** Apenas fazendas de Danilela

### 6.3. User - Ver Suas Fazendas

```http
GET /api/v1/fazendas
X-User-UID: uid_do_usuario_danilela
```

**Resposta:** Apenas fazendas de Danilela (filtrado automaticamente)

### 6.4. Operador - Ver Fazendas do Proprietário

```http
GET /api/v1/fazendas
X-User-UID: uid_do_operador
```

**Resposta:** Fazendas do proprietário vinculado ao operador

---

## 7. Testes

### 7.1. Teste Manual com cURL

```bash
# Como Admin - ver todas
curl -H "X-User-UID: UID_DO_ADMIN" http://localhost:8080/api/v1/fazendas

# Como User - ver só do proprietário
curl -H "X-User-UID: UID_DO_USUARIO" http://localhost:8080/api/v1/fazendas

# Como Operador
curl -H "X-User-UID: UID_DO_OPERADOR" http://localhost:8080/api/v1/fazendas
```

### 7.2. Verificação no DevTools

1. Faça login como usuário não-admin
2. Abra DevTools (F12) > Network
3. Observe a resposta de `/api/v1/fazendas`
4. **Deve conter APENAS fazendas do seu proprietário**

### 7.3. Testes Recomendados

| Cenário | Esperado |
|---------|----------|
| Admin sem filtro | Retorna todos |
| Admin com `?proprietarioId` | Retorna filtrados |
| User de "Danilela" | Retorna só de Danilela |
| User de "Dimas" | Retorna só de Dimas |
| Operador de "Danilela" | Retorna só de Danilela |
| User sem proprietário | Retorna lista vazia |
| Acesso a recurso de outro proprietário | 403 Forbidden |

---

## 📊 Resumo de Segurança

| Antes | Depois |
|-------|--------|
| API retorna TODOS os dados | API filtra por proprietarioId |
| Frontend filtra (inseguro) | Backend filtra (seguro) |
| Dados expostos no tráfego | Dados protegidos |
| Depende do frontend | Depende do backend |

---

## 🚀 Próximos Passos

O app **não precisa de alterações** - já envia o header `X-User-UID` corretamente.

Se quiser remover filtros duplicados no frontend (opcional, para performance), pode fazer isso sabendo que a API já protege os dados.

---

*Documento gerado em 27/11/2025 para o projeto SIGA*

