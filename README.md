# 🚀 SIGA API - Sistema de Gestão Agrícola

API RESTful completa para gerenciamento de fazendas com suporte a registro de pontos de operadores, chamados de suporte, notificações e controle multi-tenant, construída com **Spring Boot** + **Firebase Firestore**.

---

## 📋 ÍNDICE

1. [Visão Geral](#-visão-geral)
2. [Tecnologias](#-tecnologias)
3. [Estrutura do Projeto](#-estrutura-do-projeto)
4. [Autenticação e Segurança](#-autenticação-e-segurança)
5. [Endpoints da API](#-endpoints-da-api)
6. [Filtro por Proprietário](#-filtro-por-proprietário)
7. [Upload de Fotos (ImgBB)](#-upload-de-fotos-imgbb)
8. [Sistema de Notificações](#-sistema-de-notificações)
9. [Configuração de Rede](#-configuração-de-rede)
10. [Como Executar](#-como-executar)
11. [Integração com App Mobile](#-integração-com-app-mobile)
12. [Estrutura do Firestore](#-estrutura-do-firestore)

---

## 🎯 VISÃO GERAL

A **SIGA API** é uma solução completa para gestão de fazendas que oferece:

- ✅ **Gerenciamento de Fazendas** - CRUD completo de fazendas
- ✅ **Controle de Operadores** - Gerenciamento de operadores de máquinas
- ✅ **Gestão de Máquinas** - Controle de máquinas agrícolas
- ✅ **Administração de Talhões** - Organização de áreas de plantio
- ✅ **Registro de Trabalhos** - Acompanhamento de atividades agrícolas
- ✅ **Controle de Safras** - Gestão de safras por proprietário
- ✅ **Registro de Pontos** - Sistema completo de ponto eletrônico para operadores
- ✅ **Chamados de Suporte** - Sistema de chamados com upload de fotos
- ✅ **Notificações** - Sistema completo de notificações em tempo real
- ✅ **Autenticação Firebase** - Login seguro com Firebase Auth
- ✅ **Multi-tenant** - Suporte a múltiplos proprietários com filtro de segurança
- ✅ **Sincronização em tempo real** - Dados sempre atualizados

---

## 🛠️ TECNOLOGIAS

- **Java 17+**
- **Spring Boot 3.x**
- **Firebase Admin SDK** - Firestore Database
- **Firebase Authentication** - Autenticação de usuários
- **ImgBB API** - Armazenamento gratuito de fotos
- **Lombok** - Redução de código boilerplate
- **Maven** - Gerenciamento de dependências
- **CORS** habilitado para integração web/mobile

---

## 📁 ESTRUTURA DO PROJETO

```
src/main/java/com/siga/
├── model/                          # Entidades do sistema
│   ├── Fazenda.java
│   ├── Operador.java
│   ├── Maquina.java
│   ├── Talhao.java
│   ├── Trabalho.java
│   ├── Safra.java
│   ├── Proprietario.java
│   ├── Ponto.java
│   ├── Chamado.java
│   ├── Notificacao.java
│   └── UserProfile.java
│
├── dto/                            # Data Transfer Objects
│   ├── RegistroPontoRequest.java
│   ├── StatusOperadorResponse.java
│   ├── EstatisticasPontosResponse.java
│   ├── CriarChamadoRequest.java
│   ├── AtualizarChamadoRequest.java
│   ├── AdicionarObservacaoRequest.java
│   ├── FotoUploadResponse.java
│   ├── CriarNotificacaoRequest.java
│   └── NotificacaoBatchRequest.java
│
├── repository/                     # Acesso ao Firestore
│   ├── FazendaRepository.java
│   ├── OperadorRepository.java
│   ├── MaquinaRepository.java
│   ├── TalhaoRepository.java
│   ├── TrabalhoRepository.java
│   ├── SafraRepository.java
│   ├── ProprietarioRepository.java
│   ├── PontoRepository.java
│   ├── ChamadoRepository.java
│   ├── NotificacaoRepository.java
│   └── UserProfileRepository.java
│
├── service/                        # Lógica de negócio
│   ├── FazendaService.java
│   ├── OperadorService.java
│   ├── MaquinaService.java
│   ├── TalhaoService.java
│   ├── TrabalhoService.java
│   ├── SafraService.java
│   ├── ProprietarioService.java
│   ├── PontoService.java
│   ├── ChamadoService.java
│   ├── FotoService.java
│   ├── NotificacaoService.java
│   └── AuthService.java
│
├── controller/                     # Endpoints REST
│   ├── FazendaController.java
│   ├── OperadorController.java
│   ├── MaquinaController.java
│   ├── TalhaoController.java
│   ├── TrabalhoController.java
│   ├── SafraController.java
│   ├── ProprietarioController.java
│   ├── PontoController.java
│   ├── ChamadoController.java
│   ├── NotificacaoController.java
│   └── AuthController.java
│
├── config/
│   ├── FirebaseConfig.java
│   ├── CorsConfig.java
│   └── AsyncConfig.java
│
└── SigaApiApplication.java
```

---

## 🔐 AUTENTICAÇÃO E SEGURANÇA

### Header Obrigatório

**TODAS** as requisições precisam do header:
```
X-User-UID: {uid_do_firebase_auth}
```

### Roles do Sistema

| Role | Descrição | Permissões |
|------|-----------|------------|
| `admin` | Administrador do site | Acesso total a todos os dados |
| `user` | Usuário/Proprietário | Acessa apenas dados do seu proprietário |
| `operador` | Operador de máquina | Acessa apenas seus próprios dados e do proprietário vinculado |

### Fluxo de Autenticação

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

## 🌐 ENDPOINTS DA API

### BASE URL
```
http://localhost:8080/api/v1
```

### 📍 FAZENDAS

| Método | Endpoint | Descrição | Permissões |
|--------|----------|-----------|------------|
| `GET` | `/fazendas` | Lista fazendas | Admin: todas / User/Operador: só do proprietário |
| `GET` | `/fazendas/{id}` | Busca fazenda por ID | Admin: qualquer / User/Operador: só do proprietário |
| `GET` | `/fazendas/proprietario/{proprietarioId}` | Busca fazendas de um proprietário | Admin: qualquer / User/Operador: só o seu |

### 👨‍🌾 OPERADORES

| Método | Endpoint | Descrição | Permissões |
|--------|----------|-----------|------------|
| `GET` | `/operadores` | Lista operadores | Admin: todos / User/Operador: só do proprietário |
| `GET` | `/operadores/{id}` | Busca operador por ID | Admin: qualquer / User/Operador: só do proprietário |
| `GET` | `/operadores/fazenda/{fazendaId}` | Busca operadores de uma fazenda | Filtrado por proprietário |

### 🚜 MÁQUINAS

| Método | Endpoint | Descrição | Permissões |
|--------|----------|-----------|------------|
| `GET` | `/maquinas` | Lista máquinas | Admin: todas / User/Operador: filtrado via fazendas |
| `GET` | `/maquinas/{id}` | Busca máquina por ID | Filtrado por proprietário |
| `GET` | `/maquinas/fazenda/{fazendaId}` | Busca máquinas de uma fazenda | Filtrado por proprietário |

### 🌾 TALHÕES

| Método | Endpoint | Descrição | Permissões |
|--------|----------|-----------|------------|
| `GET` | `/talhoes` | Lista talhões | Admin: todos / User/Operador: filtrado via fazendas |
| `GET` | `/talhoes/{id}` | Busca talhão por ID | Filtrado por proprietário |
| `GET` | `/talhoes/fazenda/{fazendaId}` | Busca talhões de uma fazenda | Filtrado por proprietário |

### 🚜 TRABALHOS

| Método | Endpoint | Descrição | Permissões |
|--------|----------|-----------|------------|
| `GET` | `/trabalhos` | Lista trabalhos | Admin: todos / User/Operador: filtrado via fazendas |
| `GET` | `/trabalhos/{id}` | Busca trabalho por ID | Filtrado por proprietário |
| `GET` | `/trabalhos/fazenda/{fazendaId}` | Busca trabalhos de uma fazenda | Filtrado por proprietário |
| `GET` | `/trabalhos/talhao/{talhaoId}` | Busca trabalhos de um talhão | Filtrado por proprietário |
| `GET` | `/trabalhos/maquina/{maquinaId}` | Busca trabalhos de uma máquina | Filtrado por proprietário |
| `GET` | `/trabalhos/operador/{operadorId}` | Busca trabalhos de um operador | Filtrado por proprietário |
| `GET` | `/trabalhos/safra/{safraId}` | Busca trabalhos de uma safra | Filtrado por proprietário |

### 🌱 SAFRAS

| Método | Endpoint | Descrição | Permissões |
|--------|----------|-----------|------------|
| `GET` | `/safras` | Lista safras | Admin: todas / User/Operador: só do proprietário |
| `GET` | `/safras/{id}` | Busca safra por ID | Filtrado por proprietário |
| `GET` | `/safras/proprietario/{proprietarioId}` | Busca safras de um proprietário | Filtrado por proprietário |

### 🕐 REGISTRO DE PONTOS

Sistema completo de registro de ponto eletrônico para operadores.

#### Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/pontos/registrar` | Registrar entrada/saída |
| `GET` | `/pontos/status` | Status atual do operador |
| `GET` | `/pontos/hoje` | Pontos de hoje |
| `GET` | `/pontos/historico` | Histórico com filtros de data |
| `GET` | `/pontos/estatisticas` | Estatísticas de horas trabalhadas |
| `GET` | `/pontos/admin/proprietario/{id}` | Pontos por proprietário (admin) |
| `PUT` | `/pontos/admin/{id}` | Atualizar ponto (admin) |
| `DELETE` | `/pontos/admin/{id}` | Deletar ponto (admin) |

#### Exemplo: Registrar Ponto

```http
POST /api/v1/pontos/registrar
X-User-UID: firebase_uid_abc123
Content-Type: application/json
```

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
  "observacao": "Início do turno"
}
```

### 📞 CHAMADOS (SUPORTE/MANUTENÇÃO)

Sistema completo de chamados para operadores e usuários reportarem problemas.

#### Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/chamados` | Criar chamado |
| `GET` | `/chamados` | Listar chamados (filtrado por proprietário) |
| `GET` | `/chamados/{id}` | Buscar chamado específico |
| `PUT` | `/chamados/{id}` | Atualizar chamado (admin) |
| `POST` | `/chamados/{id}/observacoes` | Adicionar observação |
| `POST` | `/chamados/{id}/fotos` | Upload de foto |
| `DELETE` | `/chamados/{id}` | Deletar chamado |
| `GET` | `/chamados/admin/proprietario/{id}` | Chamados por proprietário (admin) |

#### Exemplo: Criar Chamado

```http
POST /api/v1/chamados
X-User-UID: firebase_uid_abc123
Content-Type: application/json
```

```json
{
  "titulo": "Problema na colhedeira C-120",
  "descricao": "A colhedeira está apresentando falha no motor",
  "tipo": "manutencao",
  "prioridade": "alta",
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308,
    "accuracy": 10.5
  },
  "fazendaId": "faz_001",
  "fazendaNome": "Fazenda São José",
  "maquinaId": "maq_001",
  "maquinaNome": "Colhedeira C-120"
}
```

**Tipos de Chamado:** `manutencao`, `problema`, `suporte`, `outro`  
**Prioridades:** `baixa`, `media`, `alta`, `urgente`  
**Status:** `aberto`, `em_andamento`, `resolvido`, `cancelado`

### 🔔 NOTIFICAÇÕES

Sistema completo de notificações em tempo real.

#### Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/notificacoes` | Listar todas do usuário |
| `GET` | `/notificacoes/nao-lidas` | Listar não lidas |
| `GET` | `/notificacoes/count` | Contar não lidas |
| `POST` | `/notificacoes` | Criar notificação |
| `PUT` | `/notificacoes/{id}/lida` | Marcar como lida |
| `PUT` | `/notificacoes/lidas` | Marcar todas como lidas |
| `DELETE` | `/notificacoes/{id}` | Deletar notificação |
| `POST` | `/notificacoes/batch` | Enviar para múltiplos (admin) |

#### Tipos de Notificação

| Tipo | Descrição | Cor sugerida |
|------|-----------|--------------|
| `info` | Informação geral | 🔵 Azul |
| `sucesso` | Ação concluída | 🟢 Verde |
| `alerta` | Atenção necessária | 🟡 Amarelo |
| `erro` | Problema/erro | 🔴 Vermelho |

#### Categorias

| Categoria | Descrição |
|-----------|-----------|
| `chamado` | Relacionado a chamados |
| `sistema` | Notificação do sistema |
| `ponto` | Relacionado a pontos |
| `geral` | Geral |

**Regra Especial:** Se `userId` for `"admin"` ao criar notificação, a API cria uma notificação para **todos** os usuários com role "admin".

### 🔐 AUTENTICAÇÃO

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/auth/me` | Informações do usuário logado |
| `GET` | `/auth/validate` | Validar token |

---

## 🔒 FILTRO POR PROPRIETÁRIO

### Conceito

A API implementa **filtro de segurança por `proprietarioId`** para garantir que usuários só vejam dados do seu proprietário.

### Regras de Acesso

| Role | Comportamento |
|------|---------------|
| **Admin** | Vê **TODOS** os dados (pode filtrar com `?proprietarioId=xxx`) |
| **User** | Vê apenas dados do **SEU proprietário** |
| **Operador** | Vê apenas dados do **proprietário do seu operador** |

### Filtro Direto vs Indireto

#### Filtro Direto (tem `proprietarioId`)
- ✅ Fazendas
- ✅ Operadores
- ✅ Safras
- ✅ Chamados

#### Filtro Indireto (via Fazendas)
- ⚠️ Talhões → filtrado via `fazendaId`
- ⚠️ Máquinas → filtrado via `fazendaIds[]`
- ⚠️ Trabalhos → filtrado via `fazendaId`

**Como funciona o filtro indireto:**
1. API busca fazendas do proprietário
2. Extrai IDs das fazendas
3. Busca recursos através das fazendas
4. Retorna recursos filtrados

**Limitação Firestore:** `whereIn()` aceita máximo 10 itens, então processamos em lotes de 10.

### Exemplo de Fluxo

```
1. Usuário solicita GET /api/v1/talhoes
   Header: X-User-UID: uid_usuario
   
2. API identifica proprietarioId do usuário
   → proprietarioId = "CCnyN3MpHq5XRtnl8VFV"
   
3. API busca fazendas do proprietário
   → 3 fazendas encontradas
   
4. API extrai IDs das fazendas
   → fazendaIds = ["fazenda_1", "fazenda_2", "fazenda_3"]
   
5. API busca talhões onde fazendaId IN fazendaIds
   → Processa em lotes de 10 (se necessário)
   
6. API retorna talhões filtrados
   → Apenas talhões das fazendas do proprietário
```

---

## 📸 UPLOAD DE FOTOS (ImgBB)

### Configuração

As fotos são armazenadas no **ImgBB** (100% gratuito):
- ✅ 32 MB por imagem
- ✅ Armazenamento ilimitado
- ✅ Sem expiração
- ✅ CDN global

### Endpoint

```http
POST /api/v1/chamados/{id}/fotos
X-User-UID: firebase_uid_abc123
Content-Type: multipart/form-data
```

**Form Data:**
```
foto: [arquivo de imagem]
```

**Response:**
```json
{
  "url": "https://i.ibb.co/abc123/chamado_001_1700000000.jpg",
  "fotoId": "uuid-gerado"
}
```

### Fluxo do Upload

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

---

## 🔔 SISTEMA DE NOTIFICAÇÕES

### Notificações Automáticas

Quando ações ocorrem nos chamados, notificações são criadas automaticamente:

```
OPERADOR CRIA CHAMADO
  → Notifica TODOS os admins
  → Tipo: "alerta" se urgente, "info" se não

ADMIN ASSUME CHAMADO (status → em_andamento)
  → Notifica OPERADOR que criou
  → Título: "Chamado em Atendimento"

ADMIN RESPONDE CHAMADO (adiciona observação)
  → Notifica OPERADOR
  → Título: "Chamado Respondido"

ADMIN RESOLVE CHAMADO (status → resolvido)
  → Notifica OPERADOR
  → Título: "Chamado Resolvido"
  → Tipo: "sucesso"
```

---

## 🌐 CONFIGURAÇÃO DE REDE

### Para Desenvolvimento

A API está configurada para aceitar conexões de qualquer dispositivo na rede local:

**Arquivo `application.properties`:**
```properties
server.address=0.0.0.0
server.port=8080
```

**Arquivo `CorsConfig.java`:**
- Permite requisições de `localhost`, redes locais (`192.168.*.*`, `10.*.*.*`, `172.16-31.*.*`) e `exp://*` (Expo Go)

### Configuração do App

Crie um arquivo `config/api.js` no app:

```javascript
// ⚠️ ALTERE PARA O IP DO SEU COMPUTADOR
const DEV_API_IP = '192.168.3.74'; // Use ipconfig para descobrir
const DEV_API_PORT = '8080';

const getApiUrl = () => {
  if (__DEV__) {
    return `http://${DEV_API_IP}:${DEV_API_PORT}/api/v1`;
  } else {
    return 'https://sua-api-producao.com/api/v1';
  }
};

export const API_URL = getApiUrl();
```

### Descobrir IP Local

**Windows:**
```powershell
ipconfig
```
Procure por `IPv4 Address` na conexão ativa.

**Mac/Linux:**
```bash
ifconfig
# ou
ip addr
```

### Requisitos

1. **Computador com a API** deve estar ligado e com a API rodando
2. **Dispositivo (celular/PC)** deve estar na **mesma rede Wi-Fi**
3. **IP da API** deve estar correto no app
4. **Firewall** não pode estar bloqueando a porta 8080

**Importante:** Não precisa cadastrar cada dispositivo. Qualquer dispositivo na mesma rede Wi-Fi pode acessar a API.

---

## 🚀 COMO EXECUTAR

### 1. Pré-requisitos

- Java 17+
- Maven
- Conta Firebase (Firestore habilitado)

### 2. Configuração Firebase

1. Crie um projeto no [Firebase Console](https://console.firebase.google.com)
2. Ative **Firestore Database**
3. Ative **Authentication** (Email/Password)
4. Baixe o arquivo `firebase-credentials.json` (Admin SDK)
5. Coloque em `src/main/resources/firebase-credentials.json`

### 3. Configurar application.properties

```properties
# src/main/resources/application.properties
spring.application.name=SIGA-API
server.port=8080
server.address=0.0.0.0

# Firebase
firebase.project-id=seu-projeto-id
firebase.credentials.path=src/main/resources/firebase-credentials.json

# Upload de fotos (ImgBB - GRATUITO)
imgbb.api.key=sua-chave-imgbb
```

### 4. Executar o Projeto

**Via Maven:**
```bash
mvn spring-boot:run
```

**Via JAR:**
```bash
mvn clean package
java -jar target/SIGA-API-0.0.1-SNAPSHOT.jar
```

### 5. Verificar

```bash
curl http://localhost:8080/api/v1/fazendas
```

---

## 📱 INTEGRAÇÃO COM APP MOBILE

### 1. Autenticação no App

```typescript
import auth from '@react-native-firebase/auth';

// Login
const userCredential = await signInWithEmailAndPassword(auth, email, password);
const uid = userCredential.user.uid;

// Salvar UID para usar nas requisições
await AsyncStorage.setItem('userUID', uid);
```

### 2. Fazer Requisições à API

```typescript
import { API_URL } from './config/api';

// Exemplo: Registrar Entrada
const uid = await AsyncStorage.getItem('userUID');

const response = await fetch(`${API_URL}/pontos/registrar`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-User-UID': uid
  },
  body: JSON.stringify({
    tipo: 'entrada',
    localizacao: {
      latitude: -23.550520,
      longitude: -46.633308
    }
  })
});

const data = await response.json();
```

### 3. Upload de Foto

```typescript
import * as ImagePicker from 'expo-image-picker';

// Capturar foto
const result = await ImagePicker.launchCameraAsync({
  mediaTypes: ImagePicker.MediaTypeOptions.Images,
  quality: 0.8
});

// Preparar FormData
const formData = new FormData();
formData.append('foto', {
  uri: result.assets[0].uri,
  type: 'image/jpeg',
  name: 'foto.jpg',
} as any);

// Enviar para API
const response = await fetch(`${API_URL}/chamados/${chamadoId}/fotos`, {
  method: 'POST',
  headers: {
    'X-User-UID': uid,
  },
  body: formData,
});

const data = await response.json();
console.log('URL da foto:', data.url);
```

---

## 🗄️ ESTRUTURA DO FIRESTORE

### Collections

```
📦 Firestore Database
├── 📁 fazendas/
│   └── {fazendaId}
│       ├── proprietarioId: string ✅
│       ├── nome: string
│       └── ...
│
├── 📁 operadores/
│   └── {operadorId}
│       ├── proprietarioId: string ✅
│       ├── userId: string
│       └── ...
│
├── 📁 maquinas/
│   └── {maquinaId}
│       ├── fazendaIds: string[] ⚠️ (filtro indireto)
│       └── ...
│
├── 📁 talhoes/
│   └── {talhaoId}
│       ├── fazendaId: string ⚠️ (filtro indireto)
│       └── ...
│
├── 📁 trabalhos/
│   └── {trabalhoId}
│       ├── fazendaId: string ⚠️ (filtro indireto)
│       └── ...
│
├── 📁 safras/
│   └── {safraId}
│       ├── proprietarioId: string ✅
│       └── ...
│
├── 📁 pontos/
│   └── {pontoId}
│       ├── userId: string
│       ├── operadorId: string
│       ├── proprietarioId: string ✅
│       ├── tipo: "entrada" | "saida"
│       └── ...
│
├── 📁 chamados/
│   └── {chamadoId}
│       ├── userId: string
│       ├── operadorId: string
│       ├── proprietarioId: string ✅
│       ├── status: "aberto" | "em_andamento" | "resolvido" | "cancelado"
│       ├── fotos: string[]
│       ├── observacoes: Observacao[]
│       └── ...
│
├── 📁 notificacoes/
│   └── {notificacaoId}
│       ├── userId: string
│       ├── titulo: string
│       ├── mensagem: string
│       ├── tipo: "info" | "sucesso" | "alerta" | "erro"
│       ├── categoria: "chamado" | "sistema" | "ponto" | "geral"
│       ├── lida: boolean
│       └── ...
│
└── 📁 userProfiles/
    └── {uid}
        ├── role: "admin" | "user" | "operador"
        ├── proprietarioId: string
        ├── operadorId: string (se role = "operador")
        └── ...
```

### Legenda

- ✅ **Filtro Direto**: Recurso tem `proprietarioId` diretamente
- ⚠️ **Filtro Indireto**: Recurso é filtrado via fazendas (não tem `proprietarioId`)

---

## 🐛 TROUBLESHOOTING

### Erro: "UserProfile não encontrado"
**Causa:** Usuário não tem perfil criado no Firestore  
**Solução:** Criar UserProfile no Firestore vinculando `proprietarioId` e `role`

### Erro: "Network Error" ou "Failed to fetch"
**Causas possíveis:**
- API não está rodando → Execute `mvn spring-boot:run`
- IP errado no app → Verifique seu IP com `ipconfig`
- Firewall bloqueando → Libere a porta 8080 no Windows Defender
- Redes diferentes → Conecte o celular na mesma rede Wi-Fi

### Erro: "CORS" ou "Access-Control-Allow-Origin"
**Causa:** Configuração CORS incorreta  
**Solução:** Verifique se `CorsConfig.java` existe e reinicie a API

### Erro: "Usuário não possui proprietário associado"
**Causa:** UserProfile não tem `proprietarioId`  
**Solução:** Adicione `proprietarioId` ao UserProfile no Firestore

### Erro: "Acesso negado a este chamado"
**Causa:** Tentando acessar chamado de outro proprietário  
**Solução:** Verifique se o chamado pertence ao seu `proprietarioId`

---

## 📝 CHANGELOG

### v3.0.0 (2024-11-27) - Sistema Completo
- ✅ Sistema de notificações implementado
- ✅ Filtro de segurança por proprietário
- ✅ Upload de fotos via ImgBB
- ✅ Configuração de rede para dispositivos móveis
- ✅ Filtro indireto para Talhões, Máquinas e Trabalhos
- ✅ Validações completas de acesso

### v2.0.0 (2024-11-24) - Sistema de Pontos e Chamados
- ✅ Sistema completo de registro de pontos
- ✅ Sistema de chamados com upload de fotos
- ✅ Autenticação com Firebase Auth
- ✅ Suporte a geolocalização (GPS)
- ✅ Cálculo automático de duração
- ✅ Estatísticas de horas trabalhadas

### v1.0.0 (2024-01-01) - Versão Inicial
- ✅ CRUD de Fazendas, Operadores, Máquinas, Talhões, Trabalhos, Safras
- ✅ Integração com Firebase Firestore

---

## 📄 LICENÇA

Este projeto é parte do Sistema SIGA - Todos os direitos reservados.

---

**🚀 API SIGA - Sistema Integrado de Gestão Agrícola**

*Desenvolvido com ❤️ para facilitar a gestão de fazendas*
