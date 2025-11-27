# 🌐 Configuração de Rede - API e App SIGA

Este documento explica como configurar o aplicativo móvel para se conectar à API Spring Boot, tanto em desenvolvimento quanto em produção.

---

## 📋 Índice

1. [Visão Geral](#1-visão-geral)
2. [Configuração da API (Já Feita)](#2-configuração-da-api-já-feita)
3. [Configuração do App](#3-configuração-do-app)
4. [Como Funciona na Prática](#4-como-funciona-na-prática)
5. [Descobrir seu IP Local](#5-descobrir-seu-ip-local)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Visão Geral

### Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                      REDE LOCAL (Wi-Fi)                      │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ 📱 Celular 1 │  │ 📱 Celular 2 │  │ 💻 PC Web    │       │
│  │ Expo Go      │  │ Expo Go      │  │ Navegador    │       │
│  │              │  │              │  │              │       │
│  │ IP: Qualquer │  │ IP: Qualquer │  │ localhost    │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                 │                 │                │
│         └────────────────┬┴─────────────────┘                │
│                          │                                   │
│                          ▼                                   │
│              ┌─────────────────────────┐                    │
│              │    🖥️ COMPUTADOR COM    │                    │
│              │    API SPRING BOOT      │                    │
│              │                         │                    │
│              │  IP: 192.168.3.74       │                    │
│              │  Porta: 8080            │                    │
│              │                         │                    │
│              │  Aceita conexões de     │                    │
│              │  QUALQUER dispositivo   │                    │
│              │  na rede local!         │                    │
│              └─────────────────────────┘                    │
└─────────────────────────────────────────────────────────────┘
```

### Resumo

| Componente | Configuração | Status |
|------------|--------------|--------|
| API Spring Boot | CORS + server.address=0.0.0.0 | ✅ Configurado |
| App React Native | URL da API dinâmica | 📝 Configurar |

---

## 2. Configuração da API (Já Feita)

### 2.1. Arquivo `CorsConfig.java`

**Caminho:** `src/main/java/com/siga/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOriginPatterns(
                        "http://localhost:*",      // PC local
                        "http://192.168.*.*:*",    // Redes 192.168.x.x
                        "http://10.*.*.*:*",       // Redes 10.x.x.x
                        // ... outras redes locais
                        "*"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(3600);
            }
        };
    }
}
```

### 2.2. Arquivo `application.properties`

**Caminho:** `src/main/resources/application.properties`

```properties
# IMPORTANTE: 0.0.0.0 permite conexões de qualquer IP
server.address=0.0.0.0
server.port=8080
```

### 2.3. O que isso significa?

| Configuração | Efeito |
|--------------|--------|
| `server.address=0.0.0.0` | API aceita conexões de qualquer IP (não só localhost) |
| `allowedOriginPatterns` | Permite requisições de qualquer dispositivo na rede local |
| `allowedMethods` | Permite todos os métodos HTTP (GET, POST, PUT, DELETE) |
| `allowedHeaders("*")` | Permite qualquer header, incluindo `X-User-UID` |

---

## 3. Configuração do App

### 3.1. Criar arquivo de configuração

Crie um arquivo `config/api.js` ou `config/api.ts` no seu projeto React Native:

```javascript
// config/api.js

// ⚠️ ALTERE PARA O IP DO SEU COMPUTADOR
const DEV_API_IP = '192.168.3.74';
const DEV_API_PORT = '8080';

// URL de produção (quando publicar)
const PROD_API_URL = 'https://sua-api.herokuapp.com/api/v1';

// Detecta automaticamente o ambiente
const getApiUrl = () => {
  if (__DEV__) {
    // Desenvolvimento - usa IP local
    return `http://${DEV_API_IP}:${DEV_API_PORT}/api/v1`;
  } else {
    // Produção - usa URL do servidor
    return PROD_API_URL;
  }
};

export const API_URL = getApiUrl();
export const API_BASE = getApiUrl().replace('/api/v1', '');

// Para debug
console.log('🌐 API URL:', API_URL);
```

### 3.2. Usar no código

```javascript
// services/api.js
import { API_URL } from '../config/api';

// Exemplo de requisição
export const getPontos = async (uid) => {
  const response = await fetch(`${API_URL}/pontos/historico`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-UID': uid,
    },
  });
  return response.json();
};

// Exemplo com axios
import axios from 'axios';

