# API Backend — AI/R Coffee

Backend (BFF) da loja de café, acessórios e brindes do projeto **AI/R Fellowship**. A aplicação expõe uma API REST em **Spring Boot** responsável pelo catálogo (categorias e produtos), pela gestão de usuários sincronizada com o **Keycloak** e pela geração de sessões de pagamento no **Stripe**.

> ⚠️ Documentação gerada a partir do código real do repositório (`Controllers`, `Services`, migrações Flyway, `pom.xml` e arquivos de infraestrutura). Nada além do que está implementado foi descrito.

---

## 1. Visão geral

| Item | Valor |
|------|-------|
| Linguagem | Java 21 (com `--enable-preview`) |
| Framework | Spring Boot 4.1.1 |
| Banco de dados | PostgreSQL (18.6) |
| Migrações | Flyway |
| Identidade / autenticação | Keycloak (OAuth 2.0 / OpenID Connect) |
| Gateway de pagamento | Stripe |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Porta da API | `7070` |

---

## 2. Tecnologias

Dependências declaradas no `pom.xml`:

| Dependência | Finalidade |
|-------------|------------|
| `spring-boot-starter-data-jpa` | Persistência via JPA/Hibernate |
| `spring-boot-starter-webmvc` | Camada REST (controllers, JSON, serialização) |
| `spring-boot-starter-validation` | Validação de DTOs com Bean Validation (`@NotBlank`, `@Positive`, etc.) |
| `spring-boot-starter-flyway` + `flyway-database-postgresql` | Versionamento do schema do banco |
| `spring-boot-starter-security` | Segurança da aplicação |
| `spring-boot-starter-oauth2-resource-server` | Validação de JWT como *resource server* (Keycloak) |
| `springdoc-openapi-starter-webmvc-ui` (3.1.0) | Swagger UI + OpenAPI |
| `stripe-java` (33.3.0) | SDK do Stripe para criação de *Checkout Sessions* |
| `postgresql` (runtime) | Driver JDBC do PostgreSQL |
| `spring-boot-devtools`, `spring-boot-docker-compose` (runtime) | Suporte ao desenvolvimento |

Detalhes relevantes:

- **Java 21** com `--enable-preview` no `maven-compiler-plugin`.
- **Virtual threads habilitadas** (`spring.threads.virtual.enabled: true` no `application.yaml`).
- **`spring.jpa.hibernate.ddl-auto: validate`** — o Hibernate apenas valida o schema; quem cria/evolui as tabelas é o **Flyway**.

---

## 3. Endpoints

Todas as rotas estão sob o prefixo `/api/v1`. A documentação interativa fica disponível no **Swagger UI** (`/swagger-ui.html`) e o JSON do OpenAPI em `/v3/api-docs`.

### 3.1 Usuários — `/api/v1/users`

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| `GET` | `/api/v1/users/me` | Autenticado | Retorna o usuário logado (extraído do JWT); cria o registro local se ainda não existir |
| `GET` | `/api/v1/users/{id}` | `ADMIN` | Busca usuário pelo `UUID` |
| `GET` | `/api/v1/users/email?email=` | `ADMIN` | Busca usuário pelo e-mail |
| `GET` | `/api/v1/users?name=&page=&size=&sort=` | `ADMIN` | Lista paginada de usuários, com filtro por nome (padrão: 20/página, ordenado por `userName`) |
| `PATCH` | `/api/v1/users/{id}/activate` | `ADMIN` | Ativa um usuário |
| `PATCH` | `/api/v1/users/{id}/disable` | `ADMIN` | Desativa um usuário |

