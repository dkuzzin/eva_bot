CREATE TABLE events
(
    id                BIGSERIAL PRIMARY KEY,
    owner_max_user_id BIGINT       NOT NULL,

    title             VARCHAR(255) NOT NULL,
    description       TEXT,

    starts_at         TIMESTAMPTZ  NOT NULL,
    ends_at           TIMESTAMPTZ,

    location          VARCHAR(255),
    capacity          INTEGER,

    status            VARCHAR(32)  NOT NULL,

    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);

CREATE TABLE form_fields
(
    id       BIGSERIAL PRIMARY KEY,
    event_id BIGINT       NOT NULL,
    label    VARCHAR(255) NOT NULL,
    position INTEGER      NOT NULL,

    CONSTRAINT fk_form_fields_event
        FOREIGN KEY (event_id)
            REFERENCES events(id)
            ON DELETE CASCADE
);