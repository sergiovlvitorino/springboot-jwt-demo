package com.sergiovitorino.springbootjwt.application.command.user;

import com.sergiovitorino.springbootjwt.infrastructure.validations.SafeHtml;
import com.sergiovitorino.springbootjwt.infrastructure.validations.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SaveCommand(
    @NotNull
    @Size(min = 2, max = 50, message = "Name should be minimum 2 character and maximum 50 character")
    @SafeHtml
    String name,

    @NotNull(message = "E-mail not found")
    @Email
    String email,

    @NotNull(message = "Password not found")
    @Size(min = 8, max = 100, message = "Password should be minimum 8 characters and maximum 100 characters")
    @StrongPassword
    String password,

    @NotNull(message = "RoleId not found")
    UUID roleId
) {
}
