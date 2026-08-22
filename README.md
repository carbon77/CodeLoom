# 🧵 CodeLoom

> Платформа для решения задач по программированию в духе LeetCode и Codeforces.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Apache_Kafka-7.7-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

## О проекте

CodeLoom позволяет пользователям находить задачи по теме и сложности, отправлять решения на Java, C++ или Python и получать результаты по каждому тесту. Администраторы управляют задачами, темами и тест-кейсами.

Проверка выполняется асинхронно: REST API сохраняет отправку и публикует событие в Kafka, executor запускает код в изолированных Docker-контейнерах, а затем отправляет итоговый статус обратно в backend.

## Возможности

- авторизация через Keycloak (OpenID Connect), роли `USER` и `ADMIN`;
- каталог задач с фильтрацией по сложности и теме;
- создание, редактирование, публикация и удаление задач;
- управление темами и тест-кейсами;
- отправка решений и история попыток;
- Java 21, C++20 и Python 3;
- ограничения времени и памяти, запуск без сети;
- полный жизненный цикл отправки: `PENDING`, `COMPILING`, `RUNNING` и финальные статусы `ACCEPTED`, `WRONG_ANSWER`, `COMPILE_ERROR`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`, `MEMORY_LIMIT_EXCEEDED` или `SYSTEM_ERROR`.

## Архитектура

```text
React SPA ──HTTP/JWT──> Backend API ──submissions──> Kafka
                            ^                         │
                            │                         v
                       PostgreSQL <────────────── Executor ──> Docker
                            ^                         │
                            └──submission_statuses───┘
```

- `common/` — общие Kafka-события, статусы и спецификации языков;
- `backend/` — Spring Boot REST API, Spring Data JDBC, Flyway, OAuth2 Resource Server, Kafka producer/consumer;
- `executor/` — Kafka consumer и Docker-based judge engine, REST API отсутствует;
- `frontend/` — React 19, TypeScript, Vite, MUI, React Router, Monaco Editor и `oidc-client-ts`.

## Требования

- JDK 21;
- Docker с доступным Docker daemon;
- Node.js и [pnpm](https://pnpm.io/);
- Git.

## Быстрый старт

### 1. Инфраструктура

```bash
git clone https://github.com/carbon77/CodeLoom.git
cd CodeLoom
docker compose up -d
```

Compose запускает только инфраструктуру:

| Сервис | Адрес/порт | Данные для входа |
|---|---|---|
| Keycloak | `http://localhost:8080` | `admin` / `password` |
| PostgreSQL | `localhost:5433` | `zakat` / `zakat_pwd` |
| Kafka | `localhost:29092` | — |
| Adminer | `http://localhost:8088` | параметры PostgreSQL выше |

При первом запуске PostgreSQL создаются базы `codeloom_backend` и `keycloak`.

### 2. Keycloak

В Keycloak создайте:

1. realm `codeloom`;
2. realm-роли `ROLE_USER` и `ROLE_ADMIN`;
3. OpenID Connect client `codeloom-frontend` с redirect URI `http://localhost:5173/*` и web origin `http://localhost:5173`;
4. пользователя и назначьте ему нужную realm-роль.

Backend принимает только realm-роли с префиксом `ROLE_`.

### 3. Backend

Порт `8080` по умолчанию занят Keycloak, поэтому локально используйте, например, `8081`.

PowerShell:

```powershell
$env:CODELOOM_DB_URL = "jdbc:postgresql://localhost:5433/codeloom_backend"
$env:CODELOOM_DB_USER = "zakat"
$env:CODELOOM_DB_PASSWORD = "zakat_pwd"
$env:CODELOOM_PORT = "8081"
.\gradlew.bat :backend:bootRun
```

Bash:

```bash
CODELOOM_DB_URL=jdbc:postgresql://localhost:5433/codeloom_backend \
CODELOOM_DB_USER=zakat CODELOOM_DB_PASSWORD=zakat_pwd CODELOOM_PORT=8081 \
./gradlew :backend:bootRun
```

Swagger UI: `http://localhost:8081/docs/swagger.html`; OpenAPI JSON: `/docs/api`.

### 4. Executor

