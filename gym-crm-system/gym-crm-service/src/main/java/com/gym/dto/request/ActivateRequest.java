package com.gym.dto.request;

import jakarta.validation.constraints.NotNull;

public record ActivateRequest(
        @NotNull(message = "Activeness status is required") Boolean isActive
) {
}
