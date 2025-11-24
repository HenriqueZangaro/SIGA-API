# 🛠️ COMANDOS ÚTEIS - SIGA API

Referência rápida de comandos para desenvolvimento, testes e deploy.

---

## 🚀 EXECUTAR O PROJETO

### Via Maven
```bash
# Executar diretamente
mvn spring-boot:run

# Ou com logs detalhados
mvn spring-boot:run -X
```

### Via JAR
```bash
# Compilar
mvn clean package

# Executar
java -jar target/SIGA-API-0.0.1-SNAPSHOT.jar

# Executar com perfil específico
java -jar target/SIGA-API-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Via IDE (IntelliJ/Eclipse)
```
Run → Run 'SigaApiApplication'
```

---

## 🧪 TESTAR ENDPOINTS

### 1. **Verificar se API está rodando**
```bash
curl http://localhost:8080/api/v1/fazendas
```

### 2. **Testar Autenticação**
```bash
# Buscar informações do operador
curl -X GET "http://localhost:8080/api/v1/auth/me" \
  -H "X-User-UID: SEU_UID_AQUI"

# Validar token
curl -X GET "http://localhost:8080/api/v1/auth/validate" \
  -H "X-User-UID: SEU_UID_AQUI"
```

### 3. **Testar Status do Operador**
```bash
curl -X GET "http://localhost:8080/api/v1/pontos/status" \
  -H "X-User-UID: SEU_UID_AQUI"
```

### 4. **Registrar Entrada**
```bash
curl -X POST "http://localhost:8080/api/v1/pontos/registrar" \
  -H "X-User-UID: SEU_UID_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "entrada",
    "localizacao": {
      "latitude": -23.550520,
      "longitude": -46.633308,
      "accuracy": 10.5
    },
    "dispositivo": "Teste Manual",
    "versaoApp": "1.0.0"
  }'
```

### 5. **Registrar Saída**
```bash
curl -X POST "http://localhost:8080/api/v1/pontos/registrar" \
  -H "X-User-UID: SEU_UID_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "saida",
    "localizacao": {
      "latitude": -23.550520,
      "longitude": -46.633308,
      "accuracy": 10.5
    },
    "dispositivo": "Teste Manual",
    "versaoApp": "1.0.0"
  }'
```

### 6. **Buscar Pontos de Hoje**
```bash
curl -X GET "http://localhost:8080/api/v1/pontos/hoje" \
  -H "X-User-UID: SEU_UID_AQUI"
```

### 7. **Buscar Histórico**
```bash
# Sem filtro de data (todos os pontos)
curl -X GET "http://localhost:8080/api/v1/pontos/historico" \
  -H "X-User-UID: SEU_UID_AQUI"

# Com filtro de data
curl -X GET "http://localhost:8080/api/v1/pontos/historico?dataInicio=2024-11-01&dataFim=2024-11-30" \
  -H "X-User-UID: SEU_UID_AQUI"
```

### 8. **Buscar Estatísticas**
```bash
curl -X GET "http://localhost:8080/api/v1/pontos/estatisticas?dataInicio=2024-11-01&dataFim=2024-11-30" \
  -H "X-User-UID: SEU_UID_AQUI"
```

### 9. **Admin: Buscar Pontos de Proprietário**
```bash
curl -X GET "http://localhost:8080/api/v1/pontos/admin/proprietario/prop_001" \
  -H "X-User-UID: ADMIN_UID"
```

### 10. **Admin: Atualizar Ponto**
```bash
curl -X PUT "http://localhost:8080/api/v1/pontos/admin/ponto_123" \
  -H "X-User-UID: ADMIN_UID" \
  -H "Content-Type: application/json" \
  -d '{
    "observacao": "Atualizado pelo admin"
  }'
```

### 11. **Admin: Deletar Ponto**
```bash
curl -X DELETE "http://localhost:8080/api/v1/pontos/admin/ponto_123" \
  -H "X-User-UID: ADMIN_UID"
```

---

## 📦 MAVEN

### Compilar
```bash
# Limpar e compilar
mvn clean compile

# Limpar, compilar e testar
mvn clean test

# Limpar e criar JAR
mvn clean package

# Pular testes
mvn clean package -DskipTests
```

### Dependências
```bash
# Baixar dependências
mvn dependency:resolve

# Verificar dependências desatualizadas
mvn versions:display-dependency-updates

