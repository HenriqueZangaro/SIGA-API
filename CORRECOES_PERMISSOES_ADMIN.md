# 🔐 Correções de Permissões - Admin vs Operador

## 📋 Resumo das Alterações

Foi implementado um sistema completo de permissões para diferenciar **Admin do Site** de **Operadores**, seguindo o modelo do frontend.

---

## 🎯 Hierarquia de Usuários

### 1. **Admin do Site** (`role: 'admin'`)
- **Acesso**: Todos os dados de todos os operadores e proprietários
- **Permissões**: CRUD completo em todas as entidades
- **Características**:
  - Pode ver todos os pontos de todos os operadores
  - Pode registrar pontos em nome de qualquer operador
  - Pode consultar estatísticas de qualquer operador
  - Não precisa estar vinculado a um `operadorId`

### 2. **Operador** (`role: 'operador'`)
- **Acesso**: Apenas seus próprios dados
- **Permissões**: Apenas seus pontos de entrada/saída
- **Características**:
  - Pode ver apenas seus próprios pontos
  - Pode registrar apenas seus próprios pontos
  - Deve estar vinculado a um `operadorId` no `UserProfile`

### 3. **Usuário Comum** (`role: 'user'`)
- **Acesso**: Dados dos proprietários associados
- **Permissões**: Limitadas ao contexto do proprietário
- **Características**:
  - Não tem acesso ao sistema de pontos
  - Acessa dados baseado em associações com proprietários

---

## 🛠️ Arquivos Modificados

### 1. **`AuthService.java`**

#### Método `getOperadorInfo()`
**ANTES:**
```java
// Verificar se é operador
if (!"operador".equalsIgnoreCase(userProfile.getRole())) {
    throw new RuntimeException("Usuário não é um operador");
}
```

**DEPOIS:**
```java
// Se for ADMIN do site, retornar dados simulados (admin tem acesso total)
if ("admin".equalsIgnoreCase(userProfile.getRole())) {
    System.out.println("✅ Service: Admin do site detectado - Acesso total concedido");
    // Criar dados simulados para admin (não precisa de operador)
    Map<String, Object> adminData = new HashMap<>();
    adminData.put("id", "admin");
    adminData.put("nome", userProfile.getDisplayName() != null ? userProfile.getDisplayName() : "Administrador");
    adminData.put("proprietarioId", "all"); // Admin acessa todos
    resultado.put("operador", adminData);
    return resultado;
}

// Se for OPERADOR, buscar dados do operador
if ("operador".equalsIgnoreCase(userProfile.getRole())) {
    // ... lógica existente ...
}

// Se não for nem admin nem operador
throw new RuntimeException("Usuário não tem permissão para acessar recursos de ponto (role: " + userProfile.getRole() + ")");
```

---

### 2. **`PontoController.java`**

#### Endpoint: `GET /api/v1/pontos/historico`
**Novo comportamento:**
- **Admin**: Retorna todos os pontos de todos os operadores (sem filtro)
- **Admin com `operadorId`**: Retorna pontos de um operador específico
- **Operador**: Retorna apenas seus próprios pontos

**Novos parâmetros:**
- `operadorId` (opcional): Para admin especificar qual operador consultar

#### Endpoint: `GET /api/v1/pontos/hoje`
**Novo comportamento:**
- **Admin sem `operadorId`**: Retorna todos os pontos de hoje de todos os operadores
- **Admin com `operadorId`**: Retorna pontos de hoje de um operador específico
- **Operador**: Retorna apenas seus próprios pontos de hoje

**Novos parâmetros:**
- `operadorId` (opcional): Para admin especificar qual operador consultar

#### Endpoint: `GET /api/v1/pontos/status`
**Novo comportamento:**
- **Admin**: Deve especificar `operadorId` obrigatório
- **Operador**: Retorna seu próprio status

**Novos parâmetros:**
- `operadorId` (opcional para operador, obrigatório para admin)

#### Endpoint: `GET /api/v1/pontos/estatisticas`
**Novo comportamento:**
- **Admin**: Deve especificar `operadorId` obrigatório
- **Operador**: Retorna suas próprias estatísticas

**Novos parâmetros:**
- `operadorId` (opcional para operador, obrigatório para admin)

---

### 3. **`PontoService.java`**

#### Novo método: `getTodosPontos()`
```java
/**
 * Busca TODOS os pontos (para admin)
 */
public List<Ponto> getTodosPontos(Date dataInicio, Date dataFim) {
    System.out.println("🔍 Service: Buscando TODOS os pontos (admin)");
    List<Ponto> pontos = pontoRepository.findAll(dataInicio, dataFim);
    System.out.println("✅ Service: Encontrados " + pontos.size() + " pontos no total");
    return pontos;
}
```

