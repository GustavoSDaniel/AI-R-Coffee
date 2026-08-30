# Frontend — AI/R Coffee

Loja virtual de **café, acessórios e brindes**, construída como uma aplicação **Headless Commerce** em miniatura. Este diretório contém todo o front-end: um site **estático**, sem frameworks, que monta a vitrine dinamicamente a partir de dados estruturados (JSON/API) e consome uma API separada para identidade e pagamento.

---

## 1. Tecnologias utilizadas

| Tecnologia | Papel |
|---|---|
| **HTML5** | Estrutura semântica das páginas (semântica, `aria-*`, `skip-link`, meta viewport). |
| **CSS3** | Design system com *CSS Custom Properties*, temas claro/escuro, responsividade (`grid`, `clamp`, media queries). |
| **Vanilla JS (ES6+)** | Toda a lógica: roteador por hash, renderização, busca/filtro, carrinho, tema e comunicação com a API. |

Sem frameworks, sem bundlers e sem dependências de build — apenas HTML, CSS e JavaScript puros.

---

## 2. Conceito: Headless Commerce

A loja aplica o conceito de **headless commerce** na prática: o **conteúdo** (o catálogo de produtos) é totalmente separado da **apresentação** (o HTML/CSS/JS).

- O `index.html` **não contém nenhum produto hardcoded** — o `<main id="app">` começa vazio.
- Quem monta a vitrine é o JavaScript, lendo o catálogo via `fetch`.
- O catálogo vive em `products.json` e/ou numa API (`/products/active`), num formato estruturado que qualquer front (site, app, etc.) poderia consumir.

Na prática, adicionar um produto ao catálogo **não exige alterar nenhuma linha de HTML** — a vitrine é um reflexo dos dados.

---

## 3. Como a vitrine consome os dados (e o mecanismo de fallback)

### Fluxo principal

```
index.html (shell vazio)
   │
   └── js/app.js  →  roteador por hash (#/, #/produto/:id, #/carrinho, #/checkout)
            │
            └── Api.getAllActiveProducts()   (js/api.js)
                     │
                     ├── 1) GET /api/v1/products/active   (API Spring Boot, paginado)
                     │
                     └── 2) fetch('products.json')        (fallback local)
                              │
                              └── merge + dedup por id  →  renderGrid()
```

### Passo a passo

1. **`app.js`** chama `ensureProducts()`, que dispara `Api.getAllActiveProducts()`.
2. **`api.js`** busca o catálogo em **duas fontes, em paralelo**:
   - a **API** (`/products/active`), paginada, como fonte primária;
   - o **`products.json`** local, como fonte de garantia.
3. As duas listas são **mescladas e deduplicadas por `id`** (`getAllActiveProducts`).
4. O resultado vira `App.products`, e `renderGrid()` gera os cards dinamicamente via `productCardHtml()`.

### O mecanismo de fallback

O `products.json` atua como **fallback local**: se a API principal estiver fora do ar, lenta ou retornar vazio, a loja **continua de pé**, exibindo o catálogo estático. O desacoplamento entre conteúdo e serviço garante que a vitrine não quebra se a origem cair — apenas as operações que exigem backend (login e checkout) ficam indisponíveis.

> Detalhe de robustez: as chamadas à API têm **timeout** (`AbortController`) para a vitrine nunca ficar "travada" aguardando o backend.

---

## 4. Estrutura de pastas

```
frontend/
├── index.html              # Shell da aplicação (header, nav de categorias, <main>, footer)
├── como-fiz.html           # Página do vídeo de apresentação + diagrama BFF
├── products.json           # Catálogo local (fallback headless)
├── css/
│   └── styles.css          # Design system, temas e responsividade
├── js/
│   ├── config.js           # Config de ambiente (URLs da API, Keycloak, pageSize)
│   ├── utils.js            # Helpers (BRL, escape, normalize, placeholder, toast)
│   ├── cart.js             # Carrinho persistido em localStorage
│   ├── api.js              # Cliente HTTP + merge/fallback do catálogo
│   ├── auth.js             # Autenticação via Keycloak (OIDC + PKCE)
│   ├── theme.js            # Alternância de tema claro/escuro
│   └── app.js              # Roteador (hash) + renderização das páginas
├── assets/
│   ├── img/                # Imagens dos produtos e o diagrama de arquitetura BFF
│   └── videos/             # Vídeo de apresentação auto-hospedado (apresentacao.mp4)
└── checkout/
    ├── sucesso.html        # Retorno de pagamento aprovado (Stripe)
    └── cancelado.html      # Retorno de pagamento cancelado (Stripe)
```

