package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.sergiovitorino.springbootjwt.application.command.role.CountCommand;
import com.sergiovitorino.springbootjwt.application.command.role.ListCommand;
import com.sergiovitorino.springbootjwt.application.service.RoleService;
import com.sergiovitorino.springbootjwt.domain.model.AuthorityConstants;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/role")
public class RoleRestController {

    private final RoleService roleService;

    public RoleRestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.ROLE_RETRIEVE + "')")
    @GetMapping
    public ResponseEntity<Page<Role>> get(@Valid ListCommand command) {
        return ResponseEntity.ok(roleService.findAll(command.pageNumber(), command.pageSize(), command.orderBy(), command.asc(), command.role() == null ? new Role() : command.role()));
    }

    @PreAuthorize("hasAuthority('" + AuthorityConstants.ROLE_RETRIEVE + "')")
    @GetMapping("/count")
    public ResponseEntity<Long> get(CountCommand command) {
        return ResponseEntity.ok(roleService.count(command.role() == null ? new Role() : command.role()));
    }

}
