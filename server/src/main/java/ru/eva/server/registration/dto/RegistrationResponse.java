package ru.eva.server.registration.dto;

import java.time.OffsetDateTime;

public record RegistrationResponse(
        Long id,
        Long eventId,
        OffsetDateTime registeredAt
)
{

}