### 3.2 Categorias — `/api/v1/categories`

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| `POST` | `/api/v1/categories` | `ADMIN` | Cria categoria (`name`, `description`) |
| `GET` | `/api/v1/categories/all?name=` | `ADMIN` | Lista todas as categorias (filtro por nome) |
| `GET` | `/api/v1/categories/active?name=` | Público | Lista categorias ativas (filtro por nome) |
| `GET` | `/api/v1/categories/inactive?name=` | `ADMIN` | Lista categorias inativas (filtro por nome) |
| `PATCH` | `/api/v1/categories/{id}/activate` | `ADMIN` | Ativa uma categoria |
| `PATCH` | `/api/v1/categories/{id}/disable` | `ADMIN` | Desativa uma categoria |

### 3.3 Produtos — `/api/v1/products`

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| `POST` | `/api/v1/products/{categoryId}` | `ADMIN` | Cria produto vinculado à categoria |
| `GET` | `/api/v1/products/{id}` | Público | Busca **produto ativo** pelo `UUID` |
| `GET` | `/api/v1/products/all?name=&page=&size=&sort=` | `ADMIN` | Lista todos os produtos (paginado, filtro por nome) |
| `GET` | `/api/v1/products/active?page=` | Público | Lista produtos ativos (paginado) |
| `GET` | `/api/v1/products/search?name=` | Público | Busca resumo (`id`, `name`, `imageUrl`) dos produtos ativos |
| `GET` | `/api/v1/products/inactive?name=` | `ADMIN` | Lista resumo dos produtos inativos |
| `GET` | `/api/v1/products/{categoryId}/category-product?page=` | Público | Produtos ativos de uma categoria |
| `PATCH` | `/api/v1/products/{id}/activate` | `ADMIN` | Ativa um produto |
| `PATCH` | `/api/v1/products/{id}/disable` | `ADMIN` | Desativa um produto |

### 3.4 Checkout — `/api/v1/checkout`

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| `POST` | `/api/v1/checkout/session` | Autenticado | Cria uma *Checkout Session* no Stripe e retorna a URL de pagamento |

**Corpo do checkout** (`CheckoutRequest`):

```json
{
  "items": [
    { "productId": "uuid-do-produto", "quantity": 2 }
  ]
}
```

**Resposta**:

```json
{ "url": "https://checkout.stripe.com/..." }
```

> ℹ️ **Escopo atual do checkout:** o serviço apenas valida os produtos (existência, `active` e estoque), monta os *line items* em **BRL** e cria a sessão de pagamento no Stripe. **Não há**, neste código, *webhook* de confirmação de pagamento, decremento de estoque nem tabela de pedidos/pedido-itens — esse fluxo termina na geração do link de pagamento.

---

## 4. Fluxo de segurança (JWT via Keycloak)

A aplicação atua como **OAuth 2.0 Resource Server**: ela não autentica o usuário, apenas **valida o token JWT** emitido pelo Keycloak.

```
 Cliente (frontend)
      │  1. login no Keycloak → recebe access token (JWT)
      ▼
 Requisição à API  (Authorization: Bearer <jwt>)
      │
      ▼
 Spring Security (oauth2ResourceServer.jwt)
      │  2. valida assinatura (JWKS) e issuer do token
      ▼
 JwtAuthenticationConverter
      │  3. extrai roles de "realm_access.roles" e
      │     "resource_access.ai-r-coffee-app.roles"
      │     → mapeia para ROLE_ADMIN / ROLE_CONSUMER
      ▼
 Autorização por rota (authorizeHttpRequests)
```

Pontos de configuração (`SecurityConfig`):

- **Sessão:** `SessionCreationPolicy.STATELESS` — sem estado no servidor; cada requisição carrega o JWT.
- **CSRF:** desabilitado (API stateless autenticada por token).
- **CORS:** habilitado, com origens permitidas definidas por `API_CORS_ALLOWED_ORIGINS` (lista separada por vírgula) aplicada às rotas `/api/**`.
- **Validação do JWT:**
  - `issuer-uri` → `JWT_ISSUER_URI`
  - `jwk-set-uri` → `JWT_JWK_SET_URI` (endpoint de chaves públicas do Keycloak)
