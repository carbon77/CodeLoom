# Code executor

Microservice that:
1. Consumes submission events from Kafka (`submissions` by default).
2. Loads test cases from PostgreSQL by `problem_id`.
3. Executes code in Docker for Java, C++, Python, and Go.
4. Publishes processing statuses to Kafka (`submission_changed` by default).

## Architecture

- `CodeExecutionService` is an isolated, reusable code execution API.
- `DockerCodeExecutionService` is the Docker-backed implementation.
- `SubmissionProcessingService` handles only submission workflow orchestration.
- Spring MVC stack is used (`spring-boot-starter-web`) with JDBC (`JdbcTemplate`) for database access.

## Event contract

### Incoming (`submissions`)
```json
{
  "submissionId": "uuid",
  "problemId": 1,
  "userId": "uuid",
  "language": "java|cpp|python|go",
  "code": "..."
}
```

### Outgoing (`submission_changed`)
`RUNNING`, then final one of `ACCEPTED`, `WRONG_ANSWER`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`, `SYSTEM_ERROR`.

Each test-case result includes execution time and memory used (`bytesUsed`) when available from the runtime container.
