--liquibase formatted sql

--changeset dev:001-create-users-table
CREATE TABLE users
(
    id         UUID         DEFAULT gen_random_uuid() NOT NULL,
    name       VARCHAR(100)                           NOT NULL,
    surname    VARCHAR(100)                           NOT NULL,
    birth_date DATE,
    email      VARCHAR(255)                           NOT NULL,
    active     BOOLEAN      DEFAULT true              NOT NULL,
    created_at TIMESTAMP                              NOT NULL DEFAULT now(),
    updated_at TIMESTAMP                              NOT NULL DEFAULT now(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

--changeset dev:001-create-users-indexes
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_active ON users (active);
CREATE INDEX idx_users_surname_name ON users (surname, name);