- **Extração de roles** (`JwtAuthenticationConverter`):
  - Lê `realm_access.roles` e `resource_access["ai-r-coffee-app"].roles` do token.
  - Prefixa cada role com `ROLE_` e coloca em maiúsculas (ex.: `admin` → `ROLE_ADMIN`).
- **Rotas públicas** (sem autenticação): somente os endpoints do Swagger/OpenAPI (`/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-resources/**`, `/webjars/**`) e as rotas de catálogo marcadas como `permitAll` (ver seção de Endpoints).

**Sincronização de usuários** (`UserService.getOrCreateCurrentUser`): no `GET /users/me`, a API extrai do JWT o `sub` (id do Keycloak), `email`, `preferred_username` e a role; se não existir um registro local com aquele `keycloak_id`, cria um novo `User` no banco (tabela `users`). Se o usuário já existir mas a role do token tiver mudado, a role local é atualizada.

---

## 5. Modelagem do banco de dados (Flyway)

O schema é versionado pelo **Flyway** a partir de `src/main/resources/db/migration`. Três migrações criam o modelo:

### `V1__create_table_users.sql` — tabela `users`

| Coluna | Tipo | Observações |
|--------|------|-------------|
| `id` | `UUID` | PK, `gen_random_uuid()` |
| `keycloak_id` | `VARCHAR(255)` | `NOT NULL UNIQUE` — chave do usuário no Keycloak |
| `email` | `VARCHAR(255)` | `NOT NULL UNIQUE` |
| `user_name` | `VARCHAR(255)` | `NOT NULL` |
| `role` | `VARCHAR(50)` | `NOT NULL DEFAULT 'CONSUMER'` (enum `ADMIN`/`CONSUMER`) |
| `active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | `DEFAULT NOW()` |

Índices: `idx_users_user_name` (user_name) e `idx_users_email_lower` (`LOWER(email)`).

### `V2__create_table_categories.sql` — tabela `categories`

| Coluna | Tipo | Observações |
|--------|------|-------------|
| `id` | `UUID` | PK, `gen_random_uuid()` |
| `name` | `VARCHAR(255)` | `NOT NULL UNIQUE` |
| `description` | `TEXT` | `NOT NULL` |
| `active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | `DEFAULT NOW()` |

Índices: `idx_categories_name_lower` (`LOWER(name)`) e `idx_categories_active` (parcial, `active = TRUE`).

### `V3__create_table_products.sql` — tabela `products`

| Coluna | Tipo | Observações |
|--------|------|-------------|
| `id` | `UUID` | PK, `gen_random_uuid()` |
| `name` | `VARCHAR(255)` | `NOT NULL UNIQUE` |
| `description` | `TEXT` | `NOT NULL` |
| `quantity` | `INTEGER` | `NOT NULL DEFAULT 0` (estoque) |
| `unit_measure` | `VARCHAR(20)` | `NOT NULL DEFAULT 'UNIDADE'` (enum `UnitMeasure`) |
| `price` | `DECIMAL(10,2)` | `NOT NULL` |
| `image_url` | `VARCHAR(255)` | `NOT NULL` |
| `category_id` | `UUID` | `NOT NULL REFERENCES categories(id)` |
| `active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | `DEFAULT NOW()` |

Índices: `idx_products_category_id` (category_id), `idx_products_active` (parcial) e `idx_products_name_lower` (`LOWER(name)`).

### Relacionamentos

```
categories 1 ──── n products
```

Um produto pertence a **uma** categoria (`@ManyToOne` com `@JoinColumn(name = "category_id")`, carregamento *lazy*). Usuários não possuem vínculo de FK com o resto do domínio — são sincronizados a partir do Keycloak.

### Enums

- **`UserRole`**: `ADMIN`, `CONSUMER`.
- **`UnitMeasure`** (unidades de medida dos produtos): `UN` (UNIDADE), `KIT`, `KG` (QUILOGRAMA), `G` (GRAMA), `L` (LITRO), `ML` (MILILITRO), `M` (METRO), `CX` (CAIXA), `PR` (PAR).

---

## 6. Variáveis de ambiente

A API resolve as configurações sensíveis via variáveis de ambiente (com valores padrão para dev). Um template completo está em `variaveis-de-ambiente.example.env`.

### 6.1 Banco de dados (aplicação)

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `7071` | Porta do PostgreSQL |
| `POSTGRES_DB` | — | Nome do banco (ex.: `db_ai_r_coffee`) |
| `POSTGRES_USER` | — | Usuário do banco |
| `POSTGRES_PASSWORD` | — | Senha do banco |

### 6.2 Keycloak / JWT

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `JWT_ISSUER_URI` | `http://localhost:7072/auth/realms/ai-r-coffee-realm` | Issuer usado para validar o JWT |
| `JWT_JWK_SET_URI` | `http://localhost:7072/auth/realms/ai-r-coffee-realm/protocol/openid-connect/certs` | Endpoint de chaves públicas |
| `KEYCLOAK_REALM` | — | Nome do realm (usado na montagem das URIs via compose) |