# Árvore de dependências
mvn dependency:tree
```

### Limpar
```bash
# Limpar target/
mvn clean

# Limpar tudo incluindo cache
mvn clean -Dmaven.repo.local=.m2/repository
```

---

## 🔥 FIREBASE

### Firestore
```bash
# Não há CLI direto, usar Firebase Console:
# https://console.firebase.google.com

# Ou usar SDK Admin em scripts Node.js
```

### Firebase CLI (para regras)
```bash
# Instalar
npm install -g firebase-tools

# Login
firebase login

# Inicializar projeto
firebase init

# Deploy de regras
firebase deploy --only firestore:rules

# Testar regras localmente
firebase emulators:start --only firestore
```

---

## 🐛 DEBUG

### Ver Logs em Tempo Real
```bash
# Logs completos
tail -f logs/siga-api.log

# Apenas erros
tail -f logs/siga-api.log | grep ERROR

# Apenas pontos
tail -f logs/siga-api.log | grep "ponto"
```

### Verificar Porta em Uso
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### Matar Processo na Porta 8080
```bash
# Windows
taskkill /PID <PID> /F

# Linux/Mac
kill -9 <PID>
```

---

## 📊 MONITORAMENTO

### Health Check
```bash
# Verificar se está rodando
curl http://localhost:8080/actuator/health

# Verificar métricas (se habilitado)
curl http://localhost:8080/actuator/metrics
```

### Testar Performance
```bash
# Apache Bench - 100 requisições, 10 simultâneas
ab -n 100 -c 10 http://localhost:8080/api/v1/fazendas

# Ou com wrk
wrk -t4 -c100 -d30s http://localhost:8080/api/v1/fazendas
```

---

## 🔐 VARIÁVEIS DE AMBIENTE

### Definir UID para Testes
```bash
# Windows (PowerShell)
$env:TEST_UID = "abc123def456"

# Linux/Mac (Bash)
export TEST_UID="abc123def456"

# Usar em curl
curl -H "X-User-UID: $TEST_UID" http://localhost:8080/api/v1/pontos/status
```

### Porta Customizada
```bash
# Executar em porta diferente
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090

# Ou
java -jar target/SIGA-API-0.0.1-SNAPSHOT.jar --server.port=9090
```

---

## 📝 SCRIPTS ÚTEIS

### Criar Operador Completo (Bash)
```bash
#!/bin/bash
# criar-operador.sh

OPERADOR_ID="oper_$(date +%s)"
USER_UID="$1"  # Passar UID como argumento

echo "Criando operador com ID: $OPERADOR_ID"
echo "UID: $USER_UID"

# Criar operador no Firestore (usar Firebase Admin SDK ou manualmente)
# Criar UserProfile no Firestore
# Vincular userId no operador

echo "Operador criado com sucesso!"
echo "Email: operador@fazenda.com"
echo "Senha: senha123"
echo "UID: $USER_UID"
```

### Testar Fluxo Completo (Bash)
```bash
#!/bin/bash
# testar-fluxo.sh

UID="abc123def456"
BASE_URL="http://localhost:8080/api/v1"

echo "1. Testando autenticação..."
curl -s -X GET "$BASE_URL/auth/me" -H "X-User-UID: $UID" | jq

echo "\n2. Testando status..."
curl -s -X GET "$BASE_URL/pontos/status" -H "X-User-UID: $UID" | jq

echo "\n3. Registrando entrada..."
curl -s -X POST "$BASE_URL/pontos/registrar" \
  -H "X-User-UID: $UID" \
  -H "Content-Type: application/json" \
  -d '{"tipo":"entrada","dispositivo":"Teste"}' | jq

echo "\n4. Aguardando 2 segundos..."
sleep 2

echo "\n5. Registrando saída..."
curl -s -X POST "$BASE_URL/pontos/registrar" \
  -H "X-User-UID: $UID" \
  -H "Content-Type: application/json" \
  -d '{"tipo":"saida","dispositivo":"Teste"}' | jq

echo "\n6. Verificando pontos de hoje..."
curl -s -X GET "$BASE_URL/pontos/hoje" -H "X-User-UID: $UID" | jq

echo "\nTeste completo!"
```

### Limpar Pontos de Teste (Firestore)
```javascript
// limpar-pontos.js
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();

