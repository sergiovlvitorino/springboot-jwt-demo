package com.sergiovitorino.springbootjwt.application.command.user;

import com.sergiovitorino.springbootjwt.domain.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ListCommand(
    @Min(0) Integer pageNumber,
    @Min(1) Integer pageSize,
    @NotBlank String orderBy,
    @NotNull Boolean asc,
    User user
) {
}
