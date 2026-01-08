# My Best Restaurants

API REST para compartilhar seus restaurantes favoritos. Cada usuário pode cadastrar restaurantes com suas opiniões pessoais sobre o que mais gostou.

## Tecnologias

- Java 21
- Spring Boot 3.4.1
- Spring Security 6 + JWT
- Spring Data JPA
- PostgreSQL
- Maven
- Docker
- Testcontainers

## Requisitos

- Java 21+
- Docker e Docker Compose
- Maven 3.9+ (ou use o wrapper `./mvnw`)

## Executando com Docker

```bash
docker-compose up --build
```

A aplicação estará disponível em `http://localhost:8080`

## Executando localmente

1. Inicie o PostgreSQL:
```bash
docker run -d --name postgres -e POSTGRES_DB=restaurants -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16-alpine
```

2. Execute a aplicação:
```bash
./mvnw spring-boot:run
```

## Endpoints

### Autenticação

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/auth/register` | Registrar usuário | Não |
| POST | `/api/auth/login` | Login (retorna JWT) | Não |

### Restaurantes

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/restaurants` | Listar todos | Não |
| GET | `/api/restaurants/{id}` | Buscar por ID | Não |
| GET | `/api/restaurants/my` | Meus restaurantes | Sim |
| POST | `/api/restaurants` | Criar restaurante | Sim |
| PUT | `/api/restaurants/{id}` | Atualizar | Sim |
| DELETE | `/api/restaurants/{id}` | Deletar | Sim |

## Exemplos de uso

### Registrar usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Felipe",
    "email": "felipe@email.com",
    "password": "123456"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "felipe@email.com",
    "password": "123456"
  }'
```

### Criar restaurante

```bash
curl -X POST http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN" \
  -d '{
    "name": "Outback Steakhouse",
    "description": "Melhor costela da cidade! Atendimento excelente.",
    "location": "Shopping Center Norte",
    "rating": 5
  }'
```

### Listar restaurantes

```bash
curl http://localhost:8080/api/restaurants
```

## Testes

### Executar todos os testes

```bash
./mvnw test
```

### Executar apenas testes unitários

```bash
./mvnw test -Dtest="*Test"
```

### Executar apenas testes de integração

```bash
./mvnw test -Dtest="*IntegrationTest"
```

Os testes de integração usam Testcontainers para criar um PostgreSQL automaticamente.

## Estrutura do Projeto

```
src/main/java/br/com/labs/
├── MyBestRestaurantsApplication.java
├── controller/
│   ├── AuthController.java
│   └── RestaurantController.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   └── RestaurantService.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── RestaurantRepository.java
├── model/
│   ├── User.java
│   ├── Role.java
│   └── Restaurant.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── RestaurantRequest.java
│   └── response/
│       ├── TokenResponse.java
│       ├── UserResponse.java
│       └── RestaurantResponse.java
├── config/
│   └── SecurityConfig.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
```

## Variáveis de Ambiente

| Variável | Descrição | Default |
|----------|-----------|---------|
| DATABASE_URL | URL do PostgreSQL | jdbc:postgresql://localhost:5432/restaurants |
| DATABASE_USERNAME | Usuário do banco | postgres |
| DATABASE_PASSWORD | Senha do banco | postgres |
| JWT_SECRET | Chave secreta do JWT | (chave padrão) |
| JWT_EXPIRATION | Tempo de expiração do token (ms) | 86400000 (24h) |
| SERVER_PORT | Porta da aplicação | 8080 |
