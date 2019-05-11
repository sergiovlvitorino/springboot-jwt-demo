package com.sergiovitorino.springbootjwt.ui.command.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class SaveCommand {

    @NotNull(message = "Name not found")
    @Size(min = 2, max = 50, message = "Name should be minimum 2 character and maximum 50 character")
    private String name;

    @NotNull(message = "E-mail not found")
    @Email
    private String email;

    @NotNull(message = "Password not found")
    @Size(min = 6, max = 16, message = "Name should be minimum 6 character and maximum 16 character")
    private String password;

}
