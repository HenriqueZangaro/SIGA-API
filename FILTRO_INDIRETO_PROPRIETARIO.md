# 🔗 Filtro Indireto por Proprietário - API SIGA

Este documento explica como a API filtra recursos que **não têm `proprietarioId` diretamente**, mas estão associados a recursos que **têm `proprietarioId`**.

---

## 📋 Índice

1. [Conceito de Filtro Indireto](#1-conceito-de-filtro-indireto)
2. [Hierarquia de Relacionamentos](#2-hierarquia-de-relacionamentos)
3. [Recursos que Usam Filtro Indireto](#3-recursos-que-usam-filtro-indireto)
4. [Implementação Técnica](#4-implementação-técnica)
5. [Como Funciona no App](#5-como-funciona-no-app)
6. [Exemplos Práticos](#6-exemplos-práticos)

---

## 1. Conceito de Filtro Indireto

### 1.1. Problema

Alguns recursos no Firestore **não têm `proprietarioId` diretamente**, mas estão associados a recursos que têm. Por exemplo:

- ❌ **Talhão** não tem `proprietarioId` → mas está associado a uma **Fazenda** que tem
- ❌ **Máquina** não tem `proprietarioId` → mas está associada a **Fazendas** (array) que têm
- ❌ **Trabalho** não tem `proprietarioId` → mas está associado a uma **Fazenda** que tem

### 1.2. Solução

**Filtro Indireto**: Buscar o recurso através do recurso pai que tem `proprietarioId`.

```
Usuário (proprietarioId) 
  ↓
Fazenda (proprietarioId) 
  ↓
Talhão (fazendaId) ✅
Máquina (fazendaIds[]) ✅
Trabalho (fazendaId) ✅
```

---

## 2. Hierarquia de Relacionamentos

### 2.1. Estrutura Completa

```
┌─────────────────────────────────────────────────────────────┐
│                    HIERARQUIA DE DADOS                       │
│                                                              │
│  Proprietário (proprietarioId)                               │
│    │                                                          │
│    ├── Fazenda (proprietarioId) ✅ DIRETO                    │
│    │     │                                                      │
│    │     ├── Talhão (fazendaId) ⚠️ INDIRETO                   │
│    │     │                                                      │
│    │     ├── Trabalho (fazendaId) ⚠️ INDIRETO                  │
│    │     │                                                      │
│    │     └── Máquina (fazendaIds[]) ⚠️ INDIRETO                │
│    │                                                          │
│    ├── Operador (proprietarioId) ✅ DIRETO                    │
│    │                                                          │
│    ├── Safra (proprietarioId) ✅ DIRETO                      │
│    │                                                          │
│    └── Chamado (proprietarioId) ✅ DIRETO                    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 2.2. Tipos de Relacionamento

| Tipo | Descrição | Exemplo |
|------|-----------|---------|
| **Direto** | Recurso tem `proprietarioId` | Fazenda, Operador, Safra, Chamado |
| **Indireto (1:1)** | Recurso está associado a 1 recurso pai | Talhão → Fazenda |
| **Indireto (1:N)** | Recurso está associado a múltiplos recursos pais | Máquina → Fazendas (array) |
| **Indireto (N:1)** | Múltiplos recursos associados a 1 pai | Trabalho → Fazenda |

---

## 3. Recursos que Usam Filtro Indireto

### 3.1. Talhões

**Estrutura:**
```json
{
  "id": "talhao_123",
  "fazendaId": "fazenda_456",  // ⚠️ Não tem proprietarioId
  "nome": "Talhão Norte"
}
```

**Filtro:**
1. Buscar fazendas do proprietário
2. Extrair `fazendaIds`
3. Buscar talhões onde `fazendaId IN fazendaIds`

**Limitação Firestore:**
- `whereIn()` aceita máximo 10 itens
- Solução: Processar em lotes de 10

### 3.2. Máquinas

**Estrutura:**
```json
{
  "id": "maquina_123",
  "fazendaIds": ["fazenda_1", "fazenda_2"],  // ⚠️ Array de fazendas
  "nome": "Trator John Deere"
}
```

**Filtro:**
1. Buscar fazendas do proprietário
2. Extrair `fazendaIds`
3. Buscar todas as máquinas
4. Filtrar em memória: `maquina.fazendaIds` contém algum `fazendaId` do proprietário

**Limitação Firestore:**
- Não suporta `array-contains-any` com múltiplos valores facilmente
- Solução: Buscar todas e filtrar em memória

### 3.3. Trabalhos

**Estrutura:**
```json
{
  "id": "trabalho_123",
  "fazendaId": "fazenda_456",  // ⚠️ Não tem proprietarioId
  "talhaoId": "talhao_789",
  "operadorId": "operador_101",
  "dataInicio": "2025-11-27"
}
```

**Filtro:**
1. Buscar fazendas do proprietário
2. Extrair `fazendaIds`
3. Buscar trabalhos onde `fazendaId IN fazendaIds`

**Limitação Firestore:**
- `whereIn()` aceita máximo 10 itens
- Solução: Processar em lotes de 10

---

## 4. Implementação Técnica

### 4.1. Padrão de Implementação

Todos os recursos com filtro indireto seguem o mesmo padrão:

```java
@Service
public class RecursoService {
    @Autowired
    private RecursoRepository recursoRepository;
    
    @Autowired
    private FazendaRepository fazendaRepository; // Dependência para buscar fazendas
    
    public List<Recurso> buscarPorProprietarioId(String proprietarioId) {
        // 1. Buscar fazendas do proprietário
        List<Fazenda> fazendas = fazendaRepository.findByProprietarioId(proprietarioId);
        
        if (fazendas.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. Extrair IDs das fazendas
        List<String> fazendaIds = fazendas.stream()
                .map(Fazenda::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 3. Buscar recursos através das fazendas
        return recursoRepository.findByFazendaIdIn(fazendaIds);
    }
}
```

### 4.2. Repository - Talhões e Trabalhos

**Método no Repository:**
```java
public List<Recurso> findByFazendaIdIn(List<String> fazendaIds) {
    if (fazendaIds.isEmpty()) {
        return Collections.emptyList();
    }
    
    List<Recurso> todosRecursos = new ArrayList<>();
    
    // Processar em lotes de 10 (limite do Firestore whereIn)
    for (int i = 0; i < fazendaIds.size(); i += 10) {
        List<String> lote = fazendaIds.subList(i, Math.min(i + 10, fazendaIds.size()));
        
        QuerySnapshot snapshot = firestore.collection("recursos")
            .whereIn("fazendaId", lote)
            .get().get();
        
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Recurso recurso = doc.toObject(Recurso.class);
            recurso.setId(doc.getId());
            todosRecursos.add(recurso);
        }
    }
    
    return todosRecursos;
}
```

### 4.3. Repository - Máquinas

**Método no Repository:**
```java
public List<Maquina> findByFazendaIdsContainingAny(List<String> fazendaIds) {
    if (fazendaIds.isEmpty()) {
        return Collections.emptyList();
    }
    
    List<Maquina> todasMaquinas = new ArrayList<>();
    
    // Buscar todas as máquinas (não há query eficiente para array-contains-any)
    QuerySnapshot snapshot = firestore.collection("maquinas").get().get();
    
    for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
        Maquina maquina = doc.toObject(Maquina.class);
        maquina.setId(doc.getId());
        
        // Filtrar em memória: máquina pertence a alguma fazenda do proprietário?
        if (maquina.getFazendaIds() != null) {
            boolean pertence = maquina.getFazendaIds().stream()
                .anyMatch(fazendaIds::contains);
            
            if (pertence) {
                todasMaquinas.add(maquina);
            }
        }
    }
    
    return todasMaquinas;
}
```

---

## 5. Como Funciona no App

### 5.1. Fluxo de Dados

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO NO APP                              │
│                                                              │
│  1. App solicita GET /api/v1/talhoes                        │
│     Header: X-User-UID: uid_usuario                         │
│                                                              │
│  2. API identifica proprietarioId do usuário                │
│     → proprietarioId = "MqfPVwIC7ayojtQ1HfoM"               │
│                                                              │
│  3. API busca fazendas do proprietário                      │
│     → FazendaRepository.findByProprietarioId()              │
│     → 3 fazendas encontradas                                │
│                                                              │
│  4. API extrai IDs das fazendas                            │
│     → fazendaIds = ["fazenda_1", "fazenda_2", "fazenda_3"] │
│                                                              │
│  5. API busca talhões/trabalhos/máquinas                   │
│     → RecursoRepository.findByFazendaIdIn(fazendaIds)       │
│     → Processa em lotes de 10 (se necessário)               │
│                                                              │
│  6. API retorna recursos filtrados                         │
│     → Apenas recursos das fazendas do proprietário          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 5.2. Endpoints Afetados

| Endpoint | Recurso | Tipo de Filtro |
|----------|---------|----------------|
| `GET /api/v1/talhoes` | Talhões | Indireto (via Fazendas) |
| `GET /api/v1/maquinas` | Máquinas | Indireto (via Fazendas) |
| `GET /api/v1/trabalhos` | Trabalhos | Indireto (via Fazendas) |
| `GET /api/v1/fazendas` | Fazendas | Direto (tem proprietarioId) |
| `GET /api/v1/operadores` | Operadores | Direto (tem proprietarioId) |
| `GET /api/v1/safras` | Safras | Direto (tem proprietarioId) |
| `GET /api/v1/chamados` | Chamados | Direto (tem proprietarioId) |

### 5.3. Comportamento por Role

| Role | Comportamento |
|------|---------------|
| **Admin** | Vê todos os recursos (sem filtro) |
| **User** | Vê apenas recursos do seu proprietário (filtro direto ou indireto) |
| **Operador** | Vê apenas recursos do proprietário do seu operador (filtro direto ou indireto) |

---

## 6. Exemplos Práticos

### 6.1. Exemplo: Buscar Talhões

**Request:**
```http
GET /api/v1/talhoes
X-User-UID: uid_user_danilela
```

**Processamento:**
```
1. API identifica: proprietarioId = "CCnyN3MpHq5XRtnl8VFV"
2. API busca fazendas: 3 fazendas encontradas
3. API extrai IDs: ["fazenda_1", "fazenda_2", "fazenda_3"]
4. API busca talhões: WHERE fazendaId IN ["fazenda_1", "fazenda_2", "fazenda_3"]
5. API retorna: 27 talhões encontrados
```

**Logs:**
```
🔍 Service: Buscando talhões do proprietário: CCnyN3MpHq5XRtnl8VFV
✅ Fazendas encontradas: 3
   IDs: [fazenda_1, fazenda_2, fazenda_3]
🔍 Repository: Buscando talhões de 3 fazendas
   📦 Processando lote 1 com 3 fazendas
✅ Encontrados 27 talhões para as fazendas
✅ Controller: Retornando 27 talhões
```

### 6.2. Exemplo: Buscar Trabalhos

**Request:**
```http
GET /api/v1/trabalhos
X-User-UID: uid_user_danilela
```

**Processamento:**
```
1. API identifica: proprietarioId = "MqfPVwIC7ayojtQ1HfoM"
2. API busca fazendas: 3 fazendas encontradas
3. API extrai IDs: [6nve2uRo9vek63MgLLjm, NpYUwOAtAN9uZ0QVoc6i, QFN9h8QLnPN02siWTRza]
4. API busca trabalhos: WHERE fazendaId IN [fazenda_ids]
5. API retorna: X trabalhos encontrados
```

**Logs:**
```
🔍 Service: Buscando trabalhos do proprietário: MqfPVwIC7ayojtQ1HfoM
   📋 Fazendas encontradas: 3
   📋 IDs: [6nve2uRo9vek63MgLLjm, NpYUwOAtAN9uZ0QVoc6i, QFN9h8QLnPN02siWTRza]
🔍 Repository: Buscando trabalhos de 3 fazendas
   📦 Processando lote 1 com 3 fazendas
✅ Encontrados X trabalhos para 3 fazendas
✅ Service: Encontrados X trabalhos para o proprietário
```

### 6.3. Exemplo: Buscar Máquinas

**Request:**
```http
GET /api/v1/maquinas
X-User-UID: uid_user_danilela
```

**Processamento:**
```
1. API identifica: proprietarioId = "CCnyN3MpHq5XRtnl8VFV"
2. API busca fazendas: 3 fazendas encontradas
3. API extrai IDs: ["fazenda_1", "fazenda_2", "fazenda_3"]
4. API busca todas as máquinas
5. API filtra em memória: máquina.fazendaIds contém alguma fazenda do proprietário
6. API retorna: X máquinas encontradas
```

**Logs:**
```
🔍 Service: Buscando máquinas do proprietário: CCnyN3MpHq5XRtnl8VFV
✅ Fazendas encontradas: 3
   IDs: [fazenda_1, fazenda_2, fazenda_3]
🔍 Repository: Buscando máquinas por fazendas
✅ Encontradas X máquinas para as fazendas
✅ Controller: Retornando X máquinas
```

---

## 7. Limitações e Considerações

### 7.1. Limitações do Firestore

| Limitação | Impacto | Solução |
|-----------|---------|---------|
| `whereIn()` máximo 10 itens | Não pode buscar muitos recursos de uma vez | Processar em lotes de 10 |
| `array-contains-any` limitado | Não funciona bem com múltiplos valores | Buscar todas e filtrar em memória |
| Performance com muitos dados | Pode ser lento com muitas fazendas | Otimizar queries e usar índices |

### 7.2. Otimizações Implementadas

1. **Processamento em Lotes**: Talhões e Trabalhos processam em lotes de 10
2. **Filtro em Memória**: Máquinas filtram em memória após buscar todas
3. **Logs Detalhados**: Facilita debug e monitoramento
4. **Validação de Vazios**: Retorna lista vazia se não houver fazendas

### 7.3. Recomendações para o App

1. **Cache Local**: Cachear fazendas do usuário para reduzir chamadas
2. **Paginação**: Implementar paginação para grandes volumes de dados
3. **Loading States**: Mostrar loading durante busca (pode demorar com muitas fazendas)
4. **Tratamento de Erros**: Tratar casos onde não há fazendas associadas

---

## 8. Resumo

### 8.1. Regra Geral

> **"Se um recurso não tem `proprietarioId` diretamente, ele sempre está associado a algo que tem `proprietarioId`"**

### 8.2. Recursos com Filtro Direto

- ✅ Fazendas
- ✅ Operadores
- ✅ Safras
- ✅ Chamados

### 8.3. Recursos com Filtro Indireto

- ⚠️ Talhões → via Fazendas
- ⚠️ Máquinas → via Fazendas (array)
- ⚠️ Trabalhos → via Fazendas

### 8.4. Padrão de Implementação

1. Buscar fazendas do proprietário
2. Extrair IDs das fazendas
3. Buscar recursos através das fazendas
4. Retornar recursos filtrados

---

## 9. Checklist para Implementação no App

- [ ] Entender que Talhões, Máquinas e Trabalhos são filtrados via Fazendas
- [ ] Implementar loading states adequados (pode demorar com muitas fazendas)
- [ ] Tratar casos onde usuário não tem fazendas associadas
- [ ] Cachear fazendas do usuário para otimizar
- [ ] Implementar paginação se necessário
- [ ] Testar com usuários que têm muitas fazendas (>10)
- [ ] Testar com usuários que não têm fazendas
- [ ] Verificar logs da API para debug

---

*Documento criado em 27/11/2025 - Filtro Indireto por Proprietário*

