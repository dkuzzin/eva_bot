package ru.eva.server.event.dto;

import jakarta.validation.constraints.NotBlank;

public record FormFieldRequest(
        @NotBlank String label
) {
}