async function limparPontos() {
  const snapshot = await db.collection('pontos')
    .where('dispositivo', '==', 'Teste')
    .get();
  
  const batch = db.batch();
  snapshot.docs.forEach(doc => {
    batch.delete(doc.ref);
  });
  
  await batch.commit();
  console.log(`${snapshot.size} pontos de teste deletados`);
}

limparPontos();
```

---

## 🚢 DEPLOY

### Build para Produção
```bash
# Limpar e criar JAR otimizado
mvn clean package -DskipTests -Dspring.profiles.active=prod

# Verificar JAR criado
ls -lh target/*.jar
```

### Docker (se configurado)
```bash
# Build da imagem
docker build -t siga-api:latest .

# Executar container
docker run -p 8080:8080 siga-api:latest

# Ver logs
docker logs -f <container-id>
```

### Deploy em Servidor
```bash
# Copiar JAR para servidor
scp target/SIGA-API-0.0.1-SNAPSHOT.jar usuario@servidor:/opt/siga-api/

# SSH no servidor
ssh usuario@servidor

# Executar em background
nohup java -jar /opt/siga-api/SIGA-API-0.0.1-SNAPSHOT.jar > siga-api.log 2>&1 &

# Verificar processo
ps aux | grep java

# Ver logs
tail -f siga-api.log
```

---

## 🔧 TROUBLESHOOTING

### API não inicia
```bash
# Verificar se porta 8080 está livre
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Verificar Firebase credentials
ls -la src/main/resources/firebase-credentials.json

# Verificar logs
mvn spring-boot:run > logs.txt 2>&1
cat logs.txt | grep ERROR
```

### Erro de conexão Firebase
```bash
# Verificar credenciais
cat src/main/resources/firebase-credentials.json | jq

# Testar conectividade
curl https://firestore.googleapis.com

# Regenerar credenciais no Firebase Console
```

### CORS Error
```bash
# Verificar se @CrossOrigin está nos controllers
grep -r "CrossOrigin" src/main/java/

# Ou adicionar configuração global
```

---

## 📚 REFERÊNCIAS RÁPIDAS

### Portas
- **API:** 8080
- **Firebase Emulator:** 8081 (se configurado)

### URLs
- **API Local:** http://localhost:8080
- **Firebase Console:** https://console.firebase.google.com
- **Health Check:** http://localhost:8080/actuator/health

### Headers
- **Autenticação:** `X-User-UID: <firebase-uid>`
- **Content-Type:** `application/json`

### Status HTTP
- **200:** OK
- **400:** Bad Request (validação falhou)
- **401:** Unauthorized (token inválido)
- **403:** Forbidden (sem permissão)
- **404:** Not Found
- **500:** Internal Server Error

---

## 💡 DICAS

### 1. **Usar Postman/Insomnia**
Importe a collection com todos os endpoints prontos:
```json
{
  "name": "SIGA API - Pontos",
  "requests": [
    {
      "name": "Status",
      "method": "GET",
      "url": "http://localhost:8080/api/v1/pontos/status",
      "headers": {
        "X-User-UID": "{{UID}}"
      }
    }
  ]
}
```

### 2. **Criar Alias**
```bash
# .bashrc ou .zshrc
alias siga-start='cd /caminho/siga-api && mvn spring-boot:run'
alias siga-test='curl http://localhost:8080/api/v1/fazendas'
alias siga-logs='tail -f /caminho/siga-api/logs/siga-api.log'
```

### 3. **Hot Reload (Spring DevTools)**
Adicione no `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## 🎯 COMANDOS MAIS USADOS

```bash
# Iniciar API
mvn spring-boot:run

# Testar se está rodando
curl http://localhost:8080/api/v1/fazendas

# Testar autenticação
curl -H "X-User-UID: SEU_UID" http://localhost:8080/api/v1/auth/me

# Registrar entrada
curl -X POST -H "X-User-UID: SEU_UID" -H "Content-Type: application/json" \
  -d '{"tipo":"entrada"}' http://localhost:8080/api/v1/pontos/registrar

# Ver status
curl -H "X-User-UID: SEU_UID" http://localhost:8080/api/v1/pontos/status

# Ver logs
tail -f logs/siga-api.log
```

---

**🛠️ Comandos prontos para copiar e colar!**

*Salve este arquivo para referência rápida durante o desenvolvimento.*

