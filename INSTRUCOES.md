# Deploy na AWS

Troque `3.148.244.143` pelo IP público da sua EC2.

Primeiro o servidor fica pronto (banco, Java, Apache). Depois, na sua máquina, você gera o build e manda o jar e o Angular direto para as pastas finais.

| Porta | Serviço | Security Group |
|-------|---------|----------------|
| 22 | SSH | já vem aberta |
| 80 | Apache2 (Angular) | abrir |
| 8080 | Spring Boot (API) | abrir |
| 5432 | PostgreSQL | não abrir |

## Parte 1: servidor

Entre na EC2 pelo console da AWS: Instâncias, selecione a instância, **Conectar**, aba **EC2 Instance Connect**, **Conectar**.

### 1. Instalar PostgreSQL, JDK 17 e Apache2

```bash
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install -y postgresql openjdk-17-jdk-headless apache2
java -version
```

O `java -version` tem que mostrar 17.

### 2. Banco de dados

```bash
sudo -u postgres psql
```

```sql
CREATE ROLE frota LOGIN PASSWORD 'SENHA_DO_BANCO';
CREATE DATABASE frota OWNER frota;
\q
```

```bash
psql -h localhost -U frota -d frota
```

Digite a senha, cole o conteúdo de [`configuracoes/banco.sql`](configuracoes/banco.sql) e saia com `\q`.

### 3. API como serviço

```bash
sudo useradd --system --shell /usr/sbin/nologin frota
sudo mkdir -p /opt/frota
sudo chown ubuntu:ubuntu /opt/frota
openssl rand -base64 48
sudo nano /opt/frota/frota.env
```

Cole, trocando a senha do banco e o segredo pelo resultado do `openssl`:

```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://localhost:5432/frota
DB_USERNAME=frota
DB_PASSWORD=SENHA_DO_BANCO
JWT_SECRET=RESULTADO_DO_OPENSSL
CORS_ORIGINS=http://3.148.244.143
```

Salve com `Ctrl+O`, `Enter`, `Ctrl+X`.

```bash
sudo chown frota:frota /opt/frota/frota.env
sudo chmod 600 /opt/frota/frota.env
sudo nano /etc/systemd/system/frota-api.service
```

Cole o conteúdo de [`configuracoes/frota-api.service`](configuracoes/frota-api.service) e salve.

```bash
sudo systemctl daemon-reload
sudo systemctl enable frota-api
```

O serviço só vai subir na Parte 3, quando o jar chegar.

### 4. Apache2

```bash
sudo mkdir -p /var/www/frota
sudo chown ubuntu:ubuntu /var/www/frota
sudo nano /etc/apache2/sites-available/frota-front.conf
```

Cole o conteúdo de [`configuracoes/frota-front.conf`](configuracoes/frota-front.conf) e salve.

```bash
sudo a2dissite 000-default
sudo a2ensite frota-front
sudo systemctl reload apache2
```

### 5. Portas

No console da AWS: Instâncias, selecione a instância, aba **Segurança**, clique no Security Group, **Editar regras de entrada**. Adicione **TCP 80** e **TCP 8080** com origem `0.0.0.0/0` e salve.

## Parte 2: sua máquina

### 6. Build

Em `frontend/src/environments/environment.ts`, coloque o IP da EC2:

```ts
apiUrl: 'http://3.148.244.143:8080'
```

```bash
cd backend
./mvnw clean package -DskipTests
cd ../frontend
npm install
npx ng build
cd ..
```

No Windows, a linha do Maven é `.\mvnw.cmd clean package -DskipTests`.

### 7. Chave `.pem`

O SSH recusa a chave se outras pessoas puderem lê-la. Rode na pasta onde está a chave.

macOS / Linux:

```bash
chmod 400 "chave aula.pem"
```

Windows (PowerShell):

```powershell
icacls "chave aula.pem" /inheritance:r
icacls "chave aula.pem" /grant:r "$($env:USERNAME):(R)"
```

Teste (na primeira vez, responda `yes`):

```bash
ssh -i "chave aula.pem" ubuntu@3.148.244.143
exit
```

### 8. Enviar para o servidor

Na raiz do repositório. Troque `"chave aula.pem"` pelo caminho da sua chave, se ela estiver em outra pasta.

```bash
scp -i "chave aula.pem" backend/target/demo-0.0.1-SNAPSHOT.jar ubuntu@3.148.244.143:/opt/frota/frota-api.jar
scp -r -i "chave aula.pem" frontend/dist/aula4_turmab/browser ubuntu@3.148.244.143:/var/www/frota
```

Os dois comandos são iguais no macOS, no Linux e no PowerShell.

## Parte 3: subir e testar

```bash
ssh -i "chave aula.pem" ubuntu@3.148.244.143
sudo systemctl restart frota-api
curl localhost:8080/health
```

A API leva uns 15 segundos para responder `{"status":"UP"}`.

Abra `http://3.148.244.143` no navegador.

| Usuário | Senha | Perfil |
|---------|-------|--------|
| `admin` | `admin123` | ADMIN |
| `usuario` | `usuario123` | USER |

Para publicar uma versão nova, repita os passos 6 e 8 e rode `sudo systemctl restart frota-api`.

## Se algo der errado

| Sintoma | O que fazer |
|---------|-------------|
| `Permissions ... are too open` | passo 7 |
| SSH dá timeout | confira a porta 22 no Security Group e se o IP mudou |
| site não abre | portas 80 e 8080 (passo 5) |
| login diz que não conectou | `apiUrl` do passo 6 ou porta 8080 fechada |
| erro de CORS no navegador | `CORS_ORIGINS` no `frota.env`, depois `sudo systemctl restart frota-api` |
| API não sobe | `sudo journalctl -u frota-api -n 50` |
