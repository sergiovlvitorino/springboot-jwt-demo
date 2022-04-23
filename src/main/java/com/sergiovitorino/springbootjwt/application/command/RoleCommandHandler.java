package com.sergiovitorino.springbootjwt.application.command;

import com.sergiovitorino.springbootjwt.application.command.role.CountCommand;
import com.sergiovitorino.springbootjwt.application.command.role.ListCommand;
import com.sergiovitorino.springbootjwt.application.service.RoleService;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class RoleCommandHandler {

    @Autowired
    private RoleService service;

    public Page<Role> execute(ListCommand command) {
        return service.findAll(command.getPageNumber(), command.getPageSize(), command.getOrderBy(), command.getAsc(), command.getRole() == null ? new Role() : command.getRole());
    }

    public Long execute(CountCommand command) {
        return service.count(command.getRole() == null ? new Role() : command.getRole());
    }

}