Executor использует ту же базу и должен иметь доступ к Docker daemon. Образы `eclipse-temurin:21-jdk`, `gcc:15` и `python:3.14-slim` загружаются при необходимости.

PowerShell:

```powershell
$env:CODELOOM_EXECUTOR_DB_URL = "jdbc:postgresql://localhost:5433/codeloom_backend"
$env:CODELOOM_EXECUTOR_DB_USER = "zakat"
$env:CODELOOM_EXECUTOR_DB_PASSWORD = "zakat_pwd"
.\gradlew.bat :executor:bootRun
```

Bash:

```bash
CODELOOM_EXECUTOR_DB_URL=jdbc:postgresql://localhost:5433/codeloom_backend \
CODELOOM_EXECUTOR_DB_USER=zakat CODELOOM_EXECUTOR_DB_PASSWORD=zakat_pwd \
./gradlew :executor:bootRun
```

По умолчанию executor подключается к `unix:///var/run/docker.sock`. Другой адрес задаётся через `CODELOOM_EXECUTOR_DOCKER_HOST`.

### 5. Frontend

Создайте `frontend/.env`:

```dotenv
VITE_APP_URL=http://localhost:5173
VITE_API_URL=http://localhost:8081
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=codeloom
VITE_KEYCLOAK_CLIENT_ID=codeloom-frontend
```

Запустите приложение:

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend будет доступен по адресу `http://localhost:5173`.

> Файлы `.env` игнорируются Git. Spring Boot не загружает их автоматически, поэтому переменные backend и executor нужно экспортировать в окружение процесса.

## Разработка и тесты

Gradle-команды выполняются из корня репозитория:

```bash
./gradlew :backend:test
./gradlew :executor:test
./gradlew spotlessCheck
./gradlew spotlessApply
```

На Windows используйте `gradlew.bat`. Backend-тесты запускают PostgreSQL через Testcontainers, а Kafka integration test также создаёт Kafka-контейнер. Executor-тесты действительно компилируют и запускают решения в Docker. Для обоих наборов нужен работающий Docker daemon; executor также может загрузить языковые образы из сети.

Frontend-команды выполняются в `frontend/`:

```bash
pnpm type-check
pnpm lint
pnpm build
```

GitHub Actions запускает backend- и executor-тесты для pull request, когда меняются соответствующий модуль или `common/`.

## Конфигурация

| Переменная | Назначение | По умолчанию |
|---|---|---|
| `CODELOOM_DB_URL` | JDBC URL backend | обязательно |
| `CODELOOM_DB_USER` | пользователь БД backend | обязательно |
| `CODELOOM_DB_PASSWORD` | пароль БД backend | обязательно |
| `CODELOOM_PORT` | порт backend | `8080` |
| `CODELOOM_EXECUTOR_DB_URL` | JDBC URL executor | обязательно |
| `CODELOOM_EXECUTOR_DB_USER` | пользователь БД executor | обязательно |
| `CODELOOM_EXECUTOR_DB_PASSWORD` | пароль БД executor | обязательно |
| `CODELOOM_EXECUTOR_PORT` | порт executor | `8082` |
| `CODELOOM_EXECUTOR_DOCKER_HOST` | адрес Docker daemon | `unix:///var/run/docker.sock` |

Kafka использует топики `submissions` и `submission_statuses`. Backend API находится под `/v1`; Swagger открыт без авторизации, остальные маршруты защищены JWT.

## Структура репозитория

```text
CodeLoom/
├── common/                 # Общие модели событий и языков
├── backend/                # REST API и миграции Flyway
├── executor/               # Сервис проверки решений
├── frontend/               # React/Vite SPA
├── .github/workflows/      # CI
├── docker-compose.yaml     # Локальная инфраструктура
├── init.sql                # Создание баз PostgreSQL
├── build.gradle            # Общие Gradle-настройки
└── settings.gradle         # Состав multi-module build
```

## Roadmap

- [ ] расширение списка поддерживаемых языков;
- [ ] рейтинги и статистика пользователей;
- [ ] соревновательный режим (контесты).

## Автор и лицензия

Автор: [carbon77](https://github.com/carbon77). Проект распространяется по лицензии [MIT](LICENSE).
