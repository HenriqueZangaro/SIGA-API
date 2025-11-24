# 🚀 GUIA RÁPIDO - Integração App Mobile

## 📱 INÍCIO RÁPIDO

Este guia mostra como integrar sua API SIGA com o app mobile em **5 passos simples**.

---

## 📋 PRÉ-REQUISITOS

Antes de começar, certifique-se de:
- ✅ API rodando em `http://localhost:8080` (ou URL do servidor)
- ✅ Firebase configurado no app (Authentication habilitado)
- ✅ Operadores criados no Firestore com `userId` vinculado
- ✅ UserProfiles criados para cada operador

---

## 🔥 CONFIGURAÇÃO INICIAL

### 1. **Criar Operador no Firestore**

**Collection:** `operadores`

```json
{
  "id": "oper_123",
  "nome": "João Silva",
  "cpf": "123.456.789-00",
  "telefone": "(11) 98765-4321",
  "email": "joao@fazenda.com",
  "fazendaIds": ["faz_001"],
  "fazendaNomes": ["Fazenda São José"],
  "proprietarioId": "prop_001",
  "proprietarioNome": "Empresa Agrícola LTDA",
  "status": "ativo",
  "especialidades": ["Plantio", "Colheita"],
  "userId": "DEIXAR_VAZIO_POR_ENQUANTO"
}
```

### 2. **Criar Usuário no Firebase Auth**

Via Firebase Console ou Admin SDK:
```javascript
// Email: joao@fazenda.com
// Senha: senha123
```

Após criar, copie o **UID** do usuário (ex: `abc123def456`).

### 3. **Criar UserProfile**

**Collection:** `userProfiles`  
**Document ID:** UID do Firebase Auth (ex: `abc123def456`)

```json
{
  "uid": "abc123def456",
  "displayName": "João Silva",
  "email": "joao@fazenda.com",
  "role": "operador",
  "operadorId": "oper_123",
  "proprietarioId": "prop_001",
  "mustChangePassword": false,
  "createdAt": "2024-11-24T10:00:00Z"
}
```

### 4. **Atualizar Operador com userId**

Volte no documento do operador e adicione o `userId`:

```json
{
  "userId": "abc123def456"
}
```

✅ **Pronto!** Agora o operador pode fazer login.

---

## 💻 CÓDIGO DO APP

### **PASSO 1: Configurar Firebase no App**

```javascript
// firebase.js
import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "SUA_API_KEY",
  authDomain: "SEU_PROJECT.firebaseapp.com",
  projectId: "SEU_PROJECT",
  // ... resto da config
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
```

---

### **PASSO 2: Tela de Login**

```javascript
// Login.jsx
import { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from './firebase';

function Login() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');

  const handleLogin = async () => {
    try {
      // 1. Fazer login no Firebase
      const userCredential = await signInWithEmailAndPassword(auth, email, senha);
      const uid = userCredential.user.uid;
      
      // 2. Salvar UID para usar nas requisições
      localStorage.setItem('userUID', uid);
      
      // 3. Validar que é operador
      const response = await fetch('http://localhost:8080/api/v1/auth/me', {
        headers: { 'X-User-UID': uid }
      });
      
      if (!response.ok) {
        alert('Erro ao buscar dados do operador');
        return;
      }
      
      const data = await response.json();
      
      // 4. Verificar role
      if (data.userProfile.role !== 'operador') {
        alert('Apenas operadores podem usar este app');
        return;
      }
      
      // 5. Salvar dados do operador
      localStorage.setItem('operadorId', data.operador.id);
      localStorage.setItem('operadorNome', data.operador.nome);
      
      // 6. Redirecionar para tela de pontos
      window.location.href = '/pontos';
      
    } catch (error) {
      console.error('Erro no login:', error);
      alert('Email ou senha incorretos');
    }
  };

  return (
    <div>
      <h1>Login - Sistema de Pontos</h1>
      <input 
        type="email" 
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      <input 
        type="password" 
        placeholder="Senha"
        value={senha}
        onChange={(e) => setSenha(e.target.value)}
      />
      <button onClick={handleLogin}>Entrar</button>
    </div>
  );
}
```

---

### **PASSO 3: Tela de Registro de Pontos**

