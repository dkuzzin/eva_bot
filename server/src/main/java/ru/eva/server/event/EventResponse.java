package ru.eva.server.event;

import java.time.OffsetDateTime;
import java.util.List;

public record EventResponse(
        Long id,
        String title,
        String description,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String location,
        Integer capacity,
        EventStatus status,
        List<FormField> formFields
) {
    public record FormField(
            Long id,
            String label,
            Integer position
    ) {
    }
}