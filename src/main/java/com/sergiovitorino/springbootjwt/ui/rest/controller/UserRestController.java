package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.sergiovitorino.springbootjwt.application.command.DisableUUIDCommand;
import com.sergiovitorino.springbootjwt.application.command.UserCommandHandler;
import com.sergiovitorino.springbootjwt.application.command.user.CountCommand;
import com.sergiovitorino.springbootjwt.application.command.user.ListCommand;
import com.sergiovitorino.springbootjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.application.command.user.UpdateCommand;
import com.sergiovitorino.springbootjwt.domain.model.AuthorityConstants;
import com.sergiovitorino.springbootjwt.infrastructure.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/rest/user")
public class UserRestController {

    @Autowired
    private UserCommandHandler commandHandler;
    @Autowired
    private ResponseEntityBuilder responseEntityBuilder;

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETRIEVE + "')")
    @GetMapping
    public ResponseEntity get(@Valid ListCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return responseEntityBuilder.bindingResult(bindingResult).build();
        return responseEntityBuilder.result(commandHandler.execute(command))
                .httpStatusError(HttpStatus.NOT_FOUND)
                .build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETRIEVE + "')")
    @GetMapping("/count")
    public ResponseEntity get(CountCommand command) {
        return responseEntityBuilder
                .result(commandHandler.execute(command))
                .httpStatusError(HttpStatus.NOT_FOUND)
                .build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @PostMapping
    public ResponseEntity post(@RequestBody @Valid SaveCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return responseEntityBuilder.bindingResult(bindingResult).build();

        return responseEntityBuilder
                .result(commandHandler.execute(command))
                .httpStatusError(HttpStatus.UNPROCESSABLE_ENTITY)
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @PutMapping
    public ResponseEntity put(@RequestBody @Valid UpdateCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return responseEntityBuilder.bindingResult(bindingResult).build();
        return responseEntityBuilder
                .result(commandHandler.execute(command))
                .httpStatusError(HttpStatus.CONFLICT)
                .build();
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity delete(@Valid DisableUUIDCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return responseEntityBuilder.bindingResult(bindingResult).build();
        return responseEntityBuilder
                .result(commandHandler.execute(command))
                .httpStatusError(HttpStatus.NOT_FOUND)
                .build();
    }

}