const api = axios.create({
  baseURL: API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para adicionar UID
api.interceptors.request.use((config) => {
  const uid = getStoredUID(); // Sua função para pegar o UID
  if (uid) {
    config.headers['X-User-UID'] = uid;
  }
  return config;
});

export default api;
```

### 3.3. Configuração alternativa com variáveis de ambiente

Se preferir usar `.env`:

**Arquivo `.env`:**
```env
API_IP=192.168.3.74
API_PORT=8080
```

**Arquivo `config/api.js`:**
```javascript
import { API_IP, API_PORT } from '@env';

export const API_URL = __DEV__
  ? `http://${API_IP}:${API_PORT}/api/v1`
  : 'https://sua-api-producao.com/api/v1';
```

---

## 4. Como Funciona na Prática

### 4.1. Dinâmica de Acesso

```
┌─────────────────────────────────────────────────────────────┐
│                    PERGUNTA FREQUENTE                        │
│                                                              │
│  "Preciso cadastrar cada celular/PC que vai usar o app?"    │
│                                                              │
│                         NÃO! ❌                              │
└─────────────────────────────────────────────────────────────┘

A API está configurada para aceitar QUALQUER dispositivo
que esteja na mesma rede Wi-Fi.
```

### 4.2. Cenários de Uso

| Cenário | Funciona? | Por quê? |
|---------|-----------|----------|
| Seu celular na rede Wi-Fi de casa | ✅ SIM | Mesmo IP range (192.168.x.x) |
| Celular de colega na mesma rede | ✅ SIM | Mesmo IP range |
| PC na mesma rede | ✅ SIM | Mesmo IP range |
| Celular em outra rede Wi-Fi | ❌ NÃO | Rede diferente |
| Celular usando 4G/5G | ❌ NÃO | Rede diferente |

### 4.3. Requisitos para Funcionar

1. **Computador com a API** deve estar ligado e com a API rodando
2. **Dispositivo (celular/PC)** deve estar na **mesma rede Wi-Fi**
3. **IP da API** deve estar correto no app (`192.168.3.74` no seu caso)
4. **Firewall** não pode estar bloqueando a porta 8080

---

## 5. Descobrir seu IP Local

### Windows (PowerShell ou CMD)

```powershell
ipconfig
```

Procure por `IPv4 Address` na conexão ativa (geralmente Wi-Fi ou Ethernet):

```
Adaptador de Rede sem Fio Wi-Fi:
   Endereço IPv4. . . . . . . .  . . . . . . . : 192.168.3.74  ← SEU IP
```

### Mac / Linux

```bash
ifconfig
# ou
ip addr
```

### Celular (Android)

1. Configurações → Wi-Fi
2. Toque na rede conectada
3. Veja o campo "Endereço IP"

---

## 6. Troubleshooting

### ❌ Erro: "Network Error" ou "Failed to fetch"

**Causas possíveis:**

| Causa | Solução |
|-------|---------|
| API não está rodando | Execute `mvn spring-boot:run` |
| IP errado no app | Verifique seu IP com `ipconfig` |
| Firewall bloqueando | Libere a porta 8080 no Windows Defender |
| Redes diferentes | Conecte o celular na mesma rede Wi-Fi |

**Testar se a API está acessível:**

No navegador do celular, acesse:
```
http://192.168.3.74:8080/api/v1/fazendas
```

Se mostrar JSON, a API está funcionando.

### ❌ Erro: "CORS" ou "Access-Control-Allow-Origin"

Isso **não deveria acontecer** com a configuração atual. Se acontecer:

1. Verifique se o arquivo `CorsConfig.java` existe
2. Reinicie a API
3. Verifique os logs da API para erros

### ❌ App funciona no PC mas não no celular

1. Verifique se o celular está na mesma rede Wi-Fi
2. Verifique se o IP no app está correto
3. Teste acessando a API pelo navegador do celular

### ❌ IP mudou

Se seu computador recebeu um novo IP do roteador:

1. Descubra o novo IP com `ipconfig`
2. Atualize o arquivo `config/api.js` no app
3. Recarregue o app (shake + "Reload")

**Dica:** Para evitar isso, configure um IP fixo no seu computador.

---

## 📝 Checklist de Configuração

### API (Já feito ✅)
- [x] `CorsConfig.java` criado
- [x] `server.address=0.0.0.0` no `application.properties`
- [x] API aceita requisições de qualquer IP local

### App (Fazer agora 📝)
- [ ] Criar arquivo `config/api.js`
- [ ] Configurar IP correto (`192.168.3.74`)
- [ ] Testar no emulador
- [ ] Testar no celular físico

---

## 🚀 Resumo Final

| Pergunta | Resposta |
|----------|----------|
| Preciso cadastrar cada dispositivo? | **NÃO** |
| Qualquer celular na rede funciona? | **SIM** |
| Preciso reiniciar a API para cada dispositivo? | **NÃO** |
| O IP pode mudar? | **SIM**, atualize no app se mudar |
| Funciona fora da rede local? | **NÃO** (só em produção) |

---

**Seu IP atual:** `192.168.3.74`
**URL da API:** `http://192.168.3.74:8080/api/v1`

---

*Documento gerado em 27/11/2025 para o projeto SIGA*

