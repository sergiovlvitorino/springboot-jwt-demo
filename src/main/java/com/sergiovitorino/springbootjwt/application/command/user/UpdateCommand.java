package com.sergiovitorino.springbootjwt.application.command.user;

import com.sergiovitorino.springbootjwt.infrastructure.validations.SafeHtml;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateCommand(
    @NotNull(message = "Id not found") UUID id,
    @NotNull(message = "Name not found")
    @Size(min = 2, max = 50, message = "Name should be minimum 2 character and maximum 50 character")
    @SafeHtml String name
) {
}
