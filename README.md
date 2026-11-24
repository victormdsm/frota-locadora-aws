# Frota Locadora: Spring Boot + Angular na AWS

Sistema simples de locação e controle de frota, usado em aula para o deploy numa EC2.

| Pasta | O que tem |
|-------|-----------|
| [`backend/`](backend/) | API Spring Boot (Java 17), JWT com roles `ADMIN` / `USER`, PostgreSQL em produção |
| [`frontend/`](frontend/) | Angular + MDBootstrap |
| [`configuracoes/`](configuracoes/) | `banco.sql`, config do Apache2 e serviço systemd |
| [`INSTRUCOES.md`](INSTRUCOES.md) | passo a passo do deploy |

## Rodar localmente

```bash
# API em http://localhost:8080 (SQLite, cria admin/admin123 e usuario/usuario123)
cd backend && ./mvnw spring-boot:run

# Front em http://localhost:4200
cd frontend && npm install && npx ng serve
```

Usuários de teste: `admin` / `admin123` (ADMIN) e `usuario` / `usuario123` (USER).
