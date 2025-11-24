# 🔥 CONFIGURAÇÃO INICIAL DO FIRESTORE

Guia completo para configurar os dados iniciais no Firestore antes de usar o sistema de pontos.

---

## 📋 ORDEM DE CRIAÇÃO

Siga esta ordem para evitar erros:

1. **Proprietário** → Criar empresa/proprietário
2. **Fazenda** → Criar fazenda vinculada ao proprietário
3. **Operador** → Criar operador vinculado à fazenda
4. **Firebase Auth** → Criar usuário no Firebase Authentication
5. **UserProfile** → Criar perfil vinculando Auth ↔ Operador
6. **Atualizar Operador** → Adicionar userId no operador

---

## 1️⃣ CRIAR PROPRIETÁRIO

**Collection:** `proprietarios`  
**Document ID:** Gerado automaticamente (ex: `prop_001`)

```json
{
  "id": "prop_001",
  "nome": "Empresa Agrícola LTDA",
  "tipo": "PJ",
  "documento": "12.345.678/0001-99",
  "email": "contato@empresaagricola.com",
  "telefone": "(11) 3456-7890",
  "endereco": "Rua das Fazendas, 100, São Paulo, SP",
  "dataCriacao": "2024-01-01T10:00:00Z",
  "ultimaAtualizacao": "2024-01-01T10:00:00Z"
}
```

**Clicar em "Add Document"**

---

## 2️⃣ CRIAR FAZENDA

**Collection:** `fazendas`  
**Document ID:** Gerado automaticamente (ex: `faz_001`)

```json
{
  "id": "faz_001",
  "nome": "Fazenda São José",
  "localizacao": "Zona Rural, São Paulo, SP",
  "area": 500,
  "proprietario": "Empresa Agrícola LTDA",
  "proprietarioId": "prop_001",
  "qtdTalhoes": 10,
  "dataCriacao": "2024-01-15T10:00:00Z",
  "ultimaAtualizacao": "2024-01-15T10:00:00Z"
}
```

**Clicar em "Add Document"**

---

## 3️⃣ CRIAR OPERADOR

**Collection:** `operadores`  
**Document ID:** Gerado automaticamente (ex: `oper_123`)

```json
{
  "id": "oper_123",
  "nome": "João Silva",
  "cpf": "123.456.789-00",
  "telefone": "(11) 98765-4321",
  "email": "joao@fazenda.com",
  "endereco": "Rua A, 100, São Paulo, SP",
  "fazendaIds": ["faz_001"],
  "fazendaNomes": ["Fazenda São José"],
  "proprietarioId": "prop_001",
  "proprietarioNome": "Empresa Agrícola LTDA",
  "status": "ativo",
  "especialidades": ["Plantio", "Colheita", "Pulverização"],
  "dataCadastro": "2024-02-01T10:00:00Z",
  "ultimaAtualizacao": "2024-02-01T10:00:00Z"
}
```

**⚠️ IMPORTANTE:** Deixe o campo `userId` vazio por enquanto. Vamos preenchê-lo depois.

**Clicar em "Add Document"**

---

## 4️⃣ CRIAR USUÁRIO NO FIREBASE AUTH

### Via Firebase Console:

1. Ir em **Authentication** → **Users**
2. Clicar em **Add user**
3. Preencher:
   - **Email:** `joao@fazenda.com`
   - **Password:** `senha123` (ou qualquer senha)
4. Clicar em **Add user**
5. **COPIAR O UID** do usuário criado (ex: `abc123def456ghi789`)

### Via Firebase Admin SDK (opcional):

```javascript
const admin = require('firebase-admin');

const user = await admin.auth().createUser({
  email: 'joao@fazenda.com',
  password: 'senha123',
  displayName: 'João Silva'
});

console.log('UID:', user.uid);
```

---

## 5️⃣ CRIAR USERPROFILE

**Collection:** `userProfiles`  
**Document ID:** UID do Firebase Auth (ex: `abc123def456ghi789`)

