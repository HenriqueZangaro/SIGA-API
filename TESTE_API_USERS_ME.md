# 🧪 Guia de Teste - API Users/Me

## ✅ Implementação Corrigida

A API foi ajustada para ficar 100% compatível com a estrutura real do Firestore do SIGA.

## 📋 Estruturas Validadas

### UserProfiles (Firestore)
```json
{
  "uid": "firebase_uid_123",
  "displayName": "João Silva",
  "email": "joao@exemplo.com",
  "photoURL": "https://...",
  "role": "user",
  "phone": "(11) 99999-9999",
  "bio": "Descrição opcional",
  "permissao": "dono",
  "proprietarioId": "prop_123",
  "operadorId": null,
  "mustChangePassword": false,
  "ultimaAtualizacao": "timestamp",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### Operadores (Firestore)
```json
{
  "id": "op_456",
  "nome": "Maria Santos",
  "cpf": "123.456.789-00",
  "telefone": "(11) 88888-8888",
  "email": "maria@exemplo.com",
  "fazendaIds": ["faz_1", "faz_2"],
  "fazendaNomes": ["Fazenda Norte", "Fazenda Sul"],
  "proprietarioId": "prop_123",
  "proprietarioNome": "Fazenda São João",
  "status": "ativo",
  "especialidades": ["Plantio", "Colheita"],
  "userId": "firebase_uid_789",
  "dataCadastro": "timestamp",
  "ultimaAtualizacao": "timestamp"
}
```

### Proprietarios (Firestore)
```json
{
  "id": "prop_123",
  "nome": "Fazenda São João Ltda",
  "tipo": "PJ",
  "documento": "12.345.678/0001-90",
  "email": "contato@fazenda.com",
  "telefone": "(16) 3333-4444",
  "endereco": "Rua das Fazendas, 123, Centro, Ribeirão Preto - SP, 14000-000",
  "dataCriacao": "timestamp",
  "ultimaAtualizacao": "timestamp"
}
```

## 🔌 Endpoint Principal

### GET /api/v1/users/me

**Headers:**
```
X-User-UID: [firebase_uid]
Content-Type: application/json
```

## 📝 Exemplos de Resposta

### 1. Admin (role: "admin")
```json
{
  "uid": "admin_uid_123",
  "nomeCompleto": "Administrador",
  "email": "admin@siga.com",
  "telefone": "(11) 99999-9999",
  "role": "admin",
  "permissao": "admin",
  "proprietarioId": "all",
  "proprietarioNome": "Sistema - Acesso Total",
  "operadorId": null,
  "photoURL": "https://...",
  "bio": "Administrador do sistema",
  "status": "ativo",
  "documento": null,
  "tipo": null,
  "endereco": null,
  "cpf": null,
  "especialidades": null,
  "fazendaIds": null,
  "fazendaNomes": null
}
```

### 2. User - Pessoa Física (role: "user", tipo: "PF")
```json
{
  "uid": "user_uid_456",
  "nomeCompleto": "João Silva",
  "email": "joao@exemplo.com",
  "telefone": "(11) 99999-9999",
  "role": "user",
  "permissao": "dono",
  "proprietarioId": "prop_123",
  "proprietarioNome": "João Silva",
  "operadorId": null,
  "photoURL": null,
  "bio": null,
  "status": "ativo",
  "documento": "123.456.789-00",
  "tipo": "PF",
  "endereco": "Rua das Fazendas, 123, Centro, Ribeirão Preto - SP, 14000-000",
  "cpf": null,
  "especialidades": null,
  "fazendaIds": null,
  "fazendaNomes": null
}
```

### 3. User - Pessoa Jurídica (role: "user", tipo: "PJ")
```json
{
  "uid": "user_uid_789",
  "nomeCompleto": "Fazenda São João Ltda",
  "email": "contato@fazenda.com",
  "telefone": "(16) 3333-4444",
  "role": "user",
  "permissao": "dono",
  "proprietarioId": "prop_456",
  "proprietarioNome": "Fazenda São João Ltda",
  "operadorId": null,
  "photoURL": null,
  "bio": null,
  "status": "ativo",
  "documento": "12.345.678/0001-90",
  "tipo": "PJ",
  "endereco": "Rodovia SP-330, Km 45, Zona Rural, Ribeirão Preto - SP, 14000-000",
  "cpf": null,
  "especialidades": null,
  "fazendaIds": null,
  "fazendaNomes": null
}
```

### 4. Operador (role: "operador")
```json
{
  "uid": "operador_uid_101",
  "nomeCompleto": "Maria Santos",
  "email": "maria@exemplo.com",
  "telefone": "(11) 88888-8888",
  "role": "operador",
  "permissao": null,
  "proprietarioId": "prop_123",
  "proprietarioNome": "Fazenda São João",
  "operadorId": "op_456",
  "photoURL": null,
  "bio": null,
  "status": "ativo",
  "documento": null,
  "tipo": null,
  "endereco": null,
  "cpf": "987.654.321-00",
  "especialidades": ["Plantio", "Colheita", "Irrigação"],
  "fazendaIds": ["faz_1", "faz_2"],
  "fazendaNomes": ["Fazenda Norte", "Fazenda Sul"]
}
```

## 🧪 Testes com cURL

### Teste 1: Admin
```bash
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "X-User-UID: admin_firebase_uid" \
  -H "Content-Type: application/json"
