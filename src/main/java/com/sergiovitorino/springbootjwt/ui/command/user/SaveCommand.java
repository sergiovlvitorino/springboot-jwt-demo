package com.sergiovitorino.springbootjwt.ui.command.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveCommand {

    @NotNull
    @Size(min = 2, max = 50, message = "Name should be minimum 2 character and maximum 50 character")
    private String name;

    @NotNull(message = "E-mail not found")
    @Email
    private String email;

    @NotNull(message = "Password not found")
    @Size(min = 6, max = 16, message = "Name should be minimum 6 character and maximum 16 character")
    private String password;

    @NotNull(message = "RoleId not found")
    private UUID roleId;

}
