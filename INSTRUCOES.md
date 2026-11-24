# Instruções: deploy da Frota Locadora na AWS

Passo a passo para colocar no ar, numa EC2 Ubuntu, o **backend Spring Boot (JDK 17)**, o **PostgreSQL** e o **frontend Angular servido pelo Apache2**.
Roteiro executado e validado em 24/11/2026 numa EC2 t3.small, Ubuntu 26.04 (us-east-2).

```
Navegador ──80──▶ Apache2 (Angular, /var/www/frota)
    │
    └────8080──▶ Spring Boot (serviço frota-api, JDK 17) ──5432 (só localhost)──▶ PostgreSQL
```

| Porta | Serviço     | Abrir no Security Group? | Por quê |
|-------|-------------|--------------------------|---------|
| 22    | SSH         | Sim (já vem aberta)      | acesso ao servidor |
| 80    | Apache2     | **Sim, 0.0.0.0/0**       | frontend Angular |
| 8080  | Spring Boot | **Sim, 0.0.0.0/0**       | API que o Angular chama do navegador |
| 5432  | PostgreSQL  | **Não**                  | só a API (na mesma máquina) acessa o banco |

Arquivos de configuração (pasta `configuracoes/`):

| Arquivo | Vai para | Para quê |
|---------|----------|----------|
| `banco.sql`          | rodado no `psql`                          | cria as tabelas e os dados iniciais |
| `frota-api.service`  | `/etc/systemd/system/`                    | roda o jar como serviço do Linux |
| `frota-front.conf`   | `/etc/apache2/sites-available/`           | Apache serve o Angular |
| `frota.env.exemplo`  | modelo do `/opt/frota/frota.env`          | senha do banco, segredo JWT, CORS |

> Em todos os comandos, troque **`3.148.244.143`** pelo IP público da **sua** EC2
> e **`chave aula.pem`** pelo nome da sua chave.

---

## 0. Pré-requisitos na sua máquina

- Git, **JDK 17**, Node.js 20+ (para o build do Angular)
- A chave `.pem` baixada ao criar a EC2

```bash
git clone https://github.com/victormdsm/frota-locadora-aws.git
cd frota-locadora-aws
```

## 1. Configurar a chave `.pem`

O SSH **recusa** chave com permissão aberta (`Permissions 0644 ... are too open`).

**macOS / Linux**

```bash
mv ~/Downloads/"chave aula.pem" ~/.ssh/
chmod 400 ~/.ssh/"chave aula.pem"

# testar o acesso (na 1ª vez responda "yes")
ssh -i ~/.ssh/"chave aula.pem" ubuntu@3.148.244.143
exit
```

**Windows (PowerShell)**

```powershell
Move-Item "$env:USERPROFILE\Downloads\chave aula.pem" "$env:USERPROFILE\.ssh\"
$k = "$env:USERPROFILE\.ssh\chave aula.pem"
icacls $k /inheritance:r
icacls $k /grant:r "$($env:USERNAME):(R)"

ssh -i $k ubuntu@3.148.244.143
```

**Opcional: atalho no `~/.ssh/config`** (aí basta `ssh aula` / `scp arquivo aula:~/`)

```
Host aula
    HostName 3.148.244.143
    User ubuntu
    IdentityFile ~/.ssh/chave aula.pem
```

> O usuário da EC2 Ubuntu é sempre **`ubuntu`**. A chave **nunca** vai para o git (o `.gitignore` já bloqueia `*.pem`).

## 2. Gerar os artefatos (na sua máquina)

```bash
# Backend -> backend/target/demo-0.0.1-SNAPSHOT.jar
cd backend
./mvnw clean package -DskipTests          # Windows: mvnw.cmd clean package -DskipTests
cd ..

# Frontend: antes, edite frontend/src/environments/environment.ts
#   apiUrl: 'http://3.148.244.143:8080'
cd frontend
npm install
npx ng build                              # -> frontend/dist/aula4_turmab/browser
cd ..
```

## 3. Enviar os arquivos para o servidor (na sua máquina)

Rode **da raiz do repositório**:

