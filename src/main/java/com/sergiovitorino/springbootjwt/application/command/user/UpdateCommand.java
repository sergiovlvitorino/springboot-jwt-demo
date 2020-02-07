package com.sergiovitorino.springbootjwt.application.command.user;

import com.sergiovitorino.springbootjwt.infrastructure.validations.SafeHtml;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class UpdateCommand {

    @NotNull(message = "Id not found")
    private UUID id;

    @NotNull(message = "Name not found")
    @Size(min = 2, max = 50, message = "Name should be minimum 2 character and maximum 50 character")
    @SafeHtml
    private String name;

}
