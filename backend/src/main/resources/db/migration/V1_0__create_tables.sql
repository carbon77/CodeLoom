CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE problem_difficulty AS ENUM ('EASY', 'MEDIUM', 'HARD');

CREATE TABLE IF NOT EXISTS "problems"
(
    "problem_id"  SERIAL PRIMARY KEY,
    "slug"        TEXT               NOT NULL,
    "title"       TEXT               NOT NULL,
    "description" TEXT               NOT NULL,
    "hints"       TEXT ARRAY         NOT NULL,
    "difficulty"  problem_difficulty NOT NULL,
    "constraints" JSONB,
    "examples"    JSONB,
    created_at    TIMESTAMP          NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP          NOT NULL DEFAULT now(),
    published_at  TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS unique_problem_slug_idx ON problems (slug);

CREATE TABLE IF NOT EXISTS "test_cases"
(
    "test_case_id"    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "problem_id"      INTEGER NOT NULL,
    "input"           TEXT    NOT NULL,
    "expected_output" TEXT    NOT NULL,
    "is_public"       BOOLEAN NOT NULL,

    FOREIGN KEY ("problem_id") REFERENCES problems ("problem_id")
);

CREATE TABLE IF NOT EXISTS "topics"
(
    "topic_id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name"     TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "problem_topics"
(
    "topic_id"   UUID    NOT NULL,
    "problem_id" INTEGER NOT NULL,
    PRIMARY KEY ("topic_id", "problem_id"),
    FOREIGN KEY ("topic_id") REFERENCES topics ("topic_id"),
    FOREIGN KEY ("problem_id") REFERENCES problems ("problem_id")
);

CREATE TABLE IF NOT EXISTS "submissions"
(
    "submission_id" UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    "user_id"       UUID      NOT NULL,
    "problem_id"    INTEGER   NOT NULL,
    "code"          TEXT      NOT NULL,
    "status"        TEXT      NOT NULL,
    "language"      TEXT      NOT NULL,
    "created_at"    TIMESTAMP NOT NULL DEFAULT now(),

    FOREIGN KEY ("problem_id") REFERENCES problems ("problem_id")
);

CREATE TABLE IF NOT EXISTS "test_case_results"
(
    "test_case_result_id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "submission_id"       UUID NOT NULL,
    "input"               TEXT NOT NULL,
    "expected_output"     TEXT NOT NULL,
    "actual_output"       TEXT NOT NULL,
    "error_message"       TEXT,
    "execution_time_ms"   INTEGER,
    "bytes_used"          INTEGER,

    FOREIGN KEY ("submission_id") REFERENCES submissions ("submission_id")
);
