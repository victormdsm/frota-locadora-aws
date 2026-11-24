# backend-aula-4o-periodo: Frota Locadora (API)

API REST de **locação e controle de frota** com Spring Boot, Spring Security (JWT) e SQLite.
Rotas autenticadas por token e autorizadas por **perfil (role)**: `ADMIN` e `USER`.

## Stack

- Java 17 · Spring Boot 4.1.1 (Web MVC, Data JPA, Security, OAuth2 Resource Server)
- JWT assinado com HMAC-SHA256 · senhas com BCrypt
- SQLite local (`./data/frota.db`, criado sozinho) · PostgreSQL em produção (profile `prod`)

## Rodando local

```bash
./mvnw spring-boot:run
```

Sobe em `http://localhost:8080`. Na primeira subida o `DatabaseSeeder` cria:

| Usuário   | Senha        | Perfil |
|-----------|--------------|--------|
| `admin`   | `admin123`   | ADMIN  |
| `usuario` | `usuario123` | USER   |

e 5 marcas + 7 carros. (Para recomeçar do zero: pare a API e apague `data/frota.db`.)

## Como a autenticação funciona

1. `POST /auth/login` com `{"username":"admin","senha":"admin123"}` → devolve `{ token, role, nome, ... }`
2. Nas próximas requisições: header `Authorization: Bearer <token>`
3. O token carrega a claim `roles: ["ADMIN"]`, que vira a authority `ROLE_ADMIN`.
   As regras ficam em `security/SecurityConfig.java` e em `@PreAuthorize` nos controllers.

Respostas: **401** sem token / token inválido ou expirado · **403** token válido mas sem permissão.

## Endpoints

| Método | Rota                          | Quem pode          |
|--------|-------------------------------|--------------------|
| POST   | `/auth/login`                 | público            |
| GET    | `/health`                     | público            |
| GET    | `/auth/me`                    | logado             |
| GET    | `/carros`, `/carros/{id}`     | logado             |
| GET    | `/carros/disponiveis`         | logado             |
| POST/PUT/DELETE | `/carros/**`         | ADMIN              |
| GET    | `/marcas`, `/marcas/{id}`     | logado             |
| POST/PUT/DELETE | `/marcas/**`         | ADMIN              |
| GET    | `/locacoes`                   | ADMIN (todas)      |
| GET    | `/locacoes/minhas`            | logado (as suas)   |
| GET    | `/locacoes/{id}`              | dono ou ADMIN      |
| POST   | `/locacoes`                   | logado             |
| PUT    | `/locacoes/{id}/devolver`     | dono ou ADMIN      |
| PUT    | `/locacoes/{id}/cancelar`     | ADMIN              |
| *      | `/usuarios/**`                | ADMIN              |
| *      | `/api/calculo/**`             | público (aula de testes) |

Corpo do `POST /locacoes` (`dataInicio` é opcional, padrão hoje; `usuarioId` só vale para ADMIN):

```json
{ "carroId": 1, "dataInicio": "2026-11-24", "dataFimPrevista": "2026-11-27" }
```

Regras: só loca carro `DISPONIVEL`; valor = diárias × valor da diária (mínimo 1);
na devolução o valor é recalculado se passou da data prevista; carro com locação não pode ser excluído.

Erros saem sempre como `{ "status": 400, "mensagem": "...", "timestamp": "..." }`.

## Variáveis de ambiente (produção / AWS)

| Variável         | Padrão                               | Para quê |
|------------------|--------------------------------------|----------|
| `JWT_SECRET`     | segredo de desenvolvimento           | **obrigatório trocar**, mín. 32 caracteres |
| `CORS_ORIGINS`   | `http://localhost:4200`              | URL(s) do front, separadas por vírgula |
| `DB_URL`         | `jdbc:sqlite:./data/frota.db`        | caminho do banco |
| `PORT`           | `8080`                               | porta HTTP |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin123` | admin criado no seed |
| `USER_USERNAME` / `USER_PASSWORD`   | `usuario` / `usuario123` | usuário criado no seed |
| `JWT_EXPIRACAO_MINUTOS` | `120`                         | validade do token |

## Deploy na AWS (EC2 Ubuntu + PostgreSQL + Apache2)

Passo a passo completo, testado: **[../INSTRUCOES.md](../INSTRUCOES.md)**.
Script do banco para copiar e colar: [../configuracoes/banco.sql](../configuracoes/banco.sql).

Em produção a API roda com `SPRING_PROFILES_ACTIVE=prod` (PostgreSQL, `ddl-auto=validate`),
variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e `CORS_ORIGINS`.
