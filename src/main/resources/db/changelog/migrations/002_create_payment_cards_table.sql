--liquibase formatted sql

--changeset dev:002-create-payment-cards-table
CREATE TABLE payment_cards
(
    id              UUID         DEFAULT gen_random_uuid() NOT NULL,
    user_id         UUID                                   NOT NULL,
    number          VARCHAR(19)                            NOT NULL,
    holder          VARCHAR(255)                           NOT NULL,
    expiration_date DATE                                   NOT NULL,
    active          BOOLEAN      DEFAULT true              NOT NULL,
    created_at      TIMESTAMP                              NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP                              NOT NULL DEFAULT now(),

    CONSTRAINT pk_payment_cards PRIMARY KEY (id),
    CONSTRAINT uq_payment_cards_number UNIQUE (number),
    CONSTRAINT fk_payment_cards_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

--changeset dev:002-create-payment-cards-indexes
CREATE INDEX idx_payment_cards_user_id ON payment_cards (user_id);
CREATE INDEX idx_payment_cards_number ON payment_cards (number);
CREATE INDEX idx_payment_cards_user_id_active ON payment_cards (user_id, active);