package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.sergiovitorino.springbootjwt.application.command.RoleCommandHandler;
import com.sergiovitorino.springbootjwt.application.command.role.CountCommand;
import com.sergiovitorino.springbootjwt.application.command.role.ListCommand;
import com.sergiovitorino.springbootjwt.domain.model.AuthorityConstants;
import com.sergiovitorino.springbootjwt.infrastructure.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/rest/role")
public class RoleRestController {

    @Autowired private RoleCommandHandler commandHandler;
    @Autowired private ResponseEntityBuilder responseEntityBuilder;

    @PreAuthorize("hasAuthority('" + AuthorityConstants.ROLE_RETRIEVE + "')")
    @GetMapping
    public ResponseEntity get(@Valid ListCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return responseEntityBuilder.bindingResult(bindingResult).build();
        return responseEntityBuilder.result(commandHandler.execute(command)).build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.ROLE_RETRIEVE + "')")
    @GetMapping("/count")
    public ResponseEntity get(CountCommand command) {
        return responseEntityBuilder.result(commandHandler.execute(command)).build();
    }

}
