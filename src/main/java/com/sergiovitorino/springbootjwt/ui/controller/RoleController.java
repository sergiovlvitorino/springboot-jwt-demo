package com.sergiovitorino.springbootjwt.ui.controller;

import com.sergiovitorino.springbootjwt.infrastructure.AbstractController;
import com.sergiovitorino.springbootjwt.infrastructure.AuthorityConstants;
import com.sergiovitorino.springbootjwt.ui.command.role.ListCommand;
import com.sergiovitorino.springbootjwt.ui.command.RoleCommandHandler;
import com.sergiovitorino.springbootjwt.ui.command.role.CountCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
public class RoleController extends AbstractController {

    @Autowired private RoleCommandHandler commandHandler;

    @PreAuthorize("hasAuthority('" + AuthorityConstants.ROLE_RETREAVE + "')")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity get(ListCommand command) {
        return responseBuilder.load(commandHandler.execute(command)).build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.ROLE_RETREAVE + "')")
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ResponseEntity get(CountCommand command) {
        return responseBuilder.load(commandHandler.execute(command)).build();
    }

}