```bash
KEY=~/.ssh/"chave aula.pem"
SERVER=ubuntu@3.148.244.143

# cria a pasta de trabalho no servidor
ssh -i "$KEY" $SERVER 'mkdir -p ~/deploy/front'

# jar do backend
scp -i "$KEY" backend/target/demo-0.0.1-SNAPSHOT.jar $SERVER:~/deploy/frota-api.jar

# arquivos de configuração
scp -i "$KEY" configuracoes/banco.sql configuracoes/frota-api.service \
    configuracoes/frota-front.conf configuracoes/frota.env.exemplo $SERVER:~/deploy/

# build do Angular
scp -r -i "$KEY" frontend/dist/aula4_turmab/browser/* $SERVER:~/deploy/front/

# conferir
ssh -i "$KEY" $SERVER 'ls -lh ~/deploy ~/deploy/front'
```

> No Windows (PowerShell) troque as variáveis por
> `$KEY="$env:USERPROFILE\.ssh\chave aula.pem"` e `$SERVER="ubuntu@3.148.244.143"`, e use `"$KEY"` / `$SERVER` igual.

Agora entre no servidor:

```bash
ssh -i ~/.ssh/"chave aula.pem" ubuntu@3.148.244.143
```

**Daqui em diante, tudo é dentro da EC2.**

> Se der `Could not get lock /var/lib/dpkg/lock-frontend`, há outro `apt` rodando
> (ex: um `apt-get upgrade` esperando o "Y"). Termine ele antes de seguir.

## 4. Atualizar e instalar o PostgreSQL

```bash
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install -y postgresql

psql --version                       # PostgreSQL 18.x
systemctl status postgresql          # active (q para sair)
sudo ss -ltnp | grep 5432            # 127.0.0.1:5432 -> só local, como queremos
```

## 5. Criar usuário, banco e tabelas

```bash
sudo -u postgres psql
```

Dentro do `psql` (escolha uma senha e **anote**, ela vai no `frota.env`):

```sql
CREATE ROLE frota LOGIN PASSWORD 'SENHA_DO_BANCO';
CREATE DATABASE frota OWNER frota;
\q
```

Rodar o script **como o usuário frota** (assim ele vira dono das tabelas):

```bash
psql -h localhost -U frota -d frota -f ~/deploy/banco.sql
# pede a SENHA_DO_BANCO
```

Ou abra `psql -h localhost -U frota -d frota` e **cole o conteúdo de `configuracoes/banco.sql`**.
Saída esperada no final:

```
  tabela  | count
----------+-------
 usuarios |     2
 marcas   |     5
 carros   |     7
```

Conferir: `psql -h localhost -U frota -d frota -c '\dt'` mostra `carro, locacao, marca, saida, usuario`.

## 6. Instalar JDK 17 e Apache2

```bash
sudo apt-get install -y openjdk-17-jdk-headless apache2

java -version                 # openjdk version "17.0.x"
apache2 -v                    # Apache/2.4.x
systemctl status apache2      # active
```

> Se o `java -version` mostrar outra versão, veja `update-alternatives --list java`
> e remova a sobrando (ex: `sudo apt-get purge openjdk-21-jdk-headless openjdk-21-jre-headless`).

## 7. Rodar o jar como serviço do Linux (systemd)

```bash
# usuário de sistema só para rodar a API (sem login)
sudo useradd --system --home /opt/frota --shell /usr/sbin/nologin frota
sudo mkdir -p /opt/frota
sudo cp ~/deploy/frota-api.jar /opt/frota/frota-api.jar

# arquivo de variáveis a partir do modelo
sudo cp ~/deploy/frota.env.exemplo /opt/frota/frota.env
openssl rand -base64 48               # copie o resultado para o JWT_SECRET
sudo nano /opt/frota/frota.env        # Ctrl+O salva, Ctrl+X sai
```

Conteúdo final de `/opt/frota/frota.env`:

```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://localhost:5432/frota
DB_USERNAME=frota
DB_PASSWORD=SENHA_DO_BANCO
JWT_SECRET=COLE_AQUI_O_RESULTADO_DO_OPENSSL
CORS_ORIGINS=http://3.148.244.143
```

```bash
sudo chown -R frota:frota /opt/frota
sudo chmod 600 /opt/frota/frota.env          # só o dono lê as senhas

sudo cp ~/deploy/frota-api.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now frota-api        # sobe agora e a cada boot

systemctl status frota-api
curl localhost:8080/health                   # {"status":"UP"}  (pode levar ~15s)
curl -i localhost:8080/carros                # 401 (sem token) -> segurança ok
```

O profile `prod` usa `ddl-auto=validate`: o Spring **não cria** tabelas, só confere se as do
`banco.sql` batem com as entidades. Se algo não bater, a API não sobe e o erro aparece no log.

