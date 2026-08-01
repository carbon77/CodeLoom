# AGENTS.md

## Repo layout
Three **independent** projects — not a Gradle multi-module build. No root build file, no root test runner. Run every command inside the relevant subdirectory. CI (`.github/workflows/ktlint.yml`) runs `ktlintCheck` on backend and executor.

- `backend/` — Kotlin + Spring Boot 4 REST API (Spring Data JDBC, OAuth2 resource server, Kafka producer, Flyway). Entry: `com.codeloom.backend.BackendApplication`. REST under `/v1`, Swagger at `/docs/swagger.html`.
- `executor/` — Kotlin + Spring Boot judge service: Kafka consumer + docker-java. **No REST controllers.** Entry: `com.codeloom.executor.ExecutorApplication`.
- `frontend/` — Vue 3 + Vite + TS (Pinia, PrimeVue, Tailwind 4, axios). Package manager is **pnpm**.

## Infrastructure
- `docker compose up -d` (repo root) starts Keycloak, Postgres, Kafka, Zookeeper, Adminer. `init.sql` creates DBs `codeloom_backend` and `keycloak`.
- Host ports: Keycloak **8080**, Postgres **5433** (not 5432), Kafka **29092**, Adminer **8088**. Keycloak admin: `admin/password`.
- Backend expects a Keycloak realm `codeloom` (issuer-uri hardcoded to `http://localhost:8080/realms/codeloom`) and maps only realm roles prefixed `ROLE_` (use `ROLE_ADMIN`/`ROLE_USER`).

## Commands
- backend & executor: `./gradlew bootRun`, `./gradlew test`, `./gradlew ktlintCheck`, `./gradlew ktlintFormat` (Java 21 toolchain; Windows: `gradlew.bat`).
- frontend: `pnpm install`, `pnpm dev`, `pnpm type-check`, `pnpm build` (runs type-check first), `pnpm lint` (eslint with `--fix`), `pnpm format`.

## Config / env
- backend needs `CODELOOM_DB_URL`, `CODELOOM_DB_USER`, `CODELOOM_DB_PASSWORD`; optional `CODELOOM_PORT` (default 8080).
- executor needs `CODELOOM_EXECUTOR_DB_URL/USER/PASSWORD`, `CODELOOM_EXECUTOR_PORT`, `CODELOOM_EXECUTOR_DOCKER_HOST`.
- Each dir has a **gitignored** `.env` with these values; Spring does not read `.env` automatically — values must be exported or passed at run time.
- Gotcha: backend's default port (8080) collides with Keycloak on the host; run backend on another port locally. `frontend/src/api.ts` hardcodes `http://localhost:8080/v1` and must be edited if the backend moves.

## Tests
- backend: integration tests in `src/test/kotlin/com/codeloom/backend/it/` use Testcontainers Postgres (`postgres:18.1-alpine3.23`); each IT class spins up its own container via `@ServiceConnection`. MockMvc with filters disabled. Requires a running Docker daemon; does not need Kafka up.
- executor: `DockerJudgeEngineTest` and `SubmissionProcessingServiceTest` execute real containers through docker-java and assert container/volume cleanup. Require a working Docker daemon and network access to pull language images.

## Architecture gotchas
- `SubmissionStatus` is **duplicated** (`backend/.../model/SubmissionStatus.kt` and `executor/.../engine/SubmissionStatus.kt`) and serialized as plain strings over Kafka — keep both in sync.
- Flow: backend publishes `SubmissionEvent` to topic `submissions` (key = submissionId); executor consumes and publishes status changes to `submission_statuses`. Backend has **no consumer for `submission_statuses` yet**, so a submission's row stays `PENDING` until that round-trip is added.
- Executor's `@KafkaListener` hardcodes `topics = ["submissions"]` and `groupId = "codeloom"`, ignoring the configurable topic/group in `application.yaml`.
- Languages are defined in `executor/.../languages/LanguageSpec.kt` (java/cpp/python) with docker image + compile/run commands; images are pulled on demand.
- Judge engine: one docker volume per submission, containers run with `--network none` + memory limit; exit codes 124 (timeout) and 137 (OOM) map to `TIME_LIMIT_EXCEEDED` / `MEMORY_LIMIT_EXCEEDED`.
- Flyway migrations live in `backend/src/main/resources/db/migration` (currently one file, `V1_0__create_tables.sql`).
- Spring Data JDBC needs JSON column converters registered in `backend/.../config/JdbcConfig.kt`; a custom `ObjectMapper` bean is defined in `BackendApplication.kt`.
- Security configs are profile-gated: `SecurityConfig` is `@Profile("!test")`, `TestSecurityConfig` is `@Profile("test")`.
