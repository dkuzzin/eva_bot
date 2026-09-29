package ru.eva.server.registration.dto;

import ru.eva.server.event.model.EventStatus;

import java.time.OffsetDateTime;

public record MyRegistrationResponse(
        Long registrationId,
        OffsetDateTime registeredAt,
        Event event
){
    public record Event(
            Long id,
            String title,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            String location,
            EventStatus status
    ){
    }
}