Comandos do dia a dia:

```bash
sudo journalctl -u frota-api -f        # log ao vivo (Ctrl+C sai)
sudo systemctl restart frota-api       # depois de trocar o jar ou o frota.env
sudo systemctl stop frota-api
```

## 8. Configurar o Apache2 para servir o Angular

```bash
sudo mkdir -p /var/www/frota
sudo cp -r ~/deploy/front/* /var/www/frota/
sudo chown -R www-data:www-data /var/www/frota

sudo cp ~/deploy/frota-front.conf /etc/apache2/sites-available/
sudo a2dissite 000-default
sudo a2ensite frota-front
sudo apache2ctl configtest              # Syntax OK
sudo systemctl reload apache2

curl -I localhost/                      # 200
curl -I localhost/sistema/carros        # 200 (FallbackResource -> index.html)
```

O `FallbackResource /index.html` do `frota-front.conf` é o que permite dar F5 numa rota do
Angular (ex: `/sistema/carros`) sem tomar 404 do Apache.

## 9. Liberar as portas no Security Group (console AWS, no navegador)

Antes disso, de fora da AWS, `http://3.148.244.143` dá **timeout**: quem bloqueia é o
Security Group (o `ufw` do Ubuntu vem inativo).

1. EC2 → **Instâncias** → selecione a instância → aba **Segurança** → clique no grupo (ex: `launch-wizard-2`)
2. **Regras de entrada** → **Editar regras de entrada**
3. **Adicionar regra**: TCP personalizado · porta **80** · origem **0.0.0.0/0**
4. **Adicionar regra**: TCP personalizado · porta **8080** · origem **0.0.0.0/0**
5. **Salvar regras**

**Não** abra a 5432.

## 10. Testar

Da sua máquina:

```bash
curl -I http://3.148.244.143/                 # 200 (Apache)
curl http://3.148.244.143:8080/health         # {"status":"UP"}
curl -i http://3.148.244.143:8080/carros      # 401
curl -X POST http://3.148.244.143:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","senha":"admin123"}'   # devolve o token
```

No navegador: **http://3.148.244.143**

| Usuário   | Senha        | O que vê |
|-----------|--------------|----------|
| `admin`   | `admin123`   | Painel, Frota (CRUD), todas as Locações, Marcas, Usuários |
| `usuario` | `usuario123` | Frota (só locar) e Minhas locações |

> Fora da sala de aula, troque essas senhas (tela Usuários, logado como admin).

## Atualizar uma nova versão

```bash
# na sua máquina (raiz do repo), depois de gerar jar/build de novo
scp -i "$KEY" backend/target/demo-0.0.1-SNAPSHOT.jar $SERVER:~/deploy/frota-api.jar
scp -r -i "$KEY" frontend/dist/aula4_turmab/browser/* $SERVER:~/deploy/front/

# na EC2
sudo cp ~/deploy/frota-api.jar /opt/frota/frota-api.jar && sudo chown frota:frota /opt/frota/frota-api.jar
sudo systemctl restart frota-api
sudo rm -rf /var/www/frota/* && sudo cp -r ~/deploy/front/* /var/www/frota/ && sudo chown -R www-data:www-data /var/www/frota
```

## Problemas comuns

| Sintoma | Causa / solução |
|---------|-----------------|
| `Permissions 0644 for 'chave aula.pem' are too open` | `chmod 400` (ou `icacls` no Windows), passo 1 |
| `Permission denied (publickey)` | usuário errado (é `ubuntu`) ou chave errada |
| `Could not get lock /var/lib/dpkg/lock-frontend` | outro apt rodando, espere/termine ele |
| site não abre de fora (timeout) | porta não liberada no Security Group |
| tela abre mas login diz "Não foi possível conectar" | 8080 fechada ou `apiUrl` errado no `environment.ts` |
| erro de CORS no console do navegador | `CORS_ORIGINS` no `frota.env` diferente do endereço do site → corrija e `restart` |
| `frota-api` não sobe | `sudo journalctl -u frota-api -n 50`: senha do banco errada ou `Schema-validation` (rode o `banco.sql`) |
| F5 numa rota dá 404 | faltou o `FallbackResource` / site não habilitado com `a2ensite` |
| IP mudou depois de parar/iniciar a EC2 | o IP público muda: refaça o build do front e o `CORS_ORIGINS` (ou use um Elastic IP) |
