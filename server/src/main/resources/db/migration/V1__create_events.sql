CREATE TABLE events
(
    id                BIGSERIAL PRIMARY KEY,
    owner_max_user_id BIGINT NOT NULL,

    title             VARCHAR(255) NOT NULL,
    description       TEXT,

    starts_at         TIMESTAMPTZ NOT NULL,
    ends_at           TIMESTAMPTZ NOT NULL,

    location          VARCHAR(255),
    capacity          INTEGER,

    status            VARCHAR(32) NOT NULL,

    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);