package ru.eva.server.registration.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record EventRegistrationsResponse(
        Long eventId,
        List<FormField> formFields,
        List<Registration> registrations
) {
    public record FormField(
            Long id,
            String label,
            Integer position
    ) {}

    public record Registration(
            Long id,
            OffsetDateTime registeredAt,
            List<Answer> answers
    ) {}

    public record Answer(
            Long fieldId,
            String value
    ) {}
}