Ordem de carregamento dos scripts em `index.html` (importante por causa das dependências globais):

```
js/config.js → js/utils.js → js/cart.js → js/api.js → js/auth.js → js/theme.js → js/app.js
```

---

## 5. Funcionalidades implementadas

### 🛒 Catálogo headless com busca e filtro

- Vitrine montada via `fetch` (API + `products.json`).
- **Busca** por nome com *sugestões* vindas da API (`/products/search`) e filtro local **sem sensibilidade a acentos** (`Utils.normalize`).
- **Filtro por categoria** (chips) e **ordenação** (nome A–Z, menor/maior preço).
- **Paginação** client-side (12 produtos por página).
- Página de **detalhe do produto** com estoque e quantidade.

### 🛍️ Carrinho de compras

- Persistido em `localStorage` (chave `aircoffee.cart.v1`).
- Operações de adicionar, alterar quantidade e remover.
- Subtotal recalculado no navegador (o preço **final** é sempre confirmado pelo backend no checkout).

### 🌗 Dark Mode

- Tema claro/escuro via *CSS Custom Properties* (`:root` e `[data-theme="dark"]`).
- Alternância com persistência da escolha em `localStorage` (`aircoffee.theme`).
- Respeita a preferência do sistema (`prefers-color-scheme`) no primeiro acesso.

### 🔐 Login e checkout reais

- Autenticação via **Keycloak** (OIDC, fluxo **PKCE S256**, escopo `openid profile email`).
- Checkout via **Stripe**: o front cria a sessão de pagamento e redireciona para a página segura da Stripe. O **preço cobrado sempre vem do banco** — nunca é calculado no navegador.
- Retorno tratado em `/checkout/sucesso` (limpa o carrinho) e `/checkout/cancelado`.

### 🎬 Página "Como Fiz" com vídeo auto-hospedado

- A página `/como-fiz` embute o **vídeo de apresentação do projeto auto-hospedado** no próprio site (tag `<video src="assets/videos/apresentacao.mp4">`), sem depender de YouTube/Loom.
- Também exibe o **diagrama de arquitetura BFF** (`assets/img/arquitetura-bff.jpg`), explicando onde entraria um *Backend for Frontend* caso a loja ganhasse um app mobile.
- **Destaque:** o vídeo auto-hospedado conta pontos extras no desafio — o arquivo vive junto com o projeto, o que também ilustra, na prática, o mundo real das CDNs (peso e entrega de mídia).

### ♿ Acessibilidade e qualidade

- `skip-link` para pular ao conteúdo, `aria-label` em controles de ícone, contraste adequado nos dois temas.
- Respeito a `prefers-reduced-motion`.
- Imagens com `loading="lazy"` e fallback para *placeholder* gerado em SVG (via `data:` URI) caso a imagem falhe.

---

## 6. Como rodar localmente

O projeto é **estático**, mas o `fetch('products.json')` exige um **servidor HTTP** — abrir o `index.html` direto via `file://` quebra o carregamento do catálogo.

### Opção 1 — VS Code + Live Server (recomendado)

1. Instale a extensão **Live Server** no VS Code.
2. Abra a pasta `frontend/` no VS Code.
3. Clique com o botão direito em `index.html` → **"Open with Live Server"**.
4. A loja abre em `http://127.0.0.1:5500` (ou porta equivalente).

### Opção 2 — Python (qualquer sistema)

```bash
cd frontend
python3 -m http.server 8080
# acesse http://localhost:8080
```

### Opção 3 — Node.js

```bash
cd frontend
npx serve .
```

### Configuração de ambiente

O `js/config.js` decide automaticamente as URLs pelo host:

- **localhost** → aponta para a API e o Keycloak locais;
- **produção** → aponta para a API publicada.

Para rodar apenas a vitrine (sem backend), basta o `products.json` — o fallback garante que a loja carregue normalmente.

---

## 7. Deploy

O front-end é um site estático, hospedado na **Vercel**, que atua como **CDN** na entrega: os arquivos (HTML, CSS, JS e imagens) são servidos das bordas, próximos do usuário — exatamente o papel que uma CDN cumpre na arquitetura headless.
