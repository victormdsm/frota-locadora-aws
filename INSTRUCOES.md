# Deploy na AWS (EC2 Ubuntu)

Troque `3.148.244.143` pelo IP da sua EC2.

| Porta | Serviço | Abrir no Security Group |
|-------|---------|-------------------------|
| 22    | SSH     | sim (já vem) |
| 80    | Apache2 (Angular) | sim |
| 8080  | Spring Boot | sim |
| 5432  | PostgreSQL | **não** |

## 1. Chave `.pem`

macOS / Linux:
```bash
chmod 400 "chave aula.pem"
ssh -i "chave aula.pem" ubuntu@3.148.244.143
```

Windows (PowerShell):
```powershell
icacls "chave aula.pem" /inheritance:r
icacls "chave aula.pem" /grant:r "$($env:USERNAME):(R)"
ssh -i "chave aula.pem" ubuntu@3.148.244.143
```

## 2. Build (na sua máquina)

Em `frontend/src/environments/environment.ts`: `apiUrl: 'http://3.148.244.143:8080'`

```bash
cd backend && ./mvnw clean package -DskipTests && cd ..     # Windows: .\mvnw.cmd
cd frontend && npm install && npx ng build && cd ..
```

## 3. Enviar os arquivos (na raiz do repositório)

```bash
ssh -i "chave aula.pem" ubuntu@3.148.244.143 "mkdir -p ~/deploy"
scp -i "chave aula.pem" backend/target/demo-0.0.1-SNAPSHOT.jar ubuntu@3.148.244.143:~/deploy/frota-api.jar
scp -i "chave aula.pem" configuracoes/frota-api.service configuracoes/frota-front.conf ubuntu@3.148.244.143:~/deploy/
scp -r -i "chave aula.pem" frontend/dist/aula4_turmab/browser ubuntu@3.148.244.143:~/deploy/front
```

Os mesmos comandos funcionam no PowerShell (no Windows pode usar `\` nos caminhos locais).

**Daqui pra baixo, tudo dentro da EC2** (`ssh -i "chave aula.pem" ubuntu@3.148.244.143`).

## 4. PostgreSQL

```bash
sudo apt-get update && sudo apt-get upgrade -y
sudo apt-get install -y postgresql
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

Cole o conteúdo de [`configuracoes/banco.sql`](configuracoes/banco.sql) e saia com `\q`.

## 5. JDK 17 e Apache2

```bash
sudo apt-get install -y openjdk-17-jdk-headless apache2
java -version     # 17
```

## 6. API como serviço

```bash
sudo useradd --system --home /opt/frota --shell /usr/sbin/nologin frota
sudo mkdir -p /opt/frota
sudo cp ~/deploy/frota-api.jar /opt/frota/
openssl rand -base64 48          # segredo do JWT
sudo nano /opt/frota/frota.env
```

Cole (com a senha do banco e o segredo gerado):
```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://localhost:5432/frota
DB_USERNAME=frota
DB_PASSWORD=SENHA_DO_BANCO
JWT_SECRET=SEGREDO_DO_OPENSSL
CORS_ORIGINS=http://3.148.244.143
```

```bash
sudo chown -R frota:frota /opt/frota
sudo chmod 600 /opt/frota/frota.env
sudo cp ~/deploy/frota-api.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now frota-api
curl localhost:8080/health       # {"status":"UP"}
```

Log: `sudo journalctl -u frota-api -f`

## 7. Apache2 servindo o Angular

```bash
sudo mkdir -p /var/www/frota
sudo cp -r ~/deploy/front/* /var/www/frota/
sudo chown -R www-data:www-data /var/www/frota
sudo cp ~/deploy/frota-front.conf /etc/apache2/sites-available/
sudo a2dissite 000-default
sudo a2ensite frota-front
sudo systemctl reload apache2
```

## 8. Portas (console AWS)

EC2 → Instância → Segurança → Security Group → Editar regras de entrada →
adicionar **TCP 80** e **TCP 8080**, origem `0.0.0.0/0` → Salvar.

## 9. Testar

Abra `http://3.148.244.143`

| Usuário | Senha |
|---------|-------|
| `admin` | `admin123` |
| `usuario` | `usuario123` |

## Problemas comuns

| Sintoma | Solução |
|---------|---------|
| `Permissions ... too open` | passo 1 (`chmod 400` / `icacls`) |
| SSH dá timeout | porta 22 no Security Group / IP mudou |
| site não abre | portas 80/8080 no Security Group |
| erro de CORS | `CORS_ORIGINS` no `frota.env` + `sudo systemctl restart frota-api` |
| API não sobe | `sudo journalctl -u frota-api -n 50` |
