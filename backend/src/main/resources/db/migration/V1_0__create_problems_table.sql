CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "problems"
(
    "problem_id"  UUID       NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    "text"        TEXT       NOT NULL,
    "name"        TEXT       NOT NULL,
    "difficulty"  INTEGER    NOT NULL,
    "constraints" TEXT ARRAY NOT NULL,
    "hints"       TEXT ARRAY NOT NULL,
    PRIMARY KEY ("problem_id")
);



CREATE TABLE IF NOT EXISTS "examples"
(
    "example_id"  UUID    NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    "input"       TEXT    NOT NULL,
    "output"      TEXT    NOT NULL,
    "explanation" TEXT,
    "problem_id"  INTEGER NOT NULL,
    PRIMARY KEY ("example_id")
);