### 6.3 Stripe

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `STRIPE_SECRET_KEY` | — | Chave secreta do Stripe (usada pelo backend) |
| `FRONTEND_URL` | `http://localhost:3000` | URL base do frontend (para as rotas de sucesso/cancelamento) |

### 6.4 CORS

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `API_CORS_ALLOWED_ORIGINS` | — | Lista de origens permitidas, separadas por vírgula |

### 6.5 Infraestrutura (containers auxiliares)

Usadas pelo Docker Compose para subir PostgreSQL e Keycloak (ver `compose.yaml`):

| Variável | Descrição |
|----------|-----------|
| `KEYCLOAK_DB` / `KEYCLOAK_DB_USER` / `KEYCLOAK_DB_PASSWORD` | Banco dedicado do Keycloak |
| `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` | Credenciais do painel administrativo do Keycloak |

---

## 7. Como rodar a API

### 7.1 Via Docker Compose (recomendado para dev)

O `compose.yaml` sobe quatro serviços: a **API** (build da imagem local), o **PostgreSQL** da aplicação, o **Keycloak** e o **PostgreSQL** do Keycloak.

```bash
# 1. Copie o template de variáveis e preencha
cp variaveis-de-ambiente.example.env .env

# 2. Suba os serviços
docker compose up --build
```

Portas expostas no ambiente de dev:

| Serviço | Porta host | Porta container |
|---------|-----------|-----------------|
| API (`ai-r-coffee-api`) | `7070` | `7070` |
| PostgreSQL aplicação (`db-ai-coffee`) | `7071` | `5432` |
| Keycloak (`keycloak-ai-coffee`) | `7072` | `8080` |
| PostgreSQL Keycloak (`db-keycloak-ai-coffee`) | — (interno) | `5432` |

O perfil `docker` (`SPRING_PROFILES_ACTIVE=docker`) é ativado automaticamente, apontando o `DB_HOST` para o container `postgres` e o `JWT_JWK_SET_URI` para `http://keycloak:8080/...`.

### 7.2 Rodando a API localmente (sem Docker)

Requer um PostgreSQL e um Keycloak acessíveis localmente:

```bash
# 1. Defina as variáveis de ambiente
export DB_HOST=localhost
export DB_PORT=5432
export POSTGRES_DB=db_ai_r_coffee
export POSTGRES_USER=seu_usuario
export POSTGRES_PASSWORD=sua_senha
export JWT_ISSUER_URI=http://localhost:8080/auth/realms/ai-r-coffee-realm
export JWT_JWK_SET_URI=http://localhost:8080/auth/realms/ai-r-coffee-realm/protocol/openid-connect/certs
export STRIPE_SECRET_KEY=sk_test_...
export FRONTEND_URL=http://localhost:3000
export API_CORS_ALLOWED_ORIGINS=http://localhost:3000

# 2. Rode a aplicação
./mvnw spring-boot:run
```

