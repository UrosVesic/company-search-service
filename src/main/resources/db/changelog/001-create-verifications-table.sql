--liquibase formatted sql

--changeset company-search-service:001-create-verifications-table
CREATE TABLE verifications (
    id              UUID DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    verification_id UUID NOT NULL UNIQUE,
    query_text      VARCHAR(255) NOT NULL,
    timestamp       TIMESTAMPTZ NOT NULL,
    result          TEXT NOT NULL,
    source          VARCHAR(50) NOT NULL
);
