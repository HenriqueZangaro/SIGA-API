# 🚀 SIGA API - Sistema de Gestão Agrícola

API RESTful completa para gerenciamento de fazendas com suporte a registro de pontos de operadores, construída com **Spring Boot** + **Firebase Firestore**.

---

## 📋 ÍNDICE

1. [Visão Geral](#-visão-geral)
2. [Tecnologias](#-tecnologias)
3. [Estrutura do Projeto](#-estrutura-do-projeto)
4. [Endpoints da API](#-endpoints-da-api)
   - [Fazendas](#fazendas)
   - [Operadores](#operadores)
   - [Máquinas](#máquinas)
   - [Talhões](#talhões)
   - [Trabalhos](#trabalhos)
   - [Safras](#safras)
   - [Proprietários](#proprietários)
   - [**🆕 Registro de Pontos**](#-registro-de-pontos)
   - [**🆕 Chamados**](#-chamados-suportemanutenção)
   - [**🆕 Autenticação**](#-autenticação)
5. [Como Executar](#-como-executar)
6. [Configuração Firebase](#-configuração-firebase)
7. [Integração com App Mobile](#-integração-com-app-mobile)

---

## 🎯 VISÃO GERAL

A **SIGA API** é uma solução completa para gestão de fazendas que oferece:

- ✅ **Gerenciamento de Fazendas** - CRUD completo de fazendas
- ✅ **Controle de Operadores** - Gerenciamento de operadores de máquinas
- ✅ **Gestão de Máquinas** - Controle de máquinas agrícolas
- ✅ **Administração de Talhões** - Organização de áreas de plantio
- ✅ **Registro de Trabalhos** - Acompanhamento de atividades agrícolas
- ✅ **Controle de Safras** - Gestão de safras por proprietário
- ✅ **🆕 Registro de Pontos** - Sistema completo de ponto eletrônico para operadores
- ✅ **🆕 Chamados de Suporte** - Sistema de chamados com upload de fotos
- ✅ **🆕 Autenticação Firebase** - Login seguro com Firebase Auth
- ✅ **Multi-tenant** - Suporte a múltiplos proprietários
- ✅ **Sincronização em tempo real** - Dados sempre atualizados

---

## 🛠️ TECNOLOGIAS

- **Java 17+**
- **Spring Boot 3.x**
- **Firebase Admin SDK** - Firestore Database
- **Firebase Authentication** - Autenticação de usuários
- **Lombok** - Redução de código boilerplate
- **Maven** - Gerenciamento de dependências
- **CORS** habilitado para integração web/mobile

---

## 📁 ESTRUTURA DO PROJETO

```
src/main/java/com/siga/
├── model/                          # Entidades do sistema
│   ├── Fazenda.java
│   ├── Operador.java              # ✅ Atualizado com userId
│   ├── Maquina.java
│   ├── Talhao.java
│   ├── Trabalho.java
│   ├── Safra.java
│   ├── Proprietario.java
│   ├── 🆕 Ponto.java              # Sistema de pontos
│   ├── 🆕 Chamado.java            # Sistema de chamados
│   ├── 🆕 UserProfile.java        # Perfil de usuário
│   └── 🆕 OperadorAuth.java       # Autenticação
│
├── dto/                            # Data Transfer Objects
│   ├── 🆕 RegistroPontoRequest.java
│   ├── 🆕 StatusOperadorResponse.java
│   ├── 🆕 EstatisticasPontosResponse.java
│   ├── 🆕 CriarChamadoRequest.java
│   ├── 🆕 AtualizarChamadoRequest.java
│   ├── 🆕 AdicionarObservacaoRequest.java
│   └── 🆕 FotoUploadResponse.java
│
├── repository/                     # Acesso ao Firestore
│   ├── FazendaRepository.java
│   ├── OperadorRepository.java
│   ├── MaquinaRepository.java
│   ├── TalhaoRepository.java
│   ├── TrabalhoRepository.java
│   ├── SafraRepository.java
│   ├── ProprietarioRepository.java
│   ├── 🆕 PontoRepository.java
│   ├── 🆕 ChamadoRepository.java
│   └── 🆕 UserProfileRepository.java
│
├── service/                        # Lógica de negócio
│   ├── FazendaService.java
│   ├── OperadorService.java
│   ├── MaquinaService.java
│   ├── TalhaoService.java
│   ├── TrabalhoService.java
│   ├── SafraService.java
│   ├── ProprietarioService.java
│   ├── NotificacaoService.java
│   ├── SincronizacaoService.java
│   ├── 🆕 PontoService.java       # Serviço de pontos
│   ├── 🆕 ChamadoService.java     # Serviço de chamados
│   ├── 🆕 FotoService.java        # Upload de fotos
│   └── 🆕 AuthService.java        # Serviço de autenticação
│
├── controller/                     # Endpoints REST
│   ├── FazendaController.java
│   ├── OperadorController.java
│   ├── MaquinaController.java
│   ├── TalhaoController.java
│   ├── TrabalhoController.java
│   ├── SafraController.java
│   ├── ProprietarioController.java
│   ├── NotificacaoController.java
│   ├── SincronizacaoController.java
│   ├── 🆕 PontoController.java    # Endpoints de pontos
│   ├── 🆕 ChamadoController.java  # Endpoints de chamados
│   └── 🆕 AuthController.java     # Endpoints de autenticação
│
├── config/
│   ├── FirebaseConfig.java
│   └── AsyncConfig.java
│
└── SigaApiApplication.java        # Aplicação principal
```

---

## 🌐 ENDPOINTS DA API

### BASE URL
```
http://localhost:8080/api/v1
```

---

### 📍 FAZENDAS

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/fazendas` | Lista todas as fazendas |
| `GET` | `/fazendas/{id}` | Busca fazenda por ID |
| `GET` | `/fazendas/proprietario/{proprietarioId}` | Busca fazendas de um proprietário |

---

### 👨‍🌾 OPERADORES

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/operadores` | Lista todos os operadores |
| `GET` | `/operadores/{id}` | Busca operador por ID |
| `GET` | `/operadores/fazenda/{fazendaId}` | Busca operadores de uma fazenda |

**🆕 Estrutura do Operador:**
```json
{
  "id": "oper_123",
  "nome": "João Silva",
  "cpf": "123.456.789-00",
  "telefone": "(11) 98765-4321",
  "email": "joao@exemplo.com",
  "fazendaIds": ["faz_001", "faz_002"],
  "fazendaNomes": ["Fazenda São José", "Fazenda Santa Rita"],
  "proprietarioId": "prop_001",
  "proprietarioNome": "Empresa Agrícola LTDA",
  "status": "ativo",
  "especialidades": ["Plantio", "Colheita"],
  "userId": "firebase_uid_abc123",  // 🆕 Vinculação com Firebase Auth
  "dataCadastro": "2024-01-15T10:30:00Z"
}
```

---

### 🚜 MÁQUINAS

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/maquinas` | Lista todas as máquinas |
| `GET` | `/maquinas/{id}` | Busca máquina por ID |
| `GET` | `/maquinas/fazenda/{fazendaId}` | Busca máquinas de uma fazenda |

---

### 🌾 TALHÕES

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/talhoes` | Lista todos os talhões |
| `GET` | `/talhoes/{id}` | Busca talhão por ID |
| `GET` | `/talhoes/fazenda/{fazendaId}` | Busca talhões de uma fazenda |

---

### 🚜 TRABALHOS

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/trabalhos` | Lista todos os trabalhos |
| `GET` | `/trabalhos/{id}` | Busca trabalho por ID |
| `GET` | `/trabalhos/fazenda/{fazendaId}` | Busca trabalhos de uma fazenda |

---

### 🌱 SAFRAS

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/safras` | Lista todas as safras |
| `GET` | `/safras/{id}` | Busca safra por ID |
| `GET` | `/safras/proprietario/{proprietarioId}` | Busca safras de um proprietário |

---

### 🏢 PROPRIETÁRIOS

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/proprietarios` | Lista todos os proprietários |
| `GET` | `/proprietarios/{id}` | Busca proprietário por ID |

---

## 🆕 REGISTRO DE PONTOS

Sistema completo de registro de ponto eletrônico para operadores com suporte a:
- ✅ Múltiplos pontos no mesmo dia (entrada/saída)
- ✅ Cálculo automático de duração
- ✅ Captura de geolocalização (GPS)
- ✅ Estatísticas de horas trabalhadas
- ✅ Histórico completo

### 📍 Endpoints de Pontos

#### 1. **Registrar Ponto (Entrada ou Saída)**
```http
POST /api/v1/pontos/registrar
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
Content-Type: application/json
```

**Body:**
```json
{
  "tipo": "entrada",  // ou "saida"
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308,
    "accuracy": 10.5,
    "timestamp": 1700000000000
  },
  "fazendaId": "faz_001",  // opcional
  "observacao": "Início do turno",  // opcional
  "dispositivo": "Android 12",  // opcional
  "versaoApp": "1.0.0"  // opcional
}
```

**Response:**
```json
{
  "id": "ponto_abc123",
  "tipo": "entrada",
  "dataHora": "2024-11-24T07:00:00Z",
  "mensagem": "Ponto registrado com sucesso"
}
```

---

#### 2. **Status do Operador**
```http
GET /api/v1/pontos/status
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Response:**
```json
{
  "pontoAberto": {
    "id": "ponto_abc123",
    "tipo": "entrada",
    "dataHora": "2024-11-24T07:00:00Z",
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

#### 3. **Pontos de Hoje**
```http
GET /api/v1/pontos/hoje
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Response:**
```json
[
  {
    "id": "ponto_001",
    "tipo": "entrada",
    "dataHora": "2024-11-24T07:00:00Z",
    "operadorNome": "João Silva",
    "localizacao": {
      "latitude": -23.550520,
      "longitude": -46.633308
    }
  },
  {
    "id": "ponto_002",
    "tipo": "saida",
    "dataHora": "2024-11-24T12:00:00Z",
    "duracaoMinutos": 300,
    "pontoEntradaId": "ponto_001"
  }
]
```

---

#### 4. **Histórico de Pontos**
```http
GET /api/v1/pontos/historico?dataInicio=2024-11-01&dataFim=2024-11-30
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Query Params:**
- `dataInicio` (opcional): Data de início (formato: `yyyy-MM-dd`)
- `dataFim` (opcional): Data de fim (formato: `yyyy-MM-dd`)

**Response:** Array de pontos (mesmo formato de "Pontos de Hoje")

---

#### 5. **Estatísticas de Pontos**
```http
GET /api/v1/pontos/estatisticas?dataInicio=2024-11-01&dataFim=2024-11-30
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Query Params:**
- `dataInicio` (obrigatório): Data de início (formato: `yyyy-MM-dd`)
- `dataFim` (obrigatório): Data de fim (formato: `yyyy-MM-dd`)

**Response:**
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

#### 6. **Pontos por Proprietário (Admin)**
```http
GET /api/v1/pontos/admin/proprietario/{proprietarioId}?dataInicio=2024-11-01&dataFim=2024-11-30
```

**Headers:**
```
X-User-UID: admin_uid_xyz
```

**Query Params:**
- `dataInicio` (opcional): Data de início
- `dataFim` (opcional): Data de fim

**Response:** Array de pontos de todos os operadores do proprietário

---

#### 7. **Atualizar Ponto (Admin)**
```http
PUT /api/v1/pontos/admin/{id}
```

**Headers:**
```
X-User-UID: admin_uid_xyz
Content-Type: application/json
```

**Body:**
```json
{
  "observacao": "Atualizado pelo admin",
  "fazendaId": "faz_002"
}
```

---

#### 8. **Deletar Ponto (Admin)**
```http
DELETE /api/v1/pontos/admin/{id}
```

**Headers:**
```
X-User-UID: admin_uid_xyz
```

---

## 🆕 AUTENTICAÇÃO

### 📍 Endpoints de Autenticação

#### 1. **Buscar Informações do Usuário Logado**
```http
GET /api/v1/auth/me
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Response:**
```json
{
  "userProfile": {
    "uid": "firebase_uid_abc123",
    "displayName": "João Silva",
    "email": "joao@exemplo.com",
    "role": "operador",
    "operadorId": "oper_123",
    "proprietarioId": "prop_001"
  },
  "operador": {
    "id": "oper_123",
    "nome": "João Silva",
    "cpf": "123.456.789-00",
    "telefone": "(11) 98765-4321",
    "fazendaIds": ["faz_001"],
    "proprietarioId": "prop_001",
    "status": "ativo"
    }
}
```

---

#### 2. **Validar Token**
```http
GET /api/v1/auth/validate
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Response:**
```json
{
  "valido": true,
  "mensagem": "Token válido"
}
```

---

## 🆕 CHAMADOS (SUPORTE/MANUTENÇÃO)

Sistema completo de chamados para operadores reportarem problemas com suporte a:
- ✅ Diferentes tipos (manutenção, problema, suporte, outro)
- ✅ Níveis de prioridade (baixa, média, alta, urgente)
- ✅ Upload de fotos
- ✅ Captura de geolocalização
- ✅ Sistema de observações/comentários
- ✅ Controle de status (aberto, em_andamento, resolvido, cancelado)

### 📍 Endpoints de Chamados

#### 1. **Criar Chamado**
```http
POST /api/v1/chamados
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
Content-Type: application/json
```

**Body:**
```json
{
  "titulo": "Problema na colhedeira C-120",
  "descricao": "A colhedeira está apresentando falha no motor",
  "tipo": "manutencao",  // manutencao, problema, suporte, outro
  "prioridade": "alta",   // baixa, media, alta, urgente
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

**Response:**
```json
{
  "id": "chamado_abc123",
  "titulo": "Problema na colhedeira C-120",
  "status": "aberto",
  "dataCriacao": "2024-11-24T14:30:00Z",
  "mensagem": "Chamado criado com sucesso"
}
```

---

#### 2. **Listar Chamados**
```http
GET /api/v1/chamados?status=aberto&tipo=manutencao&prioridade=alta
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Query Params (todos opcionais):**
- `operadorId`: ID do operador (apenas para admin)
- `status`: aberto, em_andamento, resolvido, cancelado
- `tipo`: manutencao, problema, suporte, outro
- `prioridade`: baixa, media, alta, urgente

**Response:**
```json
[
  {
    "id": "chamado_001",
    "operadorId": "oper_123",
    "operadorNome": "João Silva",
    "titulo": "Problema na colhedeira",
    "descricao": "...",
    "tipo": "manutencao",
    "prioridade": "alta",
    "status": "aberto",
    "dataHoraRegistro": "2024-11-24T14:30:00Z",
    "localizacao": {...},
    "fotos": [],
    "observacoes": []
  }
]
```

---

#### 3. **Buscar Chamado Específico**
```http
GET /api/v1/chamados/{id}
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Response:**
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
  "dataHoraRegistro": "2024-11-24T14:30:00Z",
  "dataHoraEnvio": "2024-11-24T14:32:00Z",
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308
  },
  "fotos": [
    "https://storage.googleapis.com/.../foto1.jpg",
    "https://storage.googleapis.com/.../foto2.jpg"
  ],
  "fazendaNome": "Fazenda São José",
  "maquinaNome": "Colhedeira C-120",
  "responsavelId": "user_456",
  "responsavelNome": "Carlos Admin",
  "observacoes": [
    {
      "texto": "Equipe a caminho",
      "autor": "Carlos Admin",
      "autorId": "user_456",
      "data": "2024-11-24T15:00:00Z"
    }
  ],
  "proprietarioId": "prop_001"
}
```

---

#### 4. **Atualizar Chamado (Admin)**
```http
PUT /api/v1/chamados/{id}
```

**Headers:**
```
X-User-UID: admin_uid_xyz
Content-Type: application/json
```

**Body:**
```json
{
  "status": "em_andamento",
  "responsavelId": "user_456",
  "responsavelNome": "Carlos Admin",
  "prioridade": "urgente"
}
```

**Response:**
```json
{
  "mensagem": "Chamado atualizado com sucesso"
}
```

---

#### 5. **Adicionar Observação**
```http
POST /api/v1/chamados/{id}/observacoes
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
Content-Type: application/json
```

**Body:**
```json
{
  "observacao": "Problema resolvido. Troca de correia realizada."
}
```

**Response:**
```json
{
  "mensagem": "Observação adicionada com sucesso"
}
```

---

#### 6. **Upload de Foto**
```http
POST /api/v1/chamados/{id}/fotos
```

**Headers:**
```
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
  "url": "https://storage.googleapis.com/bucket/chamados/chamado_123/foto_abc.jpg",
  "fotoId": "foto_abc123"
}
```

---

#### 7. **Deletar Chamado**
```http
DELETE /api/v1/chamados/{id}
```

**Headers:**
```
X-User-UID: firebase_uid_abc123
```

**Regras:**
- Operador: Apenas chamados com status "aberto" e criados por ele
- Admin: Qualquer chamado

**Response:**
```json
{
  "mensagem": "Chamado deletado com sucesso"
}
```

---

#### 8. **Chamados por Proprietário (Admin)**
```http
GET /api/v1/chamados/admin/proprietario/{proprietarioId}?status=aberto
```

**Headers:**
```
X-User-UID: admin_uid_xyz
```

**Query Params (todos opcionais):**
- `status`: Filtrar por status
- `tipo`: Filtrar por tipo
- `prioridade`: Filtrar por prioridade

**Response:** Array de chamados do proprietário

---

## 🔐 ESTRUTURA DE DADOS - PONTO

### Estrutura do Ponto no Firestore

**Coleção:** `pontos`

```json
{
  "id": "ponto_abc123",
  "operadorId": "oper_123",
  "operadorNome": "João Silva",
  "userId": "firebase_uid_abc123",
  "tipo": "entrada",  // ou "saida"
  "dataHora": "2024-11-24T07:00:00Z",
  "localizacao": {
    "latitude": -23.550520,
    "longitude": -46.633308,
    "accuracy": 10.5,
    "timestamp": 1700000000000
  },
  "fazendaId": "faz_001",
  "fazendaNome": "Fazenda São José",
  "observacao": "Início do turno",
  "proprietarioId": "prop_001",
  "pontoEntradaId": "ponto_001",  // apenas em saída
  "duracaoMinutos": 300,  // calculado automaticamente em saída
  "dataCriacao": "2024-11-24T07:00:05Z",
  "dispositivo": "Android 12",
  "versaoApp": "1.0.0"
}
```

---

## 🔐 ESTRUTURA DE DADOS - USER PROFILE

### Estrutura do UserProfile no Firestore

**Coleção:** `userProfiles`

**Documento ID:** UID do Firebase Auth

```json
{
  "uid": "firebase_uid_abc123",
  "displayName": "João Silva",
  "email": "joao@exemplo.com",
  "photoURL": "https://...",
  "role": "operador",  // 'admin', 'user' ou 'operador'
  "phone": "(11) 98765-4321",
  "bio": "Operador de máquinas agrícolas",
  "permissao": "editor",  // apenas para role 'user'
  "proprietarioId": "prop_001",
  "operadorId": "oper_123",  // vinculação com operadores
  "mustChangePassword": false,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-11-24T08:00:00Z"
}
```

---

## 🔄 FLUXO DE AUTENTICAÇÃO

### 1. **Login no App (Firebase Auth)**
```
App → Firebase Auth → Retorna UID
```

### 2. **Requisições à API**
```
App envia UID no header 'X-User-UID' → API valida → Retorna dados
```

### 3. **Validação na API**
```java
@GetMapping("/pontos/status")
public ResponseEntity<?> getStatus(@RequestHeader("X-User-UID") String uid) {
    // API busca UserProfile pelo UID
    // Valida role = 'operador'
    // Busca Operador vinculado
    // Retorna dados do operador
}
```

---

## 🎯 LÓGICA DE REGISTRO DE PONTOS

### Fluxo de Entrada
```
1. Operador clica em "Registrar Entrada"
2. App captura GPS (opcional)
3. App envia POST /api/v1/pontos/registrar com tipo="entrada"
4. API verifica se não há ponto aberto
5. API registra ponto de entrada
6. Retorna sucesso
```

### Fluxo de Saída
```
1. Operador clica em "Registrar Saída"
2. App captura GPS (opcional)
3. App envia POST /api/v1/pontos/registrar com tipo="saida"
4. API verifica se há ponto de entrada aberto
5. API busca ponto de entrada
6. API calcula duração (saída - entrada)
7. API registra ponto de saída vinculado à entrada
8. Retorna sucesso com duração calculada
```

### Regras de Negócio
- ✅ **Entrada** só pode ser registrada se NÃO houver ponto aberto
- ✅ **Saída** só pode ser registrada se HOUVER ponto aberto
- ✅ Duração é calculada automaticamente na saída
- ✅ Sistema permite múltiplos pares entrada-saída no mesmo dia
- ✅ Localização é opcional (capturada se disponível)

---

## 🚀 COMO EXECUTAR

### 1. **Pré-requisitos**
- Java 17+
- Maven
- Conta Firebase (Firestore habilitado)

### 2. **Configuração Firebase**
1. Crie um projeto no [Firebase Console](https://console.firebase.google.com)
2. Ative **Firestore Database**
3. Ative **Authentication** (Email/Password)
4. Baixe o arquivo `firebase-credentials.json` (Admin SDK)
5. Coloque em `src/main/resources/firebase-credentials.json`

### 3. **Configurar application.properties**
```properties
# src/main/resources/application.properties
spring.application.name=SIGA-API
server.port=8080
```

### 4. **Executar o Projeto**

**Via Maven:**
```bash
mvn spring-boot:run
```

**Via JAR:**
```bash
mvn clean package
java -jar target/SIGA-API-0.0.1-SNAPSHOT.jar
```

### 5. **Verificar**
```bash
curl http://localhost:8080/api/v1/fazendas
```

---

## 📱 INTEGRAÇÃO COM APP MOBILE

### 1. **Autenticação no App**
```javascript
// Firebase Auth Login
const userCredential = await signInWithEmailAndPassword(auth, email, password);
const uid = userCredential.user.uid;

// Salvar UID para usar nas requisições
localStorage.setItem('userUID', uid);
```

### 2. **Fazer Requisições à API**
```javascript
// Exemplo: Registrar Entrada
const uid = localStorage.getItem('userUID');

const response = await fetch('http://localhost:8080/api/v1/pontos/registrar', {
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
    },
    dispositivo: 'Android 12',
    versaoApp: '1.0.0'
  })
});

const data = await response.json();
console.log('Ponto registrado:', data);
```

### 3. **Exemplo Completo: Buscar Status**
```javascript
const uid = localStorage.getItem('userUID');

const response = await fetch('http://localhost:8080/api/v1/pontos/status', {
  method: 'GET',
  headers: {
    'X-User-UID': uid
  }
});

const status = await response.json();

console.log('Ponto aberto:', status.pontoAberto);
console.log('Pode registrar entrada:', status.podeRegistrarEntrada);
console.log('Horas trabalhadas hoje:', status.horasTrabalhadasHoje);
```

---

## 🔐 CONFIGURAÇÃO FIRESTORE SECURITY RULES

Para garantir segurança, configure as regras do Firestore:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Função auxiliar: verifica se está autenticado
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Função auxiliar: verifica se é admin
    function isAdmin() {
      return isAuthenticated() && 
             get(/databases/$(database)/documents/userProfiles/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Função auxiliar: verifica se é o próprio operador
    function isOperadorDoPonto() {
      return isAuthenticated() && 
             request.auth.uid == resource.data.userId;
    }
    
    // Regras para pontos
    match /pontos/{pontoId} {
      // Leitura: admin, usuário com permissão, ou próprio operador
      allow read: if isAuthenticated() && (
        isAdmin() || isOperadorDoPonto()
      );
      
      // Criação: operador autenticado
      allow create: if isAuthenticated() && 
                     request.resource.data.userId == request.auth.uid &&
                     request.resource.data.tipo in ['entrada', 'saida'];
      
      // Atualização/Exclusão: apenas admin
      allow update, delete: if isAdmin();
    }
    
    // Regras para userProfiles
    match /userProfiles/{uid} {
      allow read: if isAuthenticated() && (request.auth.uid == uid || isAdmin());
      allow write: if isAdmin();
    }
    
    // Regras para operadores
    match /operadores/{operadorId} {
      allow read: if isAuthenticated();
      allow write: if isAdmin();
    }
    }
}
```

---

## 📊 ESTRUTURA DE COLEÇÕES NO FIRESTORE

```
📦 Firestore Database
├── 📁 fazendas/
├── 📁 operadores/          # ✅ Atualizado com userId
├── 📁 maquinas/
├── 📁 talhoes/
├── 📁 trabalhos/
├── 📁 safras/
├── 📁 proprietarios/
├── 📁 🆕 pontos/          # Sistema de pontos
├── 📁 🆕 userProfiles/    # Perfis de usuário
└── 📁 🆕 userProprietarios/  # Associações user-proprietario
```

---

## 🎯 PRÓXIMOS PASSOS PARA O APP MOBILE

### 1. **Criar Tela de Login**
- Login com Firebase Auth (Email/Password)
- Salvar UID do usuário
- Validar role = 'operador'
- Redirecionar para tela de registro de pontos

### 2. **Criar Tela de Registro de Pontos**
- Botão "Registrar Entrada" (verde)
- Botão "Registrar Saída" (vermelho)
- Card com status atual (ponto aberto/fechado)
- Card com horas trabalhadas hoje
- Histórico de pontos do dia

### 3. **Implementar Captura de GPS**
```javascript
// Exemplo: Capturar localização
navigator.geolocation.getCurrentPosition(
  (position) => {
    const localizacao = {
      latitude: position.coords.latitude,
      longitude: position.coords.longitude,
      accuracy: position.coords.accuracy,
      timestamp: Date.now()
    };
    
    // Usar na requisição
  },
  (error) => {
    console.error('Erro ao capturar GPS:', error);
    // Registrar ponto sem localização
  },
  { enableHighAccuracy: true, timeout: 10000 }
);
```

### 4. **Validar Permissões**
- Solicitar permissão de localização no primeiro uso
- Mostrar mensagem se permissão negada
- Permitir registro mesmo sem GPS

### 5. **Tratamento de Erros**
```javascript
try {
  const response = await fetch('...');
  
  if (!response.ok) {
    const error = await response.json();
    alert(error.erro);
    return;
  }
  
  const data = await response.json();
  // Sucesso
  
} catch (error) {
  console.error('Erro:', error);
  alert('Erro ao conectar com servidor');
}
```

---

## 🐛 TROUBLESHOOTING

### Erro: "UserProfile não encontrado"
**Causa:** Operador não tem login criado no Firebase  
**Solução:** Criar UserProfile no Firestore vinculando operadorId

### Erro: "Operador não está ativo"
**Causa:** Status do operador é 'inativo'  
**Solução:** Alterar status para 'ativo' no Firestore

### Erro: "Já existe um ponto de entrada aberto"
**Causa:** Operador tentou registrar entrada com ponto já aberto  
**Solução:** Registrar saída primeiro

### Erro: "Não há ponto de entrada aberto"
**Causa:** Operador tentou registrar saída sem entrada aberta  
**Solução:** Registrar entrada primeiro

---

## 📞 SUPORTE

Para dúvidas ou problemas:
1. Verificar logs do console da API
2. Verificar se Firebase está configurado corretamente
3. Verificar se o operador tem UserProfile criado
4. Verificar se o status do operador é 'ativo'

---

## 📝 CHANGELOG

### v2.0.0 (2024-11-24) - Sistema de Pontos
- ✅ Adicionado sistema completo de registro de pontos
- ✅ Autenticação com Firebase Auth
- ✅ Vinculação Operador ↔ UserProfile
- ✅ Suporte a geolocalização (GPS)
- ✅ Cálculo automático de duração
- ✅ Estatísticas de horas trabalhadas
- ✅ Endpoints para admin e operador
- ✅ Documentação completa atualizada

### v1.0.0 (2024-01-01) - Versão Inicial
- ✅ CRUD de Fazendas
- ✅ CRUD de Operadores
- ✅ CRUD de Máquinas
- ✅ CRUD de Talhões
- ✅ CRUD de Trabalhos
- ✅ CRUD de Safras
- ✅ CRUD de Proprietários
- ✅ Integração com Firebase Firestore

---

## 📄 LICENÇA

Este projeto é parte do Sistema SIGA - Todos os direitos reservados.

---

**🚀 API SIGA - Sistema Integrado de Gestão Agrícola**

*Desenvolvido com ❤️ para facilitar a gestão de fazendas*