A API sobe em `http://localhost:7070`; o Swagger UI fica em `http://localhost:7070/swagger-ui.html`.

---

## 8. Estrutura do projeto

```
src/main/java/com/gustavosdaniel/aircoffeeapi/
├── App.java                         # Classe principal (SpringApplication)
├── config/
│   ├── SecurityConfig.java          # Segurança, CORS, extração de roles
│   ├── StripeConfig.java            # Configura a chave do Stripe
│   └── OpenApiConfig.java           # Configuração do Swagger/OpenAPI
├── controller/
│   ├── UserController.java
│   ├── CategoryController.java
│   ├── ProductController.java
│   ├── CheckoutController.java
│   └── openApi/                     # Interfaces com anotações Swagger
├── domain/
│   ├── dto/request/                 # DTOs de entrada (records)
│   ├── dto/response/                # DTOs de saída (records)
│   ├── enums/                       # UserRole, UnitMeasure
│   ├── mapping/                     # Mappers entidade ↔ DTO
│   └── po/                          # Entidades JPA (User, Category, Product, BaseEntity)
├── exception/
│   ├── *.java                       # Exceções de negócio
│   └── handler/                     # GlobalExceptionHandler + ProblemType
├── repository/                      # Interfaces JpaRepository
└── service/                         # Regras de negócio

src/main/resources/
├── application.yaml                 # Config principal (perfil default)
├── application-docker.yaml          # Overrides do perfil "docker"
└── db/migration/                    # Migrações Flyway (V1, V2, V3)
```

---

## 9. Tratamento de erros

O `GlobalExceptionHandler` (`@RestControllerAdvice`) padroniza as respostas de erro usando **RFC 7807** (`ProblemDetail`), sempre no formato:

```json
{
  "type": "urn:ai-r-coffee:...",
  "title": "...",
  "status": 400,
  "detail": "...",
  "timestamp": "...",
  "invalid_fields": { "campo": "mensagem" }   // apenas em erros de validação
}
```

| Exceção | HTTP | `type` |
|---------|------|--------|
| `BusinessRuleException` | 422 | `urn:ai-r-coffee:regra-de-negocio` |
| `MethodArgumentNotValidException` | 400 | `urn:ai-r-coffee:erro-de-validacao` |
| `NameExistException` | 409 | `urn:ai-r-coffee:nome-existe` |
| `CategoryNotFoundException` | 404 | `urn:ai-r-coffee:category-not-found` |
| `ProductNotFoundException` | 404 | `urn:ai-r-coffee:product-not-found` |
| `UserNotFoundException` | 404 | `urn:ai-r-coffee:user-not-found` |

---

## 10. Infraestrutura e deploy

- **Produção:** a API e o banco **não rodam localmente** — ficam numa instância da **Oracle Cloud**, orquestrados por `infra/instance/docker-compose.prod.yml`.
- **Proxy reverso:** o **Nginx** (`infra/nginx/conf.d/`) fica na frente da API, faz terminação TLS (Let's Encrypt/Certbot), roteia `/auth/` para o Keycloak e o restante para o container `ai-r-coffee-api`. Aplica *rate limiting* (`limit_req`, 10 req/s com burst 20).
- **Identidade:** gerenciada pelo **Keycloak**, com tema customizado em `infra/keycloak/themes/ai-r-coffee`.
- **Domínio:** `api.ai-r-coffee.gustavosdaniel.com`.
- **Imagem:** o `dockerfile` faz build em dois estágios (Maven/Temurin 21) e roda como usuário não-root (`appuser`), com `MaxRAMPercentage`, G1GC e `ExitOnOutOfMemoryError`.
- **Acesso remoto de desenvolvimento ao banco:** feito via **túnel SSH** até a instância da Oracle Cloud (configuração externa ao código, mantida no ambiente do desenvolvedor).