```json
{
  "uid": "abc123def456ghi789",
  "displayName": "João Silva",
  "email": "joao@fazenda.com",
  "photoURL": null,
  "role": "operador",
  "phone": "(11) 98765-4321",
  "bio": "Operador de máquinas agrícolas especializado em plantio e colheita",
  "operadorId": "oper_123",
  "proprietarioId": "prop_001",
  "mustChangePassword": false,
  "createdAt": "2024-02-01T10:30:00Z",
  "updatedAt": "2024-02-01T10:30:00Z"
}
```

**⚠️ ATENÇÃO:**
- O **Document ID** deve ser exatamente o **UID do Firebase Auth**
- O campo `operadorId` deve ser o ID do operador criado no passo 3
- O campo `role` deve ser `"operador"` (não `"admin"` nem `"user"`)

**Clicar em "Add Document" e preencher o ID manualmente**

---

## 6️⃣ ATUALIZAR OPERADOR COM USERID

Voltar na collection `operadores` e editar o documento `oper_123`:

**Adicionar o campo:**
```json
{
  "userId": "abc123def456ghi789"
}
```

Agora o operador está vinculado bidireccionalmente:
- **Operador** → `userId` aponta para UserProfile
- **UserProfile** → `operadorId` aponta para Operador

---

## ✅ VERIFICAÇÃO

### 1. **Verificar Estrutura**

```
📦 Firestore
├── 📁 proprietarios
│   └── 📄 prop_001
│       └── nome: "Empresa Agrícola LTDA"
│
├── 📁 fazendas
│   └── 📄 faz_001
│       ├── nome: "Fazenda São José"
│       └── proprietarioId: "prop_001"
│
├── 📁 operadores
│   └── 📄 oper_123
│       ├── nome: "João Silva"
│       ├── fazendaIds: ["faz_001"]
│       ├── proprietarioId: "prop_001"
│       ├── status: "ativo"
│       └── userId: "abc123def456ghi789"
│
└── 📁 userProfiles
    └── 📄 abc123def456ghi789
        ├── email: "joao@fazenda.com"
        ├── role: "operador"
        └── operadorId: "oper_123"
```

### 2. **Verificar Vinculações**

- ✅ Fazenda → proprietarioId = `"prop_001"`
- ✅ Operador → fazendaIds contém `"faz_001"`
- ✅ Operador → proprietarioId = `"prop_001"`
- ✅ Operador → userId = `"abc123def456ghi789"`
- ✅ UserProfile → Document ID = `"abc123def456ghi789"`
- ✅ UserProfile → operadorId = `"oper_123"`
- ✅ UserProfile → role = `"operador"`
- ✅ UserProfile → proprietarioId = `"prop_001"`
- ✅ Operador → status = `"ativo"`

---

## 🧪 TESTAR CONFIGURAÇÃO

### Via CURL:

```bash
# 1. Testar autenticação
curl -X GET "http://localhost:8080/api/v1/auth/me" \
  -H "X-User-UID: abc123def456ghi789"

# Deve retornar:
# {
#   "userProfile": { ... },
#   "operador": { ... }
# }
```

```bash
# 2. Testar status de pontos
curl -X GET "http://localhost:8080/api/v1/pontos/status" \
  -H "X-User-UID: abc123def456ghi789"

# Deve retornar:
# {
#   "pontoAberto": null,
#   "podeRegistrarEntrada": true,
#   "podeRegistrarSaida": false,
#   "pontosHoje": [],
#   "horasTrabalhadasHoje": 0.0,
#   "totalRegistrosHoje": 0
# }
```

### Via App:

1. Abrir app
2. Fazer login com:
   - **Email:** `joao@fazenda.com`
   - **Senha:** `senha123`
3. Deve redirecionar para tela de registro de pontos
4. Status deve mostrar: "⚪ Sem Ponto Aberto"
5. Botão "Registrar Entrada" deve estar habilitado
6. Botão "Registrar Saída" deve estar desabilitado

---

## 🔧 CRIAR MAIS OPERADORES

Para criar mais operadores, repita os passos 3-6 para cada um:

### **Operador 2: Maria Santos**

