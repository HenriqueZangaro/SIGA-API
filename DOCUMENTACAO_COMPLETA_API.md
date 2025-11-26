# 📱 SIGA API - DOCUMENTAÇÃO COMPLETA PARA O APP MOBILE

## 🎯 VISÃO GERAL

Este documento contém **TODAS** as informações necessárias para desenvolver o aplicativo mobile que se conecta com a SIGA API.

---

# 📋 ÍNDICE

1. [Arquitetura do Sistema](#-arquitetura-do-sistema)
2. [Configuração da API](#-configuração-da-api)
3. [Autenticação](#-autenticação)
4. [Endpoints de Pontos](#-endpoints-de-pontos)
5. [Endpoints de Chamados](#-endpoints-de-chamados)
6. [Upload de Fotos](#-upload-de-fotos)
7. [Estrutura do Firestore](#-estrutura-do-firestore)
8. [Código do App (React Native)](#-código-do-app-react-native)
9. [Modo Offline](#-modo-offline)
10. [Troubleshooting](#-troubleshooting)

---

# 🏗️ ARQUITETURA DO SISTEMA

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│                 │         │                 │         │                 │
│   APP MOBILE    │◄───────►│    SIGA API     │◄───────►│    FIREBASE     │
│  (React Native) │         │  (Spring Boot)  │         │   (Firestore)   │
│                 │         │                 │         │                 │
└─────────────────┘         └────────┬────────┘         └─────────────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │     ImgBB       │
                            │  (Upload Fotos) │
                            │    GRATUITO     │
                            └─────────────────┘
```

### Componentes:
- **App Mobile**: React Native/Expo para operadores
- **SIGA API**: Java Spring Boot 3.x (porta 8080)
- **Firebase Firestore**: Banco de dados NoSQL
- **Firebase Auth**: Autenticação de usuários
- **ImgBB**: Armazenamento de fotos (100% gratuito)

---

# ⚙️ CONFIGURAÇÃO DA API

### Base URL:
```
http://localhost:8080/api/v1
```

### Para emuladores:
```typescript
const API_BASE_URL = Platform.select({
  web: 'http://localhost:8080/api/v1',
  android: 'http://10.0.2.2:8080/api/v1',  // Emulador Android
  ios: 'http://localhost:8080/api/v1'
});
```

### Configuração atual da API (`application.properties`):
```properties
# Servidor
server.port=8080

# Firebase
firebase.project-id=fazendas-1f2b8
firebase.credentials.path=src/main/resources/firebase-credentials.json

# Upload de fotos (ImgBB - GRATUITO)
imgbb.api.key=3e5b77fb20ef45ca33a2ce577a442451
```

---

# 🔐 AUTENTICAÇÃO

## Header Obrigatório

**TODAS** as requisições precisam do header:
```
X-User-UID: {uid_do_firebase_auth}
```

## Como obter o UID no App:

```typescript
import auth from '@react-native-firebase/auth';

// Obter usuário logado
const user = auth().currentUser;
const uid = user?.uid; // Este é o UID para enviar no header

// Exemplo de requisição autenticada
const response = await fetch(`${API_URL}/pontos/status`, {
  method: 'GET',
  headers: {
    'X-User-UID': uid,
    'Content-Type': 'application/json'
  }
});
```

## Roles do Sistema:

| Role | Descrição | Permissões |
|------|-----------|------------|
| `admin` | Administrador do site | Acesso total a todos os dados |
| `operador` | Operador de máquina | Acessa apenas seus próprios dados |
| `user` | Usuário/Proprietário | Acessa dados do proprietário vinculado |

## Fluxo de Autenticação:

```
1. App faz login no Firebase Auth
   ↓
2. App obtém o UID do usuário
   ↓
3. App envia UID no header X-User-UID
   ↓
4. API valida no Firestore (userProfiles/{uid})
   ↓
5. API verifica role e aplica permissões
```

---

# 🕐 ENDPOINTS DE PONTOS

## 1. Registrar Ponto (Entrada ou Saída)

```http
POST /api/v1/pontos/registrar
```

### Headers:
```
X-User-UID: seu_uid_aqui
Content-Type: application/json
```

### Body:
```json
{
  "tipo": "entrada",
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308,
    "accuracy": 10.5,
    "timestamp": 1700000000000
  },
  "fazendaId": "faz_001",
  "observacao": "Início do turno",
  "dispositivo": "Android 12",
  "versaoApp": "1.0.0"
}
```

### Campos:
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `tipo` | string | ✅ Sim | `"entrada"` ou `"saida"` |
| `localizacao` | object | ❌ Não | GPS do dispositivo |
| `localizacao.latitude` | number | - | Latitude |
| `localizacao.longitude` | number | - | Longitude |
| `localizacao.accuracy` | number | - | Precisão em metros |
| `localizacao.timestamp` | number | - | Timestamp da captura GPS |
| `fazendaId` | string | ❌ Não | ID da fazenda |
| `observacao` | string | ❌ Não | Observação do operador |
| `dispositivo` | string | ❌ Não | Info do dispositivo |
| `versaoApp` | string | ❌ Não | Versão do app |

### Response (200):
```json
{
  "id": "ponto_abc123",
  "tipo": "entrada",
  "dataHora": "2024-11-26T07:00:00Z",
  "mensagem": "Ponto registrado com sucesso"
}
```

### Erros:
```json
// 400 - Já tem entrada aberta
{ "erro": "Já existe um ponto de entrada aberto" }

// 400 - Não tem entrada para fechar
{ "erro": "Não há ponto de entrada aberto para registrar saída" }
```

---

## 2. Status do Operador

```http
GET /api/v1/pontos/status
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
```json
{
  "pontoAberto": {
    "id": "ponto_abc123",
    "tipo": "entrada",
    "dataHora": "2024-11-26T07:00:00Z",
    "operadorNome": "João Silva"
  },
  "podeRegistrarEntrada": false,
  "podeRegistrarSaida": true,
  "pontosHoje": [...],
  "horasTrabalhadasHoje": 5.5,
  "totalRegistrosHoje": 4,
  "ultimoPonto": {...}
}
```

---

## 3. Pontos de Hoje

```http
GET /api/v1/pontos/hoje
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
```json
[
  {
    "id": "ponto_001",
    "tipo": "entrada",
    "dataHora": "2024-11-26T07:00:00Z",
    "operadorNome": "João Silva",
    "localizacao": {
      "latitude": -23.550520,
      "longitude": -46.633308
    }
  },
  {
    "id": "ponto_002",
    "tipo": "saida",
    "dataHora": "2024-11-26T12:00:00Z",
    "duracaoMinutos": 300,
    "pontoEntradaId": "ponto_001"
  }
]
```

---

## 4. Histórico de Pontos

```http
GET /api/v1/pontos/historico?dataInicio=2024-11-01&dataFim=2024-11-30
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Query Params:
| Param | Tipo | Obrigatório | Formato |
|-------|------|-------------|---------|
| `dataInicio` | string | ❌ Não | `yyyy-MM-dd` |
| `dataFim` | string | ❌ Não | `yyyy-MM-dd` |

### Response (200):
Array de pontos (mesmo formato de "Pontos de Hoje")

---

## 5. Estatísticas de Pontos

```http
GET /api/v1/pontos/estatisticas?dataInicio=2024-11-01&dataFim=2024-11-30
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Query Params:
| Param | Tipo | Obrigatório | Formato |
|-------|------|-------------|---------|
| `dataInicio` | string | ✅ Sim | `yyyy-MM-dd` |
| `dataFim` | string | ✅ Sim | `yyyy-MM-dd` |

### Response (200):
```json
{
  "totalPontos": 44,
  "totalEntradas": 22,
  "totalSaidas": 22,
  "horasTrabalhadas": 176.5,
  "diasTrabalhados": 22,
  "mediaHorasDia": 8.02
}
```

---

# 📞 ENDPOINTS DE CHAMADOS

## 1. Criar Chamado

```http
POST /api/v1/chamados
```

### Headers:
```
X-User-UID: seu_uid_aqui
Content-Type: application/json
```

### Body:
```json
{
  "titulo": "Problema na colhedeira C-120",
  "descricao": "A colhedeira está apresentando falha no motor. O motor faz barulho estranho quando acelera.",
  "tipo": "manutencao",
  "prioridade": "alta",
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308,
    "accuracy": 10.5,
    "timestamp": 1700000000000
  },
  "fazendaId": "faz_001",
  "fazendaNome": "Fazenda São José",
  "talhaoId": "tal_001",
  "talhaoNome": "Talhão A",
  "maquinaId": "maq_001",
  "maquinaNome": "Colhedeira C-120",
  "sincronizado": true
}
```

### Campos:
| Campo | Tipo | Obrigatório | Valores Válidos |
|-------|------|-------------|-----------------|
| `titulo` | string | ✅ Sim | Texto livre |
| `descricao` | string | ✅ Sim | Texto livre |
| `tipo` | string | ✅ Sim | `manutencao`, `problema`, `suporte`, `outro` |
| `prioridade` | string | ✅ Sim | `baixa`, `media`, `alta`, `urgente` |
| `localizacao` | object | ❌ Não | Objeto GPS |
| `fazendaId` | string | ❌ Não | ID da fazenda |
| `fazendaNome` | string | ❌ Não | Nome da fazenda |
| `talhaoId` | string | ❌ Não | ID do talhão |
| `talhaoNome` | string | ❌ Não | Nome do talhão |
| `maquinaId` | string | ❌ Não | ID da máquina |
| `maquinaNome` | string | ❌ Não | Nome da máquina |

### Tipos de Chamado:
| Valor | Descrição |
|-------|-----------|
| `manutencao` | Manutenção preventiva ou corretiva |
| `problema` | Problema técnico |
| `suporte` | Dúvida ou suporte |
| `outro` | Outros tipos |

### Prioridades:
| Valor | Descrição | Urgência |
|-------|-----------|----------|
| `baixa` | Pode esperar | ⬜ |
| `media` | Normal | 🟨 |
| `alta` | Urgente | 🟧 |
| `urgente` | Crítico | 🟥 |

### Response (201):
```json
{
  "id": "chamado_abc123",
  "titulo": "Problema na colhedeira C-120",
  "status": "aberto",
  "dataCriacao": "2024-11-26T14:30:00Z",
  "mensagem": "Chamado criado com sucesso"
}
```

---

## 2. Listar Chamados

```http
GET /api/v1/chamados?status=aberto&tipo=manutencao&prioridade=alta
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Query Params (todos opcionais):
| Param | Tipo | Valores |
|-------|------|---------|
| `operadorId` | string | ID do operador (apenas admin) |
| `status` | string | `aberto`, `em_andamento`, `resolvido`, `cancelado` |
| `tipo` | string | `manutencao`, `problema`, `suporte`, `outro` |
| `prioridade` | string | `baixa`, `media`, `alta`, `urgente` |

### Response (200):
```json
[
  {
    "id": "chamado_001",
    "operadorId": "oper_123",
    "operadorNome": "João Silva",
    "titulo": "Problema na colhedeira",
    "descricao": "Motor falhando",
    "tipo": "manutencao",
    "prioridade": "alta",
    "status": "aberto",
    "dataHoraRegistro": "2024-11-26T14:30:00Z",
    "localizacao": {
      "latitude": -23.550520,
      "longitude": -46.633308
    },
    "fotos": [],
    "observacoes": [],
    "fazendaNome": "Fazenda São José",
    "maquinaNome": "Colhedeira C-120"
  }
]
```

---

## 3. Buscar Chamado Específico

```http
GET /api/v1/chamados/{id}
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
```json
{
  "id": "chamado_001",
  "operadorId": "oper_123",
  "operadorNome": "João Silva",
  "titulo": "Problema na colhedeira",
  "descricao": "A colhedeira está apresentando falha no motor",
  "tipo": "manutencao",
  "prioridade": "alta",
  "status": "em_andamento",
  "dataHoraRegistro": "2024-11-26T14:30:00Z",
  "dataHoraEnvio": "2024-11-26T14:32:00Z",
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308
  },
  "fotos": [
    "https://i.ibb.co/abc123/foto1.jpg",
    "https://i.ibb.co/def456/foto2.jpg"
  ],
  "fazendaId": "faz_001",
  "fazendaNome": "Fazenda São José",
  "maquinaId": "maq_001",
  "maquinaNome": "Colhedeira C-120",
  "responsavelId": "user_456",
  "responsavelNome": "Carlos Admin",
  "observacoes": [
    {
      "texto": "Equipe a caminho",
      "autor": "Carlos Admin",
      "autorId": "user_456",
      "data": "2024-11-26T15:00:00Z"
    }
  ],
  "proprietarioId": "prop_001"
}
```

---

## 4. Adicionar Observação

```http
POST /api/v1/chamados/{id}/observacoes
```

### Headers:
```
X-User-UID: seu_uid_aqui
Content-Type: application/json
```

### Body:
```json
{
  "observacao": "Problema resolvido. Troca de correia realizada."
}
```

### Response (200):
```json
{
  "mensagem": "Observação adicionada com sucesso"
}
```

---

## 5. Deletar Chamado

```http
DELETE /api/v1/chamados/{id}
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Regras:
- **Operador**: Apenas chamados com status `"aberto"` e criados por ele
- **Admin**: Qualquer chamado

### Response (200):
```json
{
  "mensagem": "Chamado deletado com sucesso"
}
```

---

# 📸 UPLOAD DE FOTOS

## Configuração

As fotos são armazenadas no **ImgBB** (100% gratuito):
- ✅ 32 MB por imagem
- ✅ Armazenamento ilimitado
- ✅ Sem expiração
- ✅ CDN global

## Endpoint

```http
POST /api/v1/chamados/{id}/fotos
```

### Headers:
```
X-User-UID: seu_uid_aqui
Content-Type: multipart/form-data
```

### Form Data:
| Campo | Tipo | Descrição |
|-------|------|-----------|
| `foto` | File | Arquivo de imagem (JPEG, PNG, GIF, WebP) |

### Response (200):
```json
{
  "url": "https://i.ibb.co/abc123/chamado_001_1700000000.jpg",
  "fotoId": "uuid-gerado"
}
```

## Fluxo do Upload:

```
1. App captura/seleciona foto
   ↓
2. App envia para API (multipart/form-data)
   ↓
3. API recebe e valida (max 32MB, apenas imagens)
   ↓
4. API converte para Base64
   ↓
5. API envia para ImgBB
   ↓
6. ImgBB retorna URL pública
   ↓
7. API salva URL no array "fotos" do chamado
   ↓
8. API retorna URL para o App
```

## Código React Native:

```typescript
import * as ImagePicker from 'expo-image-picker';

async function uploadFoto(chamadoId: string) {
  // 1. Escolher/capturar foto
  const result = await ImagePicker.launchCameraAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    quality: 0.8,
    allowsEditing: true,
  });

  if (result.canceled) return;

  // 2. Preparar FormData
  const formData = new FormData();
  formData.append('foto', {
    uri: result.assets[0].uri,
    type: 'image/jpeg',
    name: 'foto.jpg',
  } as any);

  // 3. Enviar para API
  const user = auth().currentUser;
  const response = await fetch(
    `${API_URL}/chamados/${chamadoId}/fotos`,
    {
      method: 'POST',
      headers: {
        'X-User-UID': user.uid,
        // NÃO defina Content-Type, o fetch faz automaticamente
      },
      body: formData,
    }
  );

  const data = await response.json();
  console.log('URL da foto:', data.url);
  // Exemplo: https://i.ibb.co/abc123/chamado_001.jpg

  return data.url;
}
```

---

# 🔔 ENDPOINTS DE NOTIFICAÇÕES

## 1. Listar Notificações

```http
GET /api/v1/notificacoes
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
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
    "createdAt": "2024-11-26T10:30:00Z"
  }
]
```

---

## 2. Listar Não Lidas

```http
GET /api/v1/notificacoes/nao-lidas
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
Array de notificações com `lida: false`

---

## 3. Contar Não Lidas

```http
GET /api/v1/notificacoes/count
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
```json
{
  "count": 5
}
```

---

## 4. Criar Notificação

```http
POST /api/v1/notificacoes
```

### Headers:
```
X-User-UID: seu_uid_aqui
Content-Type: application/json
```

### Body:
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

### Campos:
| Campo | Tipo | Obrigatório | Valores |
|-------|------|-------------|---------|
| `userId` | string | ✅ Sim | UID do destinatário ou "admin" para todos admins |
| `titulo` | string | ✅ Sim | Texto livre |
| `mensagem` | string | ✅ Sim | Texto livre |
| `tipo` | string | ✅ Sim | `info`, `sucesso`, `alerta`, `erro` |
| `categoria` | string | ✅ Sim | `chamado`, `sistema`, `ponto`, `geral` |
| `dados` | object | ❌ Não | Dados extras (chamadoId, etc) |

**Regra Especial:** Se `userId` for `"admin"`, a API cria uma notificação para **todos** os usuários com role "admin".

### Response (201):
```json
{
  "id": "notif_new_123",
  "titulo": "Novo Chamado",
  "lida": false,
  "createdAt": "2024-11-26T14:00:00Z"
}
```

---

## 5. Marcar como Lida

```http
PUT /api/v1/notificacoes/{id}/lida
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
```json
{
  "mensagem": "Notificação marcada como lida"
}
```

---

## 6. Marcar Todas como Lidas

```http
PUT /api/v1/notificacoes/lidas
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
```json
{
  "mensagem": "Todas as notificações foram marcadas como lidas",
  "atualizadas": 5
}
```

---

## 7. Deletar Notificação

```http
DELETE /api/v1/notificacoes/{id}
```

### Headers:
```
X-User-UID: seu_uid_aqui
```

### Response (200):
```json
{
  "mensagem": "Notificação deletada com sucesso"
}
```

---

## 8. Enviar para Múltiplos (Admin)

```http
POST /api/v1/notificacoes/batch
```

### Headers:
```
X-User-UID: seu_uid_admin
Content-Type: application/json
```

### Body:
```json
{
  "userIds": ["uid_1", "uid_2", "uid_3"],
  "titulo": "Manutenção Programada",
  "mensagem": "O sistema ficará indisponível das 22h às 23h.",
  "tipo": "alerta",
  "categoria": "sistema"
}
```

### Response (201):
```json
{
  "mensagem": "Notificações enviadas",
  "enviadas": 3
}
```

---

## Fluxo Automático de Notificações

Quando ações ocorrem nos chamados, notificações são criadas automaticamente:

```
┌─────────────────────────────────────────────────────────┐
│  OPERADOR CRIA CHAMADO                                  │
│  → Notifica TODOS os admins                             │
│  → Tipo: "alerta" se urgente, "info" se não             │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  ADMIN ASSUME CHAMADO (status → em_andamento)           │
│  → Notifica OPERADOR que criou                          │
│  → Título: "Chamado em Atendimento"                     │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  ADMIN RESPONDE CHAMADO (adiciona observação)           │
│  → Notifica OPERADOR                                    │
│  → Título: "Chamado Respondido"                         │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  ADMIN RESOLVE CHAMADO (status → resolvido)             │
│  → Notifica OPERADOR                                    │
│  → Título: "Chamado Resolvido"                          │
│  → Tipo: "sucesso"                                      │
└─────────────────────────────────────────────────────────┘
```

---

# 🗄️ ESTRUTURA DO FIRESTORE

## Collection: `pontos`

```json
{
  "id": "ponto_abc123",
  "operadorId": "oper_001",
  "operadorNome": "João Silva",
  "userId": "firebase_uid_abc123",
  "tipo": "entrada",
  "dataHora": "2024-11-26T07:00:00Z",
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308,
    "accuracy": 10.5,
    "timestamp": 1700000000000
  },
  "proprietarioId": "prop_001",
  "fazendaId": "faz_001",
  "fazendaNome": "Fazenda São José",
  "observacao": "Início do turno",
  "pontoEntradaId": null,
  "duracaoMinutos": null,
  "dispositivo": "Android 12",
  "versaoApp": "1.0.0",
  "dataCriacao": "2024-11-26T07:00:00Z",
  "ultimaAtualizacao": "2024-11-26T07:00:00Z"
}
```

## Collection: `chamados`

```json
{
  "id": "chamado_abc123",
  "operadorId": "oper_001",
  "operadorNome": "João Silva",
  "userId": "firebase_uid_abc123",
  "titulo": "Problema na colhedeira",
  "descricao": "Motor apresentando falhas",
  "tipo": "manutencao",
  "prioridade": "alta",
  "status": "aberto",
  "dataHoraRegistro": "2024-11-26T14:30:00Z",
  "dataHoraEnvio": "2024-11-26T14:32:00Z",
  "proprietarioId": "prop_001",
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308,
    "accuracy": 10.5,
    "timestamp": 1700000000000
  },
  "fotos": [
    "https://i.ibb.co/abc123/foto1.jpg",
    "https://i.ibb.co/def456/foto2.jpg"
  ],
  "fazendaId": "faz_001",
  "fazendaNome": "Fazenda São José",
  "talhaoId": "tal_001",
  "talhaoNome": "Talhão A",
  "maquinaId": "maq_001",
  "maquinaNome": "Colhedeira C-120",
  "responsavelId": null,
  "responsavelNome": null,
  "observacoes": [
    {
      "texto": "Equipe a caminho",
      "autor": "Carlos Admin",
      "autorId": "user_456",
      "data": "2024-11-26T15:00:00Z"
    }
  ],
  "sincronizado": true,
  "dataCriacao": "2024-11-26T14:30:00Z",
  "ultimaAtualizacao": "2024-11-26T15:00:00Z"
}
```

## Collection: `userProfiles`

```json
{
  "uid": "firebase_uid_abc123",
  "displayName": "João Silva",
  "email": "joao@exemplo.com",
  "photoURL": "https://...",
  "role": "operador",
  "phone": "(11) 98765-4321",
  "bio": "Operador de colhedeira",
  "permissao": "visualizador",
  "proprietarioId": "prop_001",
  "operadorId": "oper_001",
  "mustChangePassword": false,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-11-26T07:00:00Z"
}
```

## Collection: `notificacoes`

```json
{
  "id": "notif_abc123",
  "userId": "firebase_uid_abc123",
  "titulo": "Chamado Respondido",
  "mensagem": "Seu chamado 'Problema no motor' recebeu uma resposta.",
  "tipo": "info",
  "categoria": "chamado",
  "lida": false,
  "dados": {
    "chamadoId": "chamado_456",
    "prioridade": "alta"
  },
  "createdAt": "2024-11-26T10:30:00Z",
  "updatedAt": "2024-11-26T10:30:00Z"
}
```

### Tipos de Notificação:
| Tipo | Descrição | Cor sugerida |
|------|-----------|--------------|
| `info` | Informação geral | 🔵 Azul |
| `sucesso` | Ação concluída | 🟢 Verde |
| `alerta` | Atenção necessária | 🟡 Amarelo |
| `erro` | Problema/erro | 🔴 Vermelho |

### Categorias:
| Categoria | Descrição |
|-----------|-----------|
| `chamado` | Relacionado a chamados |
| `sistema` | Notificação do sistema |
| `ponto` | Relacionado a pontos |
| `geral` | Geral |

---

# 💻 CÓDIGO DO APP (REACT NATIVE)

## Dependências Necessárias

```bash
npm install @react-native-firebase/app @react-native-firebase/auth
npm install @react-native-async-storage/async-storage
npm install @react-native-community/netinfo
npm install axios
npm install expo-location
npm install expo-image-picker
```

## Configuração da API

```typescript
// config/api.ts
import { Platform } from 'react-native';

export const API_BASE_URL = Platform.select({
  web: 'http://localhost:8080/api/v1',
  android: 'http://10.0.2.2:8080/api/v1',
  ios: 'http://localhost:8080/api/v1',
  default: 'http://localhost:8080/api/v1',
});

export const API_ENDPOINTS = {
  // Pontos
  PONTOS_REGISTRAR: '/pontos/registrar',
  PONTOS_STATUS: '/pontos/status',
  PONTOS_HOJE: '/pontos/hoje',
  PONTOS_HISTORICO: '/pontos/historico',
  PONTOS_ESTATISTICAS: '/pontos/estatisticas',

  // Chamados
  CHAMADOS: '/chamados',
  CHAMADO_BY_ID: (id: string) => `/chamados/${id}`,
  CHAMADOS_OBSERVACOES: (id: string) => `/chamados/${id}/observacoes`,
  CHAMADOS_FOTOS: (id: string) => `/chamados/${id}/fotos`,

  // Auth
  AUTH_ME: '/auth/me',
  AUTH_VALIDATE: '/auth/validate',
};
```

## Serviço de API

```typescript
// services/apiService.ts
import auth from '@react-native-firebase/auth';
import { API_BASE_URL } from '../config/api';

class ApiService {
  private async getHeaders(): Promise<Record<string, string>> {
    const user = auth().currentUser;
    if (!user) throw new Error('Usuário não autenticado');

    return {
      'X-User-UID': user.uid,
      'Content-Type': 'application/json',
    };
  }

  async get<T>(endpoint: string): Promise<T> {
    const headers = await this.getHeaders();
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.erro || 'Erro na requisição');
    }

    return response.json();
  }

  async post<T>(endpoint: string, body: any): Promise<T> {
    const headers = await this.getHeaders();
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.erro || 'Erro na requisição');
    }

    return response.json();
  }

  async uploadFoto(endpoint: string, fotoUri: string): Promise<any> {
    const user = auth().currentUser;
    if (!user) throw new Error('Usuário não autenticado');

    const formData = new FormData();
    formData.append('foto', {
      uri: fotoUri,
      type: 'image/jpeg',
      name: 'foto.jpg',
    } as any);

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers: {
        'X-User-UID': user.uid,
      },
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.erro || 'Erro no upload');
    }

    return response.json();
  }
}

export const apiService = new ApiService();
```

## Serviço de Pontos

```typescript
// services/pontoService.ts
import { apiService } from './apiService';
import { API_ENDPOINTS } from '../config/api';
import * as Location from 'expo-location';

interface Localizacao {
  latitude: number;
  longitude: number;
  accuracy: number;
  timestamp: number;
}

interface RegistroPontoRequest {
  tipo: 'entrada' | 'saida';
  localizacao?: Localizacao;
  fazendaId?: string;
  observacao?: string;
  dispositivo?: string;
  versaoApp?: string;
}

class PontoService {
  async registrarPonto(tipo: 'entrada' | 'saida', observacao?: string) {
    // Obter localização
    let localizacao: Localizacao | undefined;
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status === 'granted') {
        const location = await Location.getCurrentPositionAsync({});
        localizacao = {
          latitude: location.coords.latitude,
          longitude: location.coords.longitude,
          accuracy: location.coords.accuracy || 0,
          timestamp: location.timestamp,
        };
      }
    } catch (error) {
      console.warn('Erro ao obter localização:', error);
    }

    const request: RegistroPontoRequest = {
      tipo,
      localizacao,
      observacao,
      dispositivo: 'React Native App',
      versaoApp: '1.0.0',
    };

    return apiService.post(API_ENDPOINTS.PONTOS_REGISTRAR, request);
  }

  async getStatus() {
    return apiService.get(API_ENDPOINTS.PONTOS_STATUS);
  }

  async getPontosHoje() {
    return apiService.get(API_ENDPOINTS.PONTOS_HOJE);
  }

  async getHistorico(dataInicio?: string, dataFim?: string) {
    let endpoint = API_ENDPOINTS.PONTOS_HISTORICO;
    const params = [];
    if (dataInicio) params.push(`dataInicio=${dataInicio}`);
    if (dataFim) params.push(`dataFim=${dataFim}`);
    if (params.length > 0) endpoint += '?' + params.join('&');

    return apiService.get(endpoint);
  }

  async getEstatisticas(dataInicio: string, dataFim: string) {
    return apiService.get(
      `${API_ENDPOINTS.PONTOS_ESTATISTICAS}?dataInicio=${dataInicio}&dataFim=${dataFim}`
    );
  }
}

export const pontoService = new PontoService();
```

## Serviço de Chamados

```typescript
// services/chamadoService.ts
import { apiService } from './apiService';
import { API_ENDPOINTS } from '../config/api';
import * as Location from 'expo-location';
import * as ImagePicker from 'expo-image-picker';

interface CriarChamadoRequest {
  titulo: string;
  descricao: string;
  tipo: 'manutencao' | 'problema' | 'suporte' | 'outro';
  prioridade: 'baixa' | 'media' | 'alta' | 'urgente';
  localizacao?: {
    latitude: number;
    longitude: number;
    accuracy: number;
    timestamp: number;
  };
  fazendaId?: string;
  fazendaNome?: string;
  talhaoId?: string;
  talhaoNome?: string;
  maquinaId?: string;
  maquinaNome?: string;
}

class ChamadoService {
  async criarChamado(dados: CriarChamadoRequest) {
    // Obter localização
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status === 'granted') {
        const location = await Location.getCurrentPositionAsync({});
        dados.localizacao = {
          latitude: location.coords.latitude,
          longitude: location.coords.longitude,
          accuracy: location.coords.accuracy || 0,
          timestamp: location.timestamp,
        };
      }
    } catch (error) {
      console.warn('Erro ao obter localização:', error);
    }

    return apiService.post(API_ENDPOINTS.CHAMADOS, dados);
  }

  async listarChamados(filtros?: {
    status?: string;
    tipo?: string;
    prioridade?: string;
  }) {
    let endpoint = API_ENDPOINTS.CHAMADOS;
    const params = [];
    if (filtros?.status) params.push(`status=${filtros.status}`);
    if (filtros?.tipo) params.push(`tipo=${filtros.tipo}`);
    if (filtros?.prioridade) params.push(`prioridade=${filtros.prioridade}`);
    if (params.length > 0) endpoint += '?' + params.join('&');

    return apiService.get(endpoint);
  }

  async getChamado(id: string) {
    return apiService.get(API_ENDPOINTS.CHAMADO_BY_ID(id));
  }

  async adicionarObservacao(id: string, observacao: string) {
    return apiService.post(API_ENDPOINTS.CHAMADOS_OBSERVACOES(id), {
      observacao,
    });
  }

  async tirarFotoEEnviar(chamadoId: string) {
    // Solicitar permissão
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') {
      throw new Error('Permissão de câmera negada');
    }

    // Capturar foto
    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.8,
      allowsEditing: true,
    });

    if (result.canceled) {
      throw new Error('Captura cancelada');
    }

    // Fazer upload
    return apiService.uploadFoto(
      API_ENDPOINTS.CHAMADOS_FOTOS(chamadoId),
      result.assets[0].uri
    );
  }

  async selecionarFotoEEnviar(chamadoId: string) {
    // Solicitar permissão
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') {
      throw new Error('Permissão de galeria negada');
    }

    // Selecionar foto
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.8,
      allowsEditing: true,
    });

    if (result.canceled) {
      throw new Error('Seleção cancelada');
    }

    // Fazer upload
    return apiService.uploadFoto(
      API_ENDPOINTS.CHAMADOS_FOTOS(chamadoId),
      result.assets[0].uri
    );
  }
}

export const chamadoService = new ChamadoService();
```

## Exemplo de Tela de Registro de Ponto

```typescript
// screens/RegistroPontoScreen.tsx
import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Alert, StyleSheet } from 'react-native';
import { pontoService } from '../services/pontoService';

export function RegistroPontoScreen() {
  const [status, setStatus] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    carregarStatus();
  }, []);

  async function carregarStatus() {
    try {
      const data = await pontoService.getStatus();
      setStatus(data);
    } catch (error: any) {
      Alert.alert('Erro', error.message);
    }
  }

  async function registrarPonto(tipo: 'entrada' | 'saida') {
    setLoading(true);
    try {
      await pontoService.registrarPonto(tipo);
      Alert.alert('Sucesso', `${tipo === 'entrada' ? 'Entrada' : 'Saída'} registrada!`);
      carregarStatus();
    } catch (error: any) {
      Alert.alert('Erro', error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Registro de Ponto</Text>

      {status?.pontoAberto && (
        <View style={styles.pontoAberto}>
          <Text>Ponto aberto desde:</Text>
          <Text style={styles.hora}>
            {new Date(status.pontoAberto.dataHora).toLocaleTimeString()}
          </Text>
        </View>
      )}

      <View style={styles.botoes}>
        <TouchableOpacity
          style={[styles.botao, styles.botaoEntrada]}
          onPress={() => registrarPonto('entrada')}
          disabled={loading || !status?.podeRegistrarEntrada}
        >
          <Text style={styles.textoBotao}>ENTRADA</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.botao, styles.botaoSaida]}
          onPress={() => registrarPonto('saida')}
          disabled={loading || !status?.podeRegistrarSaida}
        >
          <Text style={styles.textoBotao}>SAÍDA</Text>
        </TouchableOpacity>
      </View>

      <Text style={styles.horas}>
        Horas hoje: {status?.horasTrabalhadasHoje?.toFixed(1) || 0}h
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, alignItems: 'center' },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 30 },
  pontoAberto: { alignItems: 'center', marginBottom: 20 },
  hora: { fontSize: 32, fontWeight: 'bold', color: '#2196F3' },
  botoes: { flexDirection: 'row', gap: 20 },
  botao: { padding: 30, borderRadius: 100, width: 140, alignItems: 'center' },
  botaoEntrada: { backgroundColor: '#4CAF50' },
  botaoSaida: { backgroundColor: '#F44336' },
  textoBotao: { color: '#fff', fontSize: 18, fontWeight: 'bold' },
  horas: { marginTop: 30, fontSize: 18 },
});
```

---

# 📴 MODO OFFLINE

## Como Implementar

```typescript
// services/offlineService.ts
import AsyncStorage from '@react-native-async-storage/async-storage';
import NetInfo from '@react-native-community/netinfo';

const PONTOS_PENDENTES_KEY = '@app:pontos_pendentes';
const CHAMADOS_PENDENTES_KEY = '@app:chamados_pendentes';

class OfflineService {
  // Verificar conexão
  async isOnline(): Promise<boolean> {
    const state = await NetInfo.fetch();
    return state.isConnected === true;
  }

  // Salvar ponto offline
  async salvarPontoOffline(ponto: any) {
    const pendentes = await this.getPontosPendentes();
    ponto.id = 'temp_' + Date.now();
    ponto.dataHoraRegistro = new Date().toISOString();
    ponto.sincronizado = false;
    pendentes.push(ponto);
    await AsyncStorage.setItem(PONTOS_PENDENTES_KEY, JSON.stringify(pendentes));
  }

  // Obter pontos pendentes
  async getPontosPendentes(): Promise<any[]> {
    const data = await AsyncStorage.getItem(PONTOS_PENDENTES_KEY);
    return data ? JSON.parse(data) : [];
  }

  // Sincronizar quando online
  async sincronizarPontos() {
    if (!(await this.isOnline())) return;

    const pendentes = await this.getPontosPendentes();
    const sincronizados: string[] = [];

    for (const ponto of pendentes) {
      try {
        await pontoService.registrarPonto(ponto.tipo, ponto.observacao);
        sincronizados.push(ponto.id);
      } catch (error) {
        console.error('Erro ao sincronizar:', error);
      }
    }

    // Remover sincronizados
    const restantes = pendentes.filter(p => !sincronizados.includes(p.id));
    await AsyncStorage.setItem(PONTOS_PENDENTES_KEY, JSON.stringify(restantes));
  }

  // Listener de conexão
  setupConnectionListener() {
    NetInfo.addEventListener(state => {
      if (state.isConnected) {
        console.log('Conexão restaurada - sincronizando...');
        this.sincronizarPontos();
        this.sincronizarChamados();
      }
    });
  }
}

export const offlineService = new OfflineService();
```

---

# 🔧 TROUBLESHOOTING

## Erro: "Usuário não é um operador"
- **Causa**: UserProfile não tem `role: "operador"` ou `operadorId` não está definido
- **Solução**: Verificar no Firestore se `userProfiles/{uid}` tem os campos corretos

## Erro: "Could not deserialize object"
- **Causa**: Campos do Firestore não coincidem com os modelos Java
- **Solução**: Verificar se os campos de data são timestamps válidos

## Erro: CORS
- **Causa**: API não permite requisições cross-origin
- **Solução**: Verificar se `@CrossOrigin(origins = "*")` está nos controllers

## Erro: "Firebase credentials not found"
- **Causa**: Arquivo de credenciais não encontrado
- **Solução**: Colocar `firebase-credentials.json` em `src/main/resources/`

## Erro: Timeout no upload de foto
- **Causa**: Imagem muito grande ou conexão lenta
- **Solução**: Comprimir imagem antes do envio (quality: 0.6-0.8)

## Erro: "Network request failed"
- **Causa**: Endereço da API incorreto ou API não está rodando
- **Solução Android**: Usar `http://10.0.2.2:8080` em vez de `localhost`

---

# 📊 RESUMO DOS ENDPOINTS

## Pontos (8 endpoints)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/pontos/registrar` | Registrar entrada/saída |
| GET | `/pontos/status` | Status atual |
| GET | `/pontos/hoje` | Pontos de hoje |
| GET | `/pontos/historico` | Histórico com filtros |
| GET | `/pontos/estatisticas` | Estatísticas |
| GET | `/pontos/admin/proprietario/{id}` | Pontos por proprietário |
| PUT | `/pontos/admin/{id}` | Atualizar ponto |
| DELETE | `/pontos/admin/{id}` | Deletar ponto |

## Chamados (8 endpoints)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/chamados` | Criar chamado |
| GET | `/chamados` | Listar com filtros |
| GET | `/chamados/{id}` | Buscar específico |
| PUT | `/chamados/{id}` | Atualizar (admin) |
| POST | `/chamados/{id}/observacoes` | Adicionar observação |
| POST | `/chamados/{id}/fotos` | Upload de foto |
| DELETE | `/chamados/{id}` | Deletar chamado |
| GET | `/chamados/admin/proprietario/{id}` | Por proprietário |

## Notificações (8 endpoints)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/notificacoes` | Listar todas do usuário |
| GET | `/notificacoes/nao-lidas` | Listar não lidas |
| GET | `/notificacoes/count` | Contar não lidas |
| POST | `/notificacoes` | Criar notificação |
| PUT | `/notificacoes/{id}/lida` | Marcar como lida |
| PUT | `/notificacoes/lidas` | Marcar todas como lidas |
| DELETE | `/notificacoes/{id}` | Deletar notificação |
| POST | `/notificacoes/batch` | Enviar para múltiplos (admin) |

## Autenticação (2 endpoints)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/auth/me` | Info do usuário logado |
| GET | `/auth/validate` | Validar token |

---

# ✅ CHECKLIST DO APP

- [ ] Configurar Firebase Auth
- [ ] Configurar API Base URL
- [ ] Implementar login
- [ ] Implementar tela de registro de ponto
- [ ] Implementar tela de histórico de pontos
- [ ] Implementar tela de chamados
- [ ] Implementar criação de chamado
- [ ] Implementar upload de fotos
- [ ] Implementar modo offline
- [ ] Implementar sincronização automática
- [ ] Testar em emulador Android
- [ ] Testar em emulador iOS
- [ ] Testar em dispositivo físico

---

**Documentação atualizada em:** 26/11/2024  
**Versão da API:** 3.0.0  
**Status:** 🟢 **PRONTO PARA PRODUÇÃO**

