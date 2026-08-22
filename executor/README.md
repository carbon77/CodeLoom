# Code executor

Microservice that:
1. Consumes submission events from Kafka (`submissionRepository` by default).
2. Loads test cases from PostgreSQL by `problem_id`.
3. Process submission via state machine
4. Publishes updated submission statuses to Kafka (`submission_statuses` by default)

## Judge engine
Service uses [docker-java](https://github.com/docker-java/docker-java) for compiling and executing code in secured environments

## State Machine
```mermaid
stateDiagram-v2
    [*] --> QUEUED
    
    QUEUED --> COMPILING
    COMPILING --> COMPILE_ERROR
    COMPILING --> RUNNING
    RUNNING --> ACCEPTED
    RUNNING --> WRONG_ANSWER
    RUNNING --> TIME_LIMIT_EXCEEDED
    RUNNING --> MEMORY_LIMIT_EXCEEDED
    RUNNING --> RUNTIME_ERROR
    
    COMPILE_ERROR --> [*]
    ACCEPTED --> [*]
    WRONG_ANSWER --> [*]
    TIME_LIMIT_EXCEEDED --> [*]
    MEMORY_LIMIT_EXCEEDED --> [*]
    RUNTIME_ERROR --> [*]
```

## Event contract

### Incoming (`submissionRepository`)
```json
{
  "submissionId": "uuid",
  "problemId": 1,
  "userId": "uuid",
  "language": "java|cpp|python",
  "code": "...",
  "timeLimitMs": 10000,
  "memoryLimitMb": 5
}
```

### Outgoing (`submission_statuses`)
```json
{
  "submissionId": "uuid",
  "problem_id": 1,
  "userId": "uuid",
  "new_status": "<submission_status>",
  "payload": {}
}
```