```javascript
// RegistroPontos.jsx
import { useState, useEffect } from 'react';

function RegistroPontos() {
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  // Buscar status ao carregar a página
  useEffect(() => {
    buscarStatus();
  }, []);

  // Função para buscar status do operador
  const buscarStatus = async () => {
    try {
      const uid = localStorage.getItem('userUID');
      
      const response = await fetch('http://localhost:8080/api/v1/pontos/status', {
        headers: { 'X-User-UID': uid }
      });
      
      const data = await response.json();
      setStatus(data);
      
    } catch (error) {
      console.error('Erro ao buscar status:', error);
    }
  };

  // Função para registrar entrada
  const registrarEntrada = async () => {
    setLoading(true);
    
    try {
      // 1. Capturar GPS
      const localizacao = await capturarGPS();
      
      // 2. Fazer requisição
      const uid = localStorage.getItem('userUID');
      
      const response = await fetch('http://localhost:8080/api/v1/pontos/registrar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-UID': uid
        },
        body: JSON.stringify({
          tipo: 'entrada',
          localizacao: localizacao,
          dispositivo: navigator.userAgent,
          versaoApp: '1.0.0'
        })
      });
      
      if (!response.ok) {
        const error = await response.json();
        alert(error.erro);
        return;
      }
      
      const data = await response.json();
      
      alert('Entrada registrada com sucesso!');
      
      // 3. Atualizar status
      buscarStatus();
      
    } catch (error) {
      console.error('Erro ao registrar entrada:', error);
      alert('Erro ao registrar entrada');
    } finally {
      setLoading(false);
    }
  };

  // Função para registrar saída
  const registrarSaida = async () => {
    setLoading(true);
    
    try {
      // 1. Capturar GPS
      const localizacao = await capturarGPS();
      
      // 2. Fazer requisição
      const uid = localStorage.getItem('userUID');
      
      const response = await fetch('http://localhost:8080/api/v1/pontos/registrar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-UID': uid
        },
        body: JSON.stringify({
          tipo: 'saida',
          localizacao: localizacao,
          dispositivo: navigator.userAgent,
          versaoApp: '1.0.0'
        })
      });
      
      if (!response.ok) {
        const error = await response.json();
        alert(error.erro);
        return;
      }
      
      const data = await response.json();
      
      alert('Saída registrada com sucesso!');
      
      // 3. Atualizar status
      buscarStatus();
      
    } catch (error) {
      console.error('Erro ao registrar saída:', error);
      alert('Erro ao registrar saída');
    } finally {
      setLoading(false);
    }
  };

  // Função para capturar GPS
  const capturarGPS = () => {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        // GPS não disponível, retornar null
        resolve(null);
        return;
      }
      
      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy,
            timestamp: Date.now()
          });
        },
        (error) => {
          console.warn('Erro ao capturar GPS:', error);
          // Registrar mesmo sem GPS
          resolve(null);
        },
        { 
          enableHighAccuracy: true, 
          timeout: 10000,
          maximumAge: 0
        }
      );
    });
  };

  if (!status) {
    return <div>Carregando...</div>;
  }

  return (
    <div style={{ padding: '20px' }}>
      <h1>Registro de Pontos</h1>
      
      {/* Status Atual */}
      <div style={{ 
        padding: '20px', 
        backgroundColor: status.pontoAberto ? '#d4edda' : '#f8d7da',
        borderRadius: '8px',
        marginBottom: '20px'
      }}>
        <h2>
          {status.pontoAberto ? '🟢 Ponto Aberto' : '⚪ Sem Ponto Aberto'}
        </h2>
        
        {status.pontoAberto && (
          <p>
            Entrada registrada às: {new Date(status.pontoAberto.dataHora).toLocaleTimeString('pt-BR')}
          </p>
        )}
      </div>
      
      {/* Botões */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <button 
          onClick={registrarEntrada}
          disabled={!status.podeRegistrarEntrada || loading}
          style={{
            padding: '15px 30px',
            fontSize: '18px',
            backgroundColor: status.podeRegistrarEntrada ? '#28a745' : '#ccc',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: status.podeRegistrarEntrada ? 'pointer' : 'not-allowed'
          }}
        >
          🟢 Registrar Entrada
        </button>
        
        <button 
          onClick={registrarSaida}
          disabled={!status.podeRegistrarSaida || loading}
          style={{
            padding: '15px 30px',
            fontSize: '18px',
            backgroundColor: status.podeRegistrarSaida ? '#dc3545' : '#ccc',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: status.podeRegistrarSaida ? 'pointer' : 'not-allowed'
          }}
        >
          🔴 Registrar Saída
        </button>
      </div>
      
      {/* Estatísticas de Hoje */}
      <div style={{ 
        padding: '20px', 
        backgroundColor: '#e7f3ff',
        borderRadius: '8px'
      }}>
        <h3>📊 Estatísticas de Hoje</h3>
        <p><strong>Horas Trabalhadas:</strong> {status.horasTrabalhadasHoje.toFixed(2)}h</p>
        <p><strong>Registros:</strong> {status.totalRegistrosHoje}</p>
      </div>
      
      {/* Histórico de Hoje */}
      <div style={{ marginTop: '20px' }}>
        <h3>📋 Histórico de Hoje</h3>
        {status.pontosHoje.length === 0 ? (
          <p>Nenhum registro hoje</p>
        ) : (
          <ul>
            {status.pontosHoje.map((ponto, index) => (
              <li key={index} style={{ marginBottom: '10px' }}>
                {ponto.tipo === 'entrada' ? '🟢' : '🔴'} 
                {' '}
                {ponto.tipo.toUpperCase()} - {new Date(ponto.dataHora).toLocaleTimeString('pt-BR')}
                {ponto.duracaoMinutos && ` (${(ponto.duracaoMinutos / 60).toFixed(2)}h)`}
                {ponto.localizacao && ' 📍'}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

export default RegistroPontos;
```

