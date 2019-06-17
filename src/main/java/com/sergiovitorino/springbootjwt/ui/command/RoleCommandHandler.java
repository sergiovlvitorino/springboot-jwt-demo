package com.sergiovitorino.springbootjwt.ui.command;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.service.RoleService;
import com.sergiovitorino.springbootjwt.ui.command.role.CountCommand;
import com.sergiovitorino.springbootjwt.ui.command.role.ListCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class RoleCommandHandler {

    @Autowired private RoleService service;

    public Page<Role> execute(ListCommand command) {
        return service.findAll(command.getPageNumber(), command.getPageSize(), command.getOrderBy(), command.getAsc(), command.getRole() == null ? new Role() : command.getRole());
    }

    public Long execute(CountCommand command) {
        return service.count(command.getRole() == null ? new Role() : command.getRole());
    }

}
