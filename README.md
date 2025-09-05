# Electrical Preorder System Backend

Preorder Management System for managing technology product preorder campaigns, orders, payments and notifications.

## Technologies

- Java 21, Spring Boot 3.4.1  
- Spring Data JPA + PostgreSQL ([pom.xml](pom.xml))  
- Spring Security with JWT ([SecurityConfig](src/main/java/com/example/electrical_preorder_system_backend/config/SecurityConfig.java)) & OAuth2 (Google)  
- Redis caching ([RedisConfiguration](src/main/java/com/example/electrical_preorder_system_backend/config/redis/RedisConfiguration.java))  
- RabbitMQ messaging ([RabbitMQConfig](src/main/java/com/example/electrical_preorder_system_backend/config/rabbitmq/RabbitMQConfig.java))  
- Firebase Cloud Messaging ([FirebaseConfig](src/main/java/com/example/electrical_preorder_system_backend/config/firebase/FirebaseConfig.java))  
- PayOS integration for payments ([PaymentService](src/main/java/com/example/electrical_preorder_system_backend/service/payment/PaymentService.java))  
- Cloudinary for file uploads  
- Springdoc OpenAPI ([OpenAPIConfig](src/main/java/com/example/electrical_preorder_system_backend/config/swagger/OpenAPIConfig.java))  
- Docker & Docker Compose ([Dockerfile](Dockerfile), [docker-compose.yml](docker-compose.yml))  
- CI/CD via GitHub Actions ([development_elecee.yml](.github/workflows/development_elecee.yml))

## Getting Started

### Prerequisites

- JDK 21 (or use the wrapper: `./mvnw`)  
- Docker & Docker Compose  
- PostgreSQL, Redis, RabbitMQ instances (or use Docker)

### Configuration

Copy `.env.example` (if provided) or set environment variables according to [development_elecee.yml](.github/workflows/development_elecee.yml):

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/elecee
export DATABASE_USERNAME=…
export DATABASE_PASSWORD=…
export JWT_SECRET=…
export API_PREFIX=/api/v1
export SWAGGER_UI_PATH=/swagger-ui.html
# …other vars (REDIS_HOST, RABBITMQ_ADDRESSES, CLOUDINARY_*, FIREBASE_*, PAYOS_CLIENT_ID, etc.)
```

### Running Locally

Build and run:

```bash
./mvnw clean package -DskipTests
java -jar target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q)-$(./mvnw help:evaluate -Dexpression=project.version -q).jar
```

The service starts on `http://localhost:8080` by default.

### Docker

Build and launch all dependencies:

```bash
docker compose up --build
```

See [README.Docker.md](README.Docker.md) for details.

## API Documentation

Once running, view the OpenAPI UI at:

```
http://localhost:8080${SWAGGER_UI_PATH}
```

(Default path is `/swagger-ui.html`, configured in [OpenAPIConfig](src/main/java/com/example/electrical_preorder_system_backend/config/swagger/OpenAPIConfig.java))

## Testing

Run unit and integration tests via:

```bash
./mvnw test
```

## Project Structure

- `src/main/java/com/example/electrical_preorder_system_backend` – application code  
- `src/main/resources` – configuration & static files  
- `Dockerfile`, `docker-compose.yml` – container setup  

## Contributing

1. Fork the repository  
2. Create your feature branch (`git checkout -b feature/foo`)  
3. Commit your changes (`git commit -am 'Add foo'`)  
4. Push to the branch (`git push origin feature/foo`)  
5. Open a Pull Request  

## License

This project is licensed under the Apache 2.0 License. See [LICENSE](LICENSE).  
