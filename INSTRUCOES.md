# Deploy na AWS

Você vai usar dois terminais:

- **sua máquina**: aberto na pasta deste repositório (`frota-locadora-aws`)
- **servidor**: conectado na EC2 por SSH (aberto no passo 2)

Cada passo diz em qual dos dois rodar. Troque `3.148.244.143` pelo IP público da sua EC2.

| Porta | Serviço | Security Group |
|-------|---------|----------------|
| 22 | SSH | já vem aberta |
| 80 | Apache2 (Angular) | abrir no passo 9 |
| 8080 | Spring Boot (API) | abrir no passo 9 |
| 5432 | PostgreSQL | fica fechada |

## 1. Preparar a chave `.pem` (sua máquina)

Copie a chave `chave aula.pem` para a pasta deste repositório. O `.gitignore` já impede que ela vá para o GitHub.

O SSH recusa a chave se outros usuários puderem lê-la. Ajuste a permissão:

macOS / Linux:

```bash
chmod 400 "chave aula.pem"
```

Windows (PowerShell):

```powershell
icacls "chave aula.pem" /inheritance:r
icacls "chave aula.pem" /grant:r "$($env:USERNAME):(R)"
```

## 2. Entrar no servidor (sua máquina)

```bash
ssh -i "chave aula.pem" ubuntu@3.148.244.143
```

Na primeira vez, responda `yes`. Este terminal agora é o **servidor**. Deixe ele aberto.

## 3. Instalar PostgreSQL, JDK 17 e Apache2 (servidor)

```bash
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install -y postgresql openjdk-17-jdk-headless apache2
java -version
```

O `java -version` tem que mostrar 17.

## 4. Criar o banco (servidor)

```bash
sudo -u postgres psql
```

Escolha uma senha para o banco e guarde, ela volta no passo 7:

```sql
CREATE ROLE frota LOGIN PASSWORD 'SENHA_DO_BANCO';
CREATE DATABASE frota OWNER frota;
\q
```

Entre como o usuário `frota`:

```bash
psql -h localhost -U frota -d frota
```

Digite a senha, cole o conteúdo de [`configuracoes/banco.sql`](configuracoes/banco.sql) e saia com `\q`.

## 5. Gerar o jar e o build do Angular (sua máquina)

Abra outro terminal na pasta do repositório.

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

## 6. Enviar o jar e o Angular (sua máquina)

```bash
scp -i "chave aula.pem" backend/target/demo-0.0.1-SNAPSHOT.jar ubuntu@3.148.244.143:~/frota-api.jar
scp -r -i "chave aula.pem" frontend/dist/aula4_turmab/browser ubuntu@3.148.244.143:~
```

Os arquivos chegam na pasta do usuário `ubuntu` no servidor (`/home/ubuntu`): o `frota-api.jar` e a pasta `browser`. Os passos 7 e 8 movem cada um para o lugar certo.

## 7. Rodar a API como serviço (servidor)

Crie o usuário que vai rodar a API e mova o jar:

```bash
sudo useradd --system --shell /usr/sbin/nologin frota
sudo mkdir -p /opt/frota
sudo mv ~/frota-api.jar /opt/frota/
openssl rand -base64 48
```

Copie o resultado do `openssl`. Ele é o segredo que assina os tokens de login.

```bash
sudo nano /opt/frota/frota.env
```

Cole, trocando `SENHA_DO_BANCO` pela senha do passo 4 e `RESULTADO_DO_OPENSSL` pelo que você copiou:

```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://localhost:5432/frota
DB_USERNAME=frota
DB_PASSWORD=SENHA_DO_BANCO
JWT_SECRET=RESULTADO_DO_OPENSSL
CORS_ORIGINS=http://3.148.244.143
```

Salve com `Ctrl+O`, `Enter`, `Ctrl+X`. Depois:

```bash
sudo chown -R frota:frota /opt/frota
sudo chmod 600 /opt/frota/frota.env
sudo nano /etc/systemd/system/frota-api.service
```

Cole o conteúdo de [`configuracoes/frota-api.service`](configuracoes/frota-api.service) e salve.

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now frota-api
```

Espere uns 15 segundos e teste:

```bash
curl localhost:8080/health
```

A resposta tem que ser `{"status":"UP"}`. Se não for, veja o log com `sudo journalctl -u frota-api -n 50`.

## 8. Servir o Angular com o Apache2 (servidor)

```bash
sudo mkdir -p /var/www/frota
sudo cp -r ~/browser/* /var/www/frota/
sudo chown -R www-data:www-data /var/www/frota
sudo nano /etc/apache2/sites-available/frota-front.conf
```

Cole o conteúdo de [`configuracoes/frota-front.conf`](configuracoes/frota-front.conf) e salve.

```bash
sudo a2dissite 000-default
sudo a2ensite frota-front
sudo systemctl reload apache2
curl -I localhost
```

A primeira linha da resposta tem que ser `HTTP/1.1 200 OK`.

## 9. Abrir as portas (console da AWS)

Até aqui tudo funciona dentro do servidor, mas o Security Group bloqueia o acesso de fora.

No console: Instâncias, selecione a instância, aba **Segurança**, clique no Security Group, **Editar regras de entrada**. Adicione **TCP 80** e **TCP 8080** com origem `0.0.0.0/0` e salve.

## 10. Testar no navegador

Abra `http://3.148.244.143`.

| Usuário | Senha | Perfil |
|---------|-------|--------|
| `admin` | `admin123` | ADMIN |
| `usuario` | `usuario123` | USER |

## Publicar uma versão nova

Repita os passos 5 e 6 na sua máquina. No servidor:

```bash
sudo mv ~/frota-api.jar /opt/frota/
sudo chown frota:frota /opt/frota/frota-api.jar
sudo systemctl restart frota-api
sudo rm -rf /var/www/frota/*
sudo cp -r ~/browser/* /var/www/frota/
sudo chown -R www-data:www-data /var/www/frota
```

## Se algo der errado

| Sintoma | O que fazer |
|---------|-------------|
| `Permissions ... are too open` | passo 1 |
| SSH dá timeout | confira a porta 22 no Security Group e se o IP da EC2 mudou |
| site não abre de fora | passo 9 |
| login diz que não conectou | `apiUrl` do passo 5 ou porta 8080 fechada |
| erro de CORS no navegador | corrija `CORS_ORIGINS` no `frota.env` e rode `sudo systemctl restart frota-api` |
| API não sobe | `sudo journalctl -u frota-api -n 50` |