```

### Teste 2: User (Proprietário)
```bash
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "X-User-UID: user_firebase_uid" \
  -H "Content-Type: application/json"
```

### Teste 3: Operador
```bash
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "X-User-UID: operador_firebase_uid" \
  -H "Content-Type: application/json"
```

## ❌ Testes de Erro

### Erro 400 - UID vazio
```bash
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "X-User-UID: " \
  -H "Content-Type: application/json"
```
**Resposta:**
```json
{
  "erro": "Header X-User-UID é obrigatório"
}
```

### Erro 404 - Usuário não encontrado
```bash
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "X-User-UID: uid_inexistente" \
  -H "Content-Type: application/json"
```
**Resposta:**
```json
{
  "erro": "Perfil de usuário não encontrado"
}
```

### Erro 404 - Operador não encontrado
```bash
# Para userProfile com role "operador" mas operadorId inválido
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "X-User-UID: operador_sem_dados" \
  -H "Content-Type: application/json"
```
**Resposta:**
```json
{
  "erro": "Dados do operador não encontrados"
}
```

### Erro 404 - Proprietário não encontrado
```bash
# Para userProfile com role "user" mas proprietarioId inválido
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "X-User-UID: user_sem_proprietario" \
  -H "Content-Type: application/json"
```
**Resposta:**
```json
{
  "erro": "Dados do proprietário não encontrados"
}
```

## 🔍 Validações Implementadas

1. **UID obrigatório** - Não pode ser null ou vazio
2. **Role válido** - Deve ser "admin", "user" ou "operador"
3. **OperadorId obrigatório** - Para role "operador"
4. **ProprietarioId obrigatório** - Para role "user"
5. **Dados existentes** - Operador e Proprietário devem existir no Firestore

## 🚀 Para Usar no React Native

```javascript
const getUserData = async () => {
  try {
    const user = firebase.auth().currentUser;
    if (!user) {
      throw new Error('Usuário não logado');
    }

    const response = await fetch('http://sua-api.com/api/v1/users/me', {
      method: 'GET',
      headers: {
        'X-User-UID': user.uid,
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.erro || 'Erro ao buscar dados');
    }

    const userData = await response.json();
    
    // Agora você tem todos os dados corretos:
    console.log('Nome:', userData.nomeCompleto); // Nome real, não mais "Usuário"
    console.log('CPF/CNPJ:', userData.documento || userData.cpf);
    console.log('Telefone:', userData.telefone);
    console.log('Endereço:', userData.endereco);
    console.log('Role:', userData.role);
    
    // Atualizar estado do app
    setUserName(userData.nomeCompleto || 'Usuário');
    setUserData(userData);
    
  } catch (error) {
    console.error('Erro ao buscar dados do usuário:', error);
    Alert.alert('Erro', error.message);
  }
};
```

## ✅ Checklist de Validação

- [ ] Endpoint compila sem erros
- [ ] Admin retorna dados corretos
- [ ] User PF retorna documento como CPF
- [ ] User PJ retorna documento como CNPJ
- [ ] Operador retorna CPF e especialidades
- [ ] Erros 404 funcionam corretamente
- [ ] Logs aparecem no console
- [ ] React Native consegue consumir os dados
- [ ] Nome real aparece no app (não mais "Usuário")

## 🎯 Resultado Esperado

Após implementar essas correções, o app React Native deve:
1. **Mostrar o nome real** da pessoa logada
2. **Exibir CPF/CNPJ** quando disponível
3. **Mostrar telefone e endereço** corretos
4. **Funcionar para todos os tipos** de usuário (admin, user, operador)
5. **Tratar erros** adequadamente