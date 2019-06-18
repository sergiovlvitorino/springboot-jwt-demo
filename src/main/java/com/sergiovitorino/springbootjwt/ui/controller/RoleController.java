package com.sergiovitorino.springbootjwt.ui.controller;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.infrastructure.AbstractController;
import com.sergiovitorino.springbootjwt.infrastructure.AuthorityConstants;
import com.sergiovitorino.springbootjwt.ui.command.RoleCommandHandler;
import com.sergiovitorino.springbootjwt.ui.command.role.CountCommand;
import com.sergiovitorino.springbootjwt.ui.command.role.ListCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/role")
@Validated
public class RoleController extends AbstractController {

    @Autowired private RoleCommandHandler commandHandler;

    @PreAuthorize("hasAuthority('" + AuthorityConstants.ROLE_RETREAVE + "')")
    @GetMapping
    public Page<Role> get(@Valid ListCommand command) {
        return commandHandler.execute(command);
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.ROLE_RETREAVE + "')")
    @GetMapping("/count")
    public Long get(CountCommand command) {
        return commandHandler.execute(command);
    }

}