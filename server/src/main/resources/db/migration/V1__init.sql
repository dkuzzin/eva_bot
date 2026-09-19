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

CREATE TABLE registrations(
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    max_user_id BIGINT NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,

    CONSTRAINT fk_registrations_event
        FOREIGN KEY (event_id)
            REFERENCES events(id)
            ON DELETE CASCADE,

    CONSTRAINT uq_registration_user_event
        UNIQUE (event_id, max_user_id)
);

CREATE TABLE registrations_answer(
    id BIGSERIAL PRIMARY KEY,
    registration_id BIGINT NOT NULL,
    form_field_id BIGINT NOT NULL,
    value TEXT NOT NULL,

    CONSTRAINT fk_registration
        FOREIGN KEY (registration_id)
            REFERENCES registrations(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_form_field
        FOREIGN KEY (form_field_id)
            REFERENCES form_fields(id)
                ON DELETE CASCADE,
    CONSTRAINT uq_registration_field
        UNIQUE (registration_id, form_field_id)
);