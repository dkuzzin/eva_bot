package ru.eva.server.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateEventRequest(
        @NotBlank String title,
        String description,
        @NotNull OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String location,
        @Positive Integer capacity,
        @NotNull @Valid List<FormFieldRequest> formFields
){ }
