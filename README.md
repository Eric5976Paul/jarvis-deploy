# jarvis-deploy

A lightweight CLI for managing multi-environment deployments with rollback support for Spring Boot apps.

---

## Installation

```bash
git clone https://github.com/your-org/jarvis-deploy.git
cd jarvis-deploy && ./mvnw install -DskipTests
```

---

## Usage

```bash
# Deploy to a target environment
jarvis deploy --env production --app my-service --version 2.4.1

# Roll back to the previous stable release
jarvis rollback --env staging --app my-service

# List available deployments
jarvis list --env production
```

### Configuration

Create a `jarvis.yml` in your project root:

```yaml
environments:
  staging:
    host: staging.example.com
    port: 8080
  production:
    host: prod.example.com
    port: 443

app:
  artifact: target/my-service.jar
  health-check: /actuator/health
```

---

## Requirements

- Java 17+
- Maven 3.8+
- SSH access to target hosts

---

## Features

- 🚀 One-command deploys to multiple environments
- ⏪ Instant rollback to any previous version
- 🩺 Automatic health checks via Spring Boot Actuator
- 📋 Deployment history and audit logging

---

## License

This project is licensed under the [MIT License](LICENSE).