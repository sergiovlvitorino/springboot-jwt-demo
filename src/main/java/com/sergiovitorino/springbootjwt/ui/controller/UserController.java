package com.sergiovitorino.springbootjwt.ui.controller;

import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.infrastructure.AuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.sergiovitorino.springbootjwt.infrastructure.AbstractController;
import com.sergiovitorino.springbootjwt.infrastructure.DisableUUIDCommand;
import com.sergiovitorino.springbootjwt.ui.command.UserCommandHandler;
import com.sergiovitorino.springbootjwt.ui.command.user.CountCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.ListCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.UpdateCommand;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Validated
public class UserController extends AbstractController {

    @Autowired private UserCommandHandler commandHandler;

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETREAVE + "')")
    @RequestMapping(method = RequestMethod.GET)
    public Page<User> get(@Valid ListCommand command) {
        return commandHandler.execute(command);
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETREAVE + "')")
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long get(@Valid CountCommand command) {
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