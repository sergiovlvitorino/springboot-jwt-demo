package com.sergiovitorino.springbootjwt.ui.controller;

import com.sergiovitorino.springbootjwt.infrastructure.AuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.sergiovitorino.springbootjwt.infrastructure.AbstractController;
import com.sergiovitorino.springbootjwt.infrastructure.DesactiveUUIDCommand;
import com.sergiovitorino.springbootjwt.ui.command.UserCommandHandler;
import com.sergiovitorino.springbootjwt.ui.command.user.CountCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.ListCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.UpdateCommand;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController extends AbstractController {

    @Autowired private UserCommandHandler commandHandler;

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETREAVE + "')")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity get(ListCommand command) {
        return responseBuilder.load(commandHandler.execute(command)).build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETREAVE + "')")
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ResponseEntity get(CountCommand command) {
        return responseBuilder.load(commandHandler.execute(command)).build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity post(@RequestBody @Valid SaveCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return responseBuilder.load(bindingResult).build();
        return responseBuilder.load(commandHandler.execute(command)).build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity put(@RequestBody @Valid UpdateCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return responseBuilder.load(bindingResult).build();
        return responseBuilder.load(commandHandler.execute(command)).build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable(value = "id", required = true) UUID id){
        return responseBuilder.load(commandHandler.execute(new DesactiveUUIDCommand(id))).build();
    }

}