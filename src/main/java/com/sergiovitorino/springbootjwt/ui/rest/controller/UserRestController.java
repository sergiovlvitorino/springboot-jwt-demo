package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.sergiovitorino.springbootjwt.application.command.user.*;
import com.sergiovitorino.springbootjwt.application.service.UserService;
import com.sergiovitorino.springbootjwt.domain.exception.ResourceNotFoundException;
import com.sergiovitorino.springbootjwt.domain.model.AuthorityConstants;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/rest/user")
@Tag(name = "User Management", description = "Endpoints for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserRestController {

    private static final Logger log = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserRestController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @Operation(summary = "List users", description = "Returns a paginated list of users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You don't have permission to access this resource")
    })
    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETRIEVE + "')")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> get(@Valid ListCommand command) {
        log.debug("GET /rest/user - Listing users: page={}, size={}", command.pageNumber(), command.pageSize());
        Page<User> page = userService.findAll(command.pageNumber(), command.pageSize(), command.orderBy(), command.asc(), command.user() == null ? new User() : command.user());
        return ResponseEntity.ok(page.map(UserResponse::from));
    }

    @Operation(summary = "Count users", description = "Returns the total number of users matching the criteria.")
    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_RETRIEVE + "')")
    @GetMapping("/count")
    public ResponseEntity<Long> get(CountCommand command) {
        log.debug("GET /rest/user/count - Counting users");
        return ResponseEntity.ok(userService.count(command.user() == null ? new User() : command.user()));
    }

    @Operation(summary = "Create a new user", description = "Creates a new user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - E-mail already exists")
    })
    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @PostMapping
    public ResponseEntity<UserResponse> post(@RequestBody @Valid SaveCommand command) {
        log.debug("POST /rest/user - Creating user with email: {}", command.email());
        var user = new User();
        user.setName(command.name());
        user.setEmail(command.email());
        user.setPassword(command.password());
        var role = roleRepository.findById(command.roleId()).orElseThrow(() -> {
            log.warn("POST /rest/user - Role not found: {}", command.roleId());
            return new ResourceNotFoundException("Role not found");
        });
        user.setRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(userService.save(user)));
    }

    @Operation(summary = "Update an existing user", description = "Updates an existing user's name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @PutMapping
    public ResponseEntity<UserResponse> put(@RequestBody @Valid UpdateCommand command) {
        log.debug("PUT /rest/user - Updating user: id={}", command.id());
        var user = new User();
        user.setId(command.id());
        user.setName(command.name());
        return ResponseEntity.ok(UserResponse.from(userService.update(user)));
    }

    @Operation(summary = "Disable a user", description = "Disables a user by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAuthority('" + AuthorityConstants.USER_SAVE + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> delete(@PathVariable UUID id) {
        log.debug("DELETE /rest/user/{} - Disabling user", id);
        return ResponseEntity.ok(UserResponse.from(userService.disable(id)));
    }

}
