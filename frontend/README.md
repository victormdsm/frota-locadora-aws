# Frota Locadora: front-end (Angular 19 + MDBootstrap)

Front do sistema de locação/controle de frota. Consome a API `backend-aula-4o-periodo` com login JWT.

## Rodando local

```bash
npm install
npm start          # http://localhost:4200  (API esperada em http://localhost:8080)
```

Usuários de teste (criados pelo backend): `admin / admin123` (ADMIN) e `usuario / usuario123` (USER).

## O que cada perfil vê

| Tela                         | ADMIN | USER |
|------------------------------|:-----:|:----:|
| Painel (indicadores)         | ✔     |      |
| Frota (listar / locar)       | ✔     | ✔ (só locar) |
| Frota (criar/editar/excluir) | ✔     |      |
| Locações                     | todas, devolver e cancelar | as suas, devolver |
| Marcas e Usuários            | ✔     |      |

## Peças de segurança no front

- `services/auth.service.ts`: faz login, guarda o token no `localStorage`, lê a role e a expiração do JWT
- `interceptors/auth.interceptor.ts`: envia `Authorization: Bearer <token>`; em 401 volta pro login
- `guards/auth.guard.ts`: `authGuard` (precisa estar logado) e `adminGuard` (precisa ser ADMIN)

> O guard só esconde telas. Quem **garante** a permissão é o backend (403).

## Build e deploy na AWS (EC2 + Apache2)

1. Em `src/environments/environment.ts` troque `apiUrl` pelo endereço público da API (ex: `http://IP-DA-EC2:8080`).
2. `npx ng build` → arquivos em `dist/aula4_turmab/browser`.
3. Envie para a EC2 e sirva com o Apache: passo a passo em **[../INSTRUCOES.md](../INSTRUCOES.md)**
   (config do Apache em [../configuracoes/frota-front.conf](../configuracoes/frota-front.conf)).
