# AI/R Coffee ☕

> E-commerce **headless** de café, acessórios e brindes — entrega final do desafio **"Minha Loja no Ar"** do Bootcamp **AI/R · Trilha Commerce**.

**Vitrine estática (Vanilla JS)** + **BFF em Spring Boot** consumidos por uma arquitetura headless completa, com identidade (Keycloak) e pagamento (Stripe) reais.

---

## 1. Visão geral da arquitetura

O projeto é dividido em duas partes independentes, publicadas em infraestruturas diferentes e conectadas apenas por **HTTP/HTTPS**:

| Camada | Onde roda | O que faz |
|---|---|---|
| **Frontend (headless)** | **Vercel** (CDN) | Site estático — HTML, CSS e JavaScript puros. Monta a vitrine dinamicamente via `fetch`, sem produto *hardcoded*. |
| **API Backend (BFF)** | **VPS Ubuntu na Oracle Cloud** | API REST em **Spring Boot** atrás de um **Nginx**, com **PostgreSQL**, **Keycloak** e **Stripe**. |

O frontend em produção aponta para `https://api.ai-r-coffee.gustavosdaniel.com/api/v1` e para o Keycloak em `https://api.ai-r-coffee.gustavosdaniel.com/auth` (ver `frontend/js/config.js`). A comunicação acontece assim:

```
┌─────────────────────────────┐          ┌────────────────────────────────────────────┐
│   Frontend (Vercel / CDN)   │          │     Oracle Cloud (VPS Ubuntu)              │
│                             │          │                                            │
│  HTML / CSS / Vanilla JS    │          │                    ┌───────────────────┐   │
│  (vitrine, carrinho, tema)  │  HTTPS   │    ┌────────────┐  │   PostgreSQL      │   │
│                             │ ───────► │    │            │  │   (Flyway)        │   │
│  • /api/v1/products         │          │    │   Nginx    │─►│                   │   │
│  • /auth (login)            │          │    │  (TLS 443) │  └───────────────────┘   │
│  • /checkout/session        │          │    │            │  ┌───────────────────┐   │
│                             │ ◄─────── │    │  rate limit│  │  Keycloak         │   │
│  • fallback: products.json  │          │    └────────────┘  │  (OIDC / PKCE)    │   │
│  • Stripe Checkout (redirect)│         │         │          └───────────────────┘   │
└─────────────────────────────┘          │         │ route: /auth → Keycloak         │
                                         │         │ route:  /*    → API (:7070)     │
                                         │         ▼                                  │
                                         │   ai-r-coffee-api (Spring Boot) ───────► Stripe
                                         └────────────────────────────────────────────┘
```

Pontos-chave da arquitetura:

- **Nginx** faz terminação TLS (Let's Encrypt/Certbot), roteia `/auth/` para o Keycloak e o restante para o container da API, além de aplicar *rate limiting* (`limit_req`).
- **Robustez no front**: o catálogo tem *fallback* local (`products.json`) — se a API cair, a vitrine continua de pé; só login e checkout ficam indisponíveis.
- **BFF (Backend for Frontend)**: a API é desenhada para servir a vitrine e há um diagrama arquitetural na página `/como-fiz` mostrando onde entraria um BFF dedicado caso a loja ganhasse um app mobile.

---

## 2. Estrutura do repositório

```
AI-R-Coffee/
├── frontend/            # Site estático (Headless Commerce) — Vercel
│   ├── index.html       # Shell da aplicação
│   ├── como-fiz.html    # Gravação do desafio + diagrama BFF
│   ├── products.json    # Catálogo local (fallback headless)
│   ├── css/  js/  assets/  checkout/
│   └── README.md
├── ai-r-coffee-api/     # API Backend (BFF) em Spring Boot — Oracle Cloud
│   ├── src/             # Código Java (controllers, services, migrações)
│   ├── infra/           # Nginx, Keycloak e docker-compose de produção
│   ├── compose.yaml     # Ambiente de dev (API + Postgres + Keycloak)
│   ├── pom.xml
│   └── README.md
└── README.md            # Este arquivo
```

---

## 3. Documentação por pasta

Cada parte do projeto tem documentação própria e detalhada:

- 📦 **[Frontend — `frontend/README.md`](frontend/README.md)** — vitrine headless, mecanismo de fallback, carrinho, dark mode, autenticação e deploy na Vercel.
- ⚙️ **[API Backend — `ai-r-coffee-api/README.md`](ai-r-coffee-api/README.md)** — endpoints, segurança JWT/Keycloak, modelagem do banco (Flyway), variáveis de ambiente e infraestrutura.

---

## 4. Como clonar e executar

### Clonar o repositório

```bash
# HTTPS
git clone https://github.com/GustavoSDaniel/AI-R-Coffee.git

# ou SSH
git clone git@github.com:GustavoSDaniel/AI-R-Coffee.git

cd AI-R-Coffee
```

### Rodar o frontend (vitrine)

O site é estático, mas o `fetch('products.json')` exige um servidor HTTP:

```bash
cd frontend
python3 -m http.server 8080
# acesse http://localhost:8080
```

Para rodar apenas a vitrine, o `products.json` (fallback) já é suficiente — não é necessário subir o backend.

### Rodar o backend (API)

O ambiente de desenvolvimento sobe API, PostgreSQL e Keycloak via Docker Compose:

```bash
cd ai-r-coffee-api
cp variaveis-de-ambiente.example.env .env   # preencha as credenciais
docker compose up --build
```

A API sobe em `http://localhost:7070` e o Swagger UI em `http://localhost:7070/swagger-ui.html`.

> Para instruções completas (variáveis, endpoints, deploy em produção), consulte os READMEs específicos de cada pasta.

---

## 5. Tecnologias

| Área | Stack |
|---|---|
| **Frontend** | HTML5 · CSS3 · Vanilla JS (ES6+) — sem frameworks ou bundlers |
| **Backend** | Java 21 · Spring Boot 4.1.1 |
| **Banco de dados** | PostgreSQL 18.6 · Flyway (migrações) |
| **Identidade** | Keycloak (OAuth 2.0 / OpenID Connect · PKCE) |
| **Pagamento** | Stripe (Checkout Sessions) |
| **Infraestrutura** | Vercel (CDN) · Oracle Cloud (VPS Ubuntu) · Docker · Nginx · Let's Encrypt |

---

## Licença

[Apache License 2.0](LICENSE)