---

## 🧪 TESTAR NO NAVEGADOR

### 1. **Login**
```
Email: joao@fazenda.com
Senha: senha123
```

### 2. **Registrar Entrada**
- Clicar no botão "Registrar Entrada"
- Permitir acesso à localização (se solicitado)
- Aguardar confirmação

### 3. **Ver Status**
- Status muda para "Ponto Aberto"
- Botão de entrada fica desabilitado
- Botão de saída fica habilitado

### 4. **Registrar Saída**
- Clicar no botão "Registrar Saída"
- Aguardar cálculo de duração
- Ver horas trabalhadas atualizar

---

## 🔧 SOLUÇÃO DE PROBLEMAS

### Erro: "UserProfile não encontrado"
**Solução:** Criar UserProfile no Firestore com o UID correto

### Erro: "Operador não está ativo"
**Solução:** Alterar `status` do operador para `"ativo"` no Firestore

### Erro: "Já existe um ponto de entrada aberto"
**Solução:** Registrar saída primeiro para fechar o ponto

### GPS não funciona
**Solução:** 
- Verificar permissões do navegador/app
- Sistema funciona mesmo sem GPS (localização opcional)

### CORS Error
**Solução:** 
- Verificar se `@CrossOrigin(origins = "*")` está nos controllers
- Ou configurar CORS global no Spring Boot

---

## 📚 RECURSOS ADICIONAIS

### Documentação Completa
- `README.md` - Documentação completa da API
- `SISTEMA_PONTOS_API.md` - Detalhes do sistema de pontos

### Collections Firestore
- `operadores` - Dados dos operadores
- `userProfiles` - Perfis de usuário
- `pontos` - Registros de pontos

### Endpoints Principais
- `POST /api/v1/pontos/registrar` - Registrar ponto
- `GET /api/v1/pontos/status` - Status do operador
- `GET /api/v1/pontos/hoje` - Pontos de hoje
- `GET /api/v1/pontos/historico` - Histórico com filtros
- `GET /api/v1/pontos/estatisticas` - Estatísticas

---

## 🎉 PRÓXIMOS PASSOS

1. ✅ Testar login no app
2. ✅ Testar registro de entrada/saída
3. ✅ Ver histórico funcionando
4. ✅ Ver estatísticas funcionando

Depois:
- 📱 Melhorar UI/UX
- 🔔 Adicionar notificações
- 📊 Adicionar gráficos
- 🔒 Adicionar geofencing (validar localização)
- 💾 Adicionar modo offline

---

**🚀 Boa sorte com o desenvolvimento!**

Se tiver dúvidas, consulte o `README.md` ou `SISTEMA_PONTOS_API.md`.

