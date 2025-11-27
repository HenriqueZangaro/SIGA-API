# 🔧 Filtro de Talhões e Máquinas via Fazendas - API SIGA

Este documento descreve a implementação do filtro de segurança para talhões e máquinas, que são filtrados através das fazendas do proprietário.

---

## ✅ STATUS: IMPLEMENTADO

Todas as alterações foram aplicadas com sucesso.

---

## 📋 Índice

1. [Problema Identificado](#1-problema-identificado)
2. [Solução Implementada](#2-solução-implementada)
3. [Como Funciona](#3-como-funciona)
4. [Arquivos Modificados](#4-arquivos-modificados)
5. [Fluxo de Dados](#5-fluxo-de-dados)
6. [Exemplos de Uso](#6-exemplos-de-uso)
7. [Limitações e Considerações](#7-limitações-e-considerações)

---

## 1. Problema Identificado

### Estrutura de Dados

Talhões e máquinas **não têm `proprietarioId` direto** no Firestore. Eles são ligados às fazendas:

```
┌─────────────────────────────────────────────────────────────┐
│                    ESTRUTURA DE DADOS                        │
│                                                              │
│  Talhão:                                                     │
│    - fazendaId: "fazenda_123"                               │
│    - proprietarioId: ❌ NÃO EXISTE                          │
│                                                              │
│  Máquina:                                                     │
│    - fazendaIds: ["fazenda_123", "fazenda_456"]             │
│    - proprietarioId: ❌ NÃO EXISTE                           │
│                                                              │
│  Fazenda:                                                     │
│    - proprietarioId: "CCnyN3MpHq5XRtnl8VFV" ✅              │
└─────────────────────────────────────────────────────────────┘
```

### Problema

A implementação anterior tentava buscar talhões e máquinas diretamente por `proprietarioId`, mas esse campo não existe nesses documentos, resultando em listas vazias.

---

## 2. Solução Implementada

### Abordagem

Filtrar talhões e máquinas **através das fazendas** do proprietário:

1. **Buscar fazendas** do proprietário
2. **Extrair IDs** das fazendas
3. **Buscar talhões/máquinas** que pertencem a essas fazendas

---

## 3. Como Funciona

### 3.1. Talhões

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO DE FILTRO - TALHÕES                 │
│                                                              │
│  1. Usuário solicita talhões                                │
│     GET /api/v1/talhoes                                     │
│     Header: X-User-UID: uid_usuario                         │
│                                                              │
│  2. API identifica proprietarioId do usuário                │
│     proprietarioId = "CCnyN3MpHq5XRtnl8VFV"                 │
│                                                              │
│  3. API busca fazendas do proprietário                      │
│     FazendaRepository.findByProprietarioId(proprietarioId)  │
│     → Retorna: [fazenda_1, fazenda_2, fazenda_3]            │
│                                                              │
│  4. API extrai IDs das fazendas                             │
│     fazendaIds = ["fazenda_1", "fazenda_2", "fazenda_3"]    │
│                                                              │
│  5. API busca talhões dessas fazendas                       │
│     TalhaoRepository.findByFazendaIdIn(fazendaIds)          │
│     → Retorna: [talhao_1, talhao_2, talhao_5]               │
│                                                              │
│  6. API retorna talhões filtrados                           │
└─────────────────────────────────────────────────────────────┘
```

### 3.2. Máquinas

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO DE FILTRO - MÁQUINAS                │
│                                                              │
│  1. Usuário solicita máquinas                               │
│     GET /api/v1/maquinas                                    │
│     Header: X-User-UID: uid_usuario                          │
│                                                              │
│  2. API identifica proprietarioId do usuário                 │
│     proprietarioId = "CCnyN3MpHq5XRtnl8VFV"                  │
│                                                              │
│  3. API busca fazendas do proprietário                       │
│     FazendaRepository.findByProprietarioId(proprietarioId)  │
│     → Retorna: [fazenda_1, fazenda_2, fazenda_3]             │
│                                                              │
│  4. API extrai IDs das fazendas                              │
│     fazendaIds = ["fazenda_1", "fazenda_2", "fazenda_3"]     │
│                                                              │
│  5. API busca máquinas que pertencem a essas fazendas       │
│     MaquinaRepository.findByFazendaIdsContainingAny(...)     │
│     → Verifica se fazendaIds[] da máquina contém algum ID    │
│     → Retorna: [maquina_1, maquina_3]                        │
│                                                              │
│  6. API retorna máquinas filtradas                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Arquivos Modificados

### 4.1. TalhaoRepository.java

**Método adicionado:**
```java
public List<Talhao> findByFazendaIdIn(List<String> fazendaIds)
```

**Funcionalidade:**
- Recebe uma lista de IDs de fazendas
- Usa `whereIn("fazendaId", lote)` do Firestore
- Divide em lotes de 10 (limite do Firestore)
- Retorna todos os talhões que pertencem a essas fazendas

**Código:**
```java
public List<Talhao> findByFazendaIdIn(List<String> fazendaIds) {
    // Firestore limita whereIn a 10 valores
    // Dividir em lotes de 10
    for (int i = 0; i < fazendaIds.size(); i += 10) {
        List<String> lote = fazendaIds.subList(i, Math.min(i + 10, fazendaIds.size()));
        
        List<QueryDocumentSnapshot> documents = firestore.collection("talhoes")
                .whereIn("fazendaId", lote)
                .get()
                .get()
                .getDocuments();
        // ... processar documentos
    }
}
```

### 4.2. TalhaoService.java

**Modificações:**
- Adicionada dependência de `FazendaRepository`
- Método `buscarPorProprietarioId()` reimplementado

**Lógica:**
```java
public List<Talhao> buscarPorProprietarioId(String proprietarioId) {
    // 1. Buscar fazendas do proprietário
    List<Fazenda> fazendas = fazendaRepository.findByProprietarioId(proprietarioId);
    
    // 2. Extrair IDs
    List<String> fazendaIds = fazendas.stream()
            .map(Fazenda::getId)
            .collect(Collectors.toList());
    
    // 3. Buscar talhões dessas fazendas
    return talhaoRepository.findByFazendaIdIn(fazendaIds);
}
```

### 4.3. MaquinaRepository.java

**Método adicionado:**
```java
public List<Maquina> findByFazendaIdsContainingAny(List<String> fazendaIds)
```

**Funcionalidade:**
- Recebe uma lista de IDs de fazendas
- Busca todas as máquinas
- Filtra em memória: verifica se `maquina.fazendaIds[]` contém algum ID da lista
- Retorna máquinas que pertencem a pelo menos uma fazenda

**Código:**
```java
public List<Maquina> findByFazendaIdsContainingAny(List<String> fazendaIds) {
    // Buscar todas as máquinas
    List<QueryDocumentSnapshot> allDocuments = firestore.collection("maquinas")
            .get()
            .get()
            .getDocuments();
    
    // Filtrar em memória
    for (QueryDocumentSnapshot document : allDocuments) {
        Maquina maquina = document.toObject(Maquina.class);
        
        if (maquina.getFazendaIds() != null) {
            boolean pertence = maquina.getFazendaIds().stream()
                    .anyMatch(fazendaIds::contains);
            
            if (pertence) {
                maquinasFiltradas.add(maquina);
            }
        }
    }
}
```

**Por que filtrar em memória?**
- Firestore não suporta `array-contains-any` diretamente
- Máquinas podem ter múltiplas fazendas (`fazendaIds[]`)
- Mais simples e eficiente para volumes moderados

### 4.4. MaquinaService.java

**Modificações:**
- Adicionada dependência de `FazendaRepository`
- Método `buscarPorProprietarioId()` reimplementado

**Lógica:**
```java
public List<Maquina> buscarPorProprietarioId(String proprietarioId) {
    // 1. Buscar fazendas do proprietário
    List<Fazenda> fazendas = fazendaRepository.findByProprietarioId(proprietarioId);
    
    // 2. Extrair IDs
    List<String> fazendaIds = fazendas.stream()
            .map(Fazenda::getId)
            .collect(Collectors.toList());
    
    // 3. Buscar máquinas que pertencem a essas fazendas
    return maquinaRepository.findByFazendaIdsContainingAny(fazendaIds);
}
```

---

## 5. Fluxo de Dados

### Diagrama Completo

```
┌─────────────────────────────────────────────────────────────┐
│                    REQUISIÇÃO DO APP                         │
│                                                              │
│  GET /api/v1/talhoes                                        │
│  Header: X-User-UID: uid_usuario                            │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    TalhaoController                         │
│  - Recebe UID                                               │
│  - Verifica role (admin/user/operador)                      │
│  - Chama TalhaoService.buscarPorProprietarioId()            │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    TalhaoService                             │
│  1. Busca fazendas: FazendaRepository.findByProprietarioId()│
│  2. Extrai IDs: fazendaIds = [fazenda_1, fazenda_2, ...]   │
│  3. Busca talhões: TalhaoRepository.findByFazendaIdIn()     │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    TalhaoRepository                          │
│  - Divide fazendaIds em lotes de 10                         │
│  - Para cada lote: whereIn("fazendaId", lote)              │
│  - Retorna todos os talhões encontrados                     │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    RESPOSTA                                 │
│  [                                                           │
│    { id: "talhao_1", fazendaId: "fazenda_1", ... },        │
│    { id: "talhao_2", fazendaId: "fazenda_1", ... },         │
│    { id: "talhao_5", fazendaId: "fazenda_3", ... }          │
│  ]                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Exemplos de Uso

### 6.1. User - Buscar Talhões

```http
GET /api/v1/talhoes
X-User-UID: uid_do_usuario_danilela
```

**Processamento:**
1. API identifica: `proprietarioId = "CCnyN3MpHq5XRtnl8VFV"` (Danilela)
2. Busca fazendas: `[Fazenda Teste]`
3. Busca talhões: `[Talhão 1, Talhão 2]` (da Fazenda Teste)

**Resposta:**
```json
[
  {
    "id": "talhao_1",
    "nome": "Talhão 1",
    "fazendaId": "fazenda_teste",
    "proprietarioId": null
  },
  {
    "id": "talhao_2",
    "nome": "Talhão 2",
    "fazendaId": "fazenda_teste",
    "proprietarioId": null
  }
]
```

### 6.2. User - Buscar Máquinas

```http
GET /api/v1/maquinas
X-User-UID: uid_do_usuario_danilela
```

**Processamento:**
1. API identifica: `proprietarioId = "CCnyN3MpHq5XRtnl8VFV"` (Danilela)
2. Busca fazendas: `[Fazenda Teste]`
3. Busca máquinas onde `fazendaIds[]` contém `"fazenda_teste"`

**Resposta:**
```json
[
  {
    "id": "maquina_1",
    "nome": "Trator John Deere",
    "fazendaIds": ["fazenda_teste", "fazenda_outra"],
    "proprietarioId": null
  }
]
```

### 6.3. Admin - Ver Todos

```http
GET /api/v1/talhoes
X-User-UID: uid_do_admin
```

**Resposta:** Todos os talhões (sem filtro)

### 6.4. Admin - Filtrar por Proprietário

```http
GET /api/v1/talhoes?proprietarioId=CCnyN3MpHq5XRtnl8VFV
X-User-UID: uid_do_admin
```

**Resposta:** Apenas talhões de Danilela

---

## 7. Limitações e Considerações

### 7.1. Firestore Limitações

| Limitação | Impacto | Solução |
|-----------|---------|---------|
| `whereIn()` aceita até 10 valores | Se proprietário tiver > 10 fazendas | Dividir em lotes de 10 |
| `array-contains-any` não disponível | Máquinas com múltiplas fazendas | Filtrar em memória |

### 7.2. Performance

**Talhões:**
- ✅ Eficiente: usa `whereIn()` do Firestore
- ⚠️ Se tiver > 10 fazendas, faz múltiplas queries

**Máquinas:**
- ⚠️ Menos eficiente: busca todas e filtra em memória
- ✅ Aceitável para volumes moderados (< 1000 máquinas)
- 💡 **Otimização futura:** Adicionar `proprietarioId` direto nas máquinas

### 7.3. Otimizações Futuras

**Opção 1: Adicionar `proprietarioId` nas máquinas**
```java
// Ao criar/atualizar máquina, copiar proprietarioId da fazenda
maquina.setProprietarioId(fazenda.getProprietarioId());
```

**Opção 2: Adicionar `proprietarioId` nos talhões**
```java
// Ao criar/atualizar talhão, copiar proprietarioId da fazenda
talhao.setProprietarioId(fazenda.getProprietarioId());
```

**Vantagens:**
- Queries mais rápidas
- Menos processamento
- Mais simples de manter

**Desvantagens:**
- Dados duplicados
- Precisa manter sincronizado

---

## 📊 Resumo

| Item | Status |
|------|--------|
| Talhões filtrados via fazendas | ✅ Implementado |
| Máquinas filtradas via fazendas | ✅ Implementado |
| Suporte a múltiplas fazendas (máquinas) | ✅ Implementado |
| Divisão em lotes (Firestore limit) | ✅ Implementado |
| Filtro em memória (máquinas) | ✅ Implementado |

---

## 🚀 Próximos Passos

1. **Testar** com diferentes cenários:
   - Usuário com 1 fazenda
   - Usuário com > 10 fazendas
   - Máquinas com múltiplas fazendas

2. **Monitorar performance**:
   - Tempo de resposta
   - Uso de memória
   - Custo do Firestore

3. **Considerar otimização**:
   - Adicionar `proprietarioId` direto (se necessário)

---

*Documento gerado em 27/11/2025 para o projeto SIGA*

