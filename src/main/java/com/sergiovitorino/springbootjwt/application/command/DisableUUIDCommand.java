package com.sergiovitorino.springbootjwt.application.command;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DisableUUIDCommand(@NotNull(message = "Id not found") UUID id) {
}
