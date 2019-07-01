package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.infrastructure.AbstractController;
import com.sergiovitorino.springbootjwt.infrastructure.AuthorityConstants;
import com.sergiovitorino.springbootjwt.infrastructure.DisableUUIDCommand;
import com.sergiovitorino.springbootjwt.application.command.UserCommandHandler;
import com.sergiovitorino.springbootjwt.application.command.user.CountCommand;
import com.sergiovitorino.springbootjwt.application.command.user.ListCommand;
import com.sergiovitorino.springbootjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.application.command.user.UpdateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/rest/user")
@Validated
public class UserRestController extends AbstractController {

    @Autowired private UserCommandHandler commandHandler;

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETREAVE + "')")
    @RequestMapping(method = RequestMethod.GET)
    public Page<User> get(@Valid ListCommand command) { return commandHandler.execute(command); }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETREAVE + "')")
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long get(CountCommand command) {
        return commandHandler.execute(command);
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @PostMapping
    public User post(@RequestBody @Valid SaveCommand command) {
        return commandHandler.execute(command);
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @PutMapping
    public User put(@RequestBody @Valid UpdateCommand command) {
        return commandHandler.execute(command);
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @DeleteMapping(value = "/{id}")
    public User delete(@Valid DisableUUIDCommand command){
        return commandHandler.execute(command);
    }

}