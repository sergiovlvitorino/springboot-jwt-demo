package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.sergiovitorino.springbootjwt.application.command.RoleCommandHandler;
import com.sergiovitorino.springbootjwt.application.command.role.CountCommand;
import com.sergiovitorino.springbootjwt.application.command.role.ListCommand;
import com.sergiovitorino.springbootjwt.domain.model.AuthorityConstants;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/rest/role")
@Validated
public class RoleRestController extends AbstractRestController {

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
