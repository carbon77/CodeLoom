# AGENTS.md

## Repo layout
Gradle multi-module build (`settings.gradle` includes `common`, `backend`, `executor`; root `build.gradle` defines shared Kotlin 2.4.10 / Spring Boot 4.1.0 toolchain, Java 21). Commands run **from the repo root**, e.g. `./gradlew :backend:bootRun`. No root runner/CI besides GitHub Actions (see below).

- `common/` — shared Kafka event + enum classes (`SubmissionEvent`, `SubmissionStatus`, `SubmissionStatusChangedEvent`, `SubmissionStatusPayload`, `TestCaseResultDto`). Package `com.codeloom.common`. No Spring starter dependency; both services depend on `project(':common')`.
- `backend/` — Kotlin + Spring Boot 4 REST API (Spring Data JDBC, OAuth2 resource server, Kafka producer+consumer, Flyway). Entry: `com.codeloom.backend.BackendApplication`. REST under `/v1`, Swagger at `/docs/swagger.html`.
- `executor/` — Kotlin + Spring Boot judge service: Kafka consumer + docker-java. **No REST controllers.** Entry: `com.codeloom.executor.ExecutorApplication`. Default port 8082.
- `frontend/` — React 19 + Vite + TS SPA (MUI, react-router, oidc-client-ts). Package manager is **pnpm**. Auth via Keycloak (realm `codeloom`, client `codeloom-frontend`); OIDC config read from `VITE_*` env vars in `src/auth/keycloak.ts`.

## Infrastructure
- `docker compose up -d` (repo root) starts Keycloak, Postgres, Kafka, Zookeeper, Adminer. `init.sql` creates DBs `codeloom_backend` and `keycloak`.
- Host ports: Keycloak **8080**, Postgres **5433** (not 5432), Kafka **29092**, Adminer **8088**. Keycloak admin: `admin/password`.
- Backend expects a Keycloak realm `codeloom` (issuer-uri hardcoded to `http://localhost:8080/realms/codeloom`) and maps only realm roles prefixed `ROLE_` (use `ADMIN`/`USER`).

## Commands
- backend & executor: `./gradlew :backend:bootRun`, `./gradlew :executor:bootRun`, `./gradlew :backend:test`, `./gradlew :executor:test` (Java 21; Windows: `gradlew.bat`).
- frontend (run inside `frontend/`): `pnpm install`, `pnpm dev`, `pnpm type-check`, `pnpm build` (runs type-check first), `pnpm lint` (oxlint).

## CI
- `.github/workflows/tests.yml` runs on PRs (paths-filtered via `common/**`): `./gradlew :backend:test` and `./gradlew :executor:test` on `ubuntu-latest`, Java 21, `gradle/actions/setup-gradle`. No frontend job.

## Config / env
- backend needs `CODELOOM_DB_URL`, `CODELOOM_DB_USER`, `CODELOOM_DB_PASSWORD`; optional `CODELOOM_PORT` (default 8080).
- executor needs `CODELOOM_EXECUTOR_DB_URL/USER/PASSWORD`, optional `CODELOOM_EXECUTOR_PORT` (default 8082), `CODELOOM_EXECUTOR_DOCKER_HOST` (default `unix:///var/run/docker.sock`).
- Each of `backend/`, `executor/`, and `frontend/` has a **gitignored** `.env` (`common/` does not). Spring does not read `.env` automatically — values must be exported or passed at run time. Vite reads `frontend/.env` automatically; `frontend/.env.example` documents the variables.
- Gotcha: backend's default port (8080) collides with Keycloak on the host; run backend on another port locally.

## Tests
- backend: integration tests in `src/test/kotlin/com/codeloom/backend/it/`. **All** use Testcontainers Postgres (`postgres:18.1-alpine3.23`) via `@ServiceConnection`; `SubmissionStatusConsumerIT` additionally spins up a Confluent Kafka container and publishes to the Kafka topic. `ProblemIT`/`TestCaseIT`/`TopicIT` mock Kafka out. Require a running Docker daemon.
- Backend test config (`src/test/resources/application.yaml`) activates `test` profile and overrides topics to `test-submissions`/`test-submission-statuses`.
- executor: `DockerJudgeEngineTest` and `SubmissionProcessingServiceTest` execute real containers through docker-java and assert container/volume cleanup. Require a working Docker daemon and network access to pull language images (`eclipse-temurin:21-jdk`, `gcc:15`, `python:3.14-slim`).

## Architecture gotchas
- **Jackson 3** (`tools.jackson.*`, not `com.fasterxml.jackson.*`) is used throughout (Spring Boot 4 / root build.gradle). Imports: `tools.jackson.databind.ObjectMapper`, `tools.jackson.core.JacksonException`, `tools.jackson.module.kotlin.jacksonObjectMapper()`. A custom `objectMapper()` bean is defined in `BackendApplication.kt`.
- Flow: **backend publishes** `SubmissionEvent` to topic `submissionRepository` (key = submissionId); **executor consumes**, runs tests, and publishes `SubmissionStatusChangedEvent` to `submission_statuses`; **backend's `SubmissionStatusKafkaListenerService` consumes `submission_statuses`** and updates the row's status + persists `TestCaseResultDto`s (deletes prior results for that submission first). The round-trip is complete — the row no longer stays `PENDING`.
- Kafka event classes and `SubmissionStatus` live in `common/` and are shared by both services; **do not redefine them in `backend/` or `executor/`**.
- Executor's `SubmissionKafkaListenerService.@KafkaListener` still **hardcodes** `topics = ["submissions"]`, `groupId = "codeloom"`, ignoring the configurable topic (`codeloom.kafka.topics.submission`) and the `codeloom-executor` consumer `group-id` in `application.yaml`. Backend's listener uses `@KafkaListener(topics = ["\${codeloom.kafka.submission-status-topic}"])`, which is configurable.
- Languages are defined in `executor/.../languages/LanguageSpec.kt` (java/cpp/python) with docker image + compile/run commands; images are pulled on demand by `DockerImageManager`.
- Judge engine (`DockerJudgeEngine`): one docker volume per submission, containers run with `--network none` + memory limit; exit codes 124 (timeout) and OOM map to `TIME_LIMIT_EXCEEDED` / `MEMORY_LIMIT_EXCEEDED` (see `CodeExecutionConstants.kt`).
- Flyway migrations live in `backend/src/main/resources/db/migration`: `V1_0__create_tables.sql`, `V2_0__rename_test_case_result_columns.sql`.
- Spring Data JDBC JSON-enum converters are registered in `backend/.../config/JdbcConfig.kt` (`ProblemExamples`, `ProblemConstraints`, `ProblemDifficulty`, `SubmissionStatus`).
- Security configs are profile-gated: `SecurityConfig` is `@Profile("!test")`, `TestSecurityConfig` is `@Profile("test")`.