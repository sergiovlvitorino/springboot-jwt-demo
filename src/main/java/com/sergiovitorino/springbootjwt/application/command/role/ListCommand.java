package com.sergiovitorino.springbootjwt.application.command.role;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ListCommand {

    @Min(0)
    private Integer pageNumber;

    @Min(1)
    private Integer pageSize;

    @NotBlank
    private String orderBy;

    @NotNull
    private Boolean asc;

    private Role role;
}