**1. Operador (`oper_456`):**
```json
{
  "id": "oper_456",
  "nome": "Maria Santos",
  "cpf": "987.654.321-00",
  "telefone": "(11) 91234-5678",
  "email": "maria@fazenda.com",
  "fazendaIds": ["faz_001"],
  "fazendaNomes": ["Fazenda São José"],
  "proprietarioId": "prop_001",
  "proprietarioNome": "Empresa Agrícola LTDA",
  "status": "ativo",
  "especialidades": ["Adubação", "Manutenção"]
}
```

**2. Firebase Auth:**
- Email: `maria@fazenda.com`
- Senha: `senha123`
- Copiar UID (ex: `xyz789abc456def123`)

**3. UserProfile (Document ID: `xyz789abc456def123`):**
```json
{
  "uid": "xyz789abc456def123",
  "displayName": "Maria Santos",
  "email": "maria@fazenda.com",
  "role": "operador",
  "operadorId": "oper_456",
  "proprietarioId": "prop_001",
  "mustChangePassword": false
}
```

**4. Atualizar Operador:**
```json
{
  "userId": "xyz789abc456def123"
}
```

---

## 🔐 CRIAR ADMIN (OPCIONAL)

Se quiser criar um admin para gerenciar o sistema:

### **1. Firebase Auth:**
- Email: `admin@fazenda.com`
- Senha: `admin123`
- Copiar UID (ex: `admin_uid_123`)

### **2. UserProfile (Document ID: `admin_uid_123`):**
```json
{
  "uid": "admin_uid_123",
  "displayName": "Administrador",
  "email": "admin@fazenda.com",
  "role": "admin",
  "phone": "(11) 3456-7890",
  "mustChangePassword": false
}
```

**⚠️ NOTA:** Admin **NÃO precisa** de `operadorId` nem `proprietarioId`.

---

## 📊 RESUMO - CAMPOS IMPORTANTES

### **Operador**
```json
{
  "status": "ativo",           // ✅ Deve ser "ativo" (não "inativo")
  "userId": "abc123...",       // ✅ UID do Firebase Auth
  "proprietarioId": "prop_001" // ✅ ID do proprietário
}
```

### **UserProfile**
```json
{
  // Document ID = UID do Firebase Auth
  "role": "operador",          // ✅ Deve ser "operador" (não "admin" nem "user")
  "operadorId": "oper_123",    // ✅ ID do operador
  "proprietarioId": "prop_001" // ✅ ID do proprietário (mesmo do operador)
}
```

---

## ❌ ERROS COMUNS

### Erro: "UserProfile não encontrado"
**Causa:** Document ID do UserProfile diferente do UID  
**Solução:** Deletar e criar novamente com ID correto

### Erro: "Operador não está ativo"
**Causa:** Campo `status` = `"inativo"`  
**Solução:** Alterar para `"ativo"`

### Erro: "Usuário não é um operador"
**Causa:** Campo `role` diferente de `"operador"`  
**Solução:** Alterar para `"operador"`

### Erro: "Operador não encontrado"
**Causa:** Campo `operadorId` no UserProfile está errado  
**Solução:** Verificar se aponta para operador existente

### Erro: "UserProfile não possui operadorId vinculado"
**Causa:** Campo `operadorId` não existe no UserProfile  
**Solução:** Adicionar campo `operadorId` com ID do operador

---

## 🎉 PRONTO!

Agora você pode:
- ✅ Fazer login no app
- ✅ Registrar pontos de entrada/saída
- ✅ Ver histórico de pontos
- ✅ Ver estatísticas de horas trabalhadas

---

**📝 NOTA:** Salve os IDs e UIDs criados para facilitar futuras configurações!

**Exemplo:**
```
Proprietário: prop_001
Fazenda: faz_001
Operador 1: oper_123 (João Silva)
  └─ UID: abc123def456ghi789
  └─ Email: joao@fazenda.com
  └─ Senha: senha123
Operador 2: oper_456 (Maria Santos)
  └─ UID: xyz789abc456def123
  └─ Email: maria@fazenda.com
  └─ Senha: senha123
```