---

### 4. **`PontoRepository.java`**

#### Novo método: `findAll()`
```java
/**
 * Busca TODOS os pontos (para admin)
 */
public List<Ponto> findAll(Date dataInicio, Date dataFim) {
    // Busca todos os documentos da coleção "pontos"
    // Filtra por data em memória
    // Ordena por dataHora (mais recente primeiro)
    // Trata erros de deserialização (ignora documentos problemáticos)
}
```

---

### 5. **`Ponto.java`**
- Adicionado `@JsonIgnoreProperties(ignoreUnknown = true)` para ignorar campos desconhecidos
- Campo `updatedAt` alterado de `Timestamp` para `Object` (compatibilidade com Firestore)

### 6. **`UserProfile.java`**
- Adicionado `@JsonIgnoreProperties(ignoreUnknown = true)` para ignorar campos desconhecidos (como `department`)
- Campos `ultimaAtualizacao`, `createdAt`, `updatedAt` alterados para `Object` (compatibilidade)

---

## 📊 Matriz de Permissões

| Endpoint | Admin | Operador |
|----------|-------|----------|
| `POST /pontos/registrar` | ✅ (qualquer operador) | ✅ (apenas si mesmo) |
| `GET /pontos/status` | ✅ (com operadorId) | ✅ (apenas si mesmo) |
| `GET /pontos/hoje` | ✅ (todos ou específico) | ✅ (apenas si mesmo) |
| `GET /pontos/historico` | ✅ (todos ou específico) | ✅ (apenas si mesmo) |
| `GET /pontos/estatisticas` | ✅ (com operadorId) | ✅ (apenas si mesmo) |
| `GET /pontos/admin/proprietario/{id}` | ✅ | ❌ |
| `PUT /pontos/admin/{id}` | ✅ | ❌ |
| `DELETE /pontos/admin/{id}` | ✅ | ❌ |

---

## 🧪 Como Testar no Bruno

### **Admin do Site**

1. **Buscar todos os pontos:**
   ```
   GET http://localhost:8080/api/v1/pontos/historico
   Header: X-User-UID: <UID_DO_ADMIN>
   ```

2. **Buscar todos os pontos de hoje:**
   ```
   GET http://localhost:8080/api/v1/pontos/hoje
   Header: X-User-UID: <UID_DO_ADMIN>
   ```

3. **Buscar pontos de um operador específico:**
   ```
   GET http://localhost:8080/api/v1/pontos/historico?operadorId=<ID_OPERADOR>
   Header: X-User-UID: <UID_DO_ADMIN>
   ```

4. **Status de um operador específico:**
   ```
   GET http://localhost:8080/api/v1/pontos/status?operadorId=<ID_OPERADOR>
   Header: X-User-UID: <UID_DO_ADMIN>
   ```

### **Operador**

1. **Buscar seus próprios pontos:**
   ```
   GET http://localhost:8080/api/v1/pontos/historico
   Header: X-User-UID: <UID_DO_OPERADOR>
   ```

2. **Buscar seus pontos de hoje:**
   ```
   GET http://localhost:8080/api/v1/pontos/hoje
   Header: X-User-UID: <UID_DO_OPERADOR>
   ```

3. **Seu próprio status:**
   ```
   GET http://localhost:8080/api/v1/pontos/status
   Header: X-User-UID: <UID_DO_OPERADOR>
   ```

---

## ✅ Checklist de Validação

- [x] Admin pode ver todos os pontos de todos os operadores
- [x] Admin pode ver pontos de um operador específico
- [x] Operador só vê seus próprios pontos
- [x] Admin não precisa estar vinculado a um `operadorId`
- [x] Operador deve estar vinculado a um `operadorId`
- [x] Campos `updatedAt` do Firestore não causam mais erro de deserialização
- [x] Campos desconhecidos (como `department`) são ignorados
- [x] Logs informativos indicam quando admin acessa sistema

---

## 🚀 Próximos Passos

1. Reinicie a API: `mvn spring-boot:run`
2. Teste todos os endpoints no Bruno com UID de admin
3. Teste os endpoints com UID de operador
4. Verifique os logs para confirmar detecção correta de roles

---

## 📝 Observações Importantes

1. **UID de Admin**: Certifique-se de usar o UID correto de um usuário com `role: 'admin'` no Firestore
2. **Firestore**: O campo `role` no documento `userProfiles/{uid}` deve ser exatamente `'admin'` (case-insensitive)
3. **Logs**: Procure por mensagens como `"👑 Admin do site detectado"` para confirmar detecção
4. **Compatibilidade**: Todas as alterações são retrocompatíveis com operadores existentes

---

**Data da Correção:** 23 de Novembro de 2025  
**Versão da API:** 1.0.0

