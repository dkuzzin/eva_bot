package ru.eva.server.event;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateEventRequest(
        String title,
        String description,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String location,
        Integer capacity,
        List<FormFieldRequest> formFields
){ }
