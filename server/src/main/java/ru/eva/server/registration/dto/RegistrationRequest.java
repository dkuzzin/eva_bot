package ru.eva.server.registration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RegistrationRequest(
        @NotNull @Valid List<Answer> answers
) {
    public record Answer(
            @NotNull @Positive Long fieldId,
            @NotBlank String value
    ) {
    }
}