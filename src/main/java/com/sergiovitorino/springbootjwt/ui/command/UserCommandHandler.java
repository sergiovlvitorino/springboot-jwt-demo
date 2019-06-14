package com.sergiovitorino.springbootjwt.ui.command;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.service.UserService;
import com.sergiovitorino.springbootjwt.ui.command.user.ListCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.sergiovitorino.springbootjwt.infrastructure.DisableUUIDCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.CountCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.UpdateCommand;

@Component
public class UserCommandHandler {

    @Autowired
    private UserService service;

    public Object execute(ListCommand command) {
        return service.findAll(command.getPageNumber(), command.getPageSize(), command.getOrderBy(), command.getAsc(), command.getUser() == null ? new User() : command.getUser());
    }

    public Object execute(CountCommand command) {
        return service.count(command.getUser() == null ? new User() : command.getUser());
    }

    public Object execute(SaveCommand command) {
        User user = new User();
        user.setName(command.getName());
        user.setEmail(command.getEmail());
        user.setPassword(command.getPassword());
        user.setRole(new Role(command.getRoleId()));
        return service.save(user);
    }

    public Object execute(UpdateCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setName(command.getName());
        return service.update(user);
    }


    public Object execute(DisableUUIDCommand command) {
        return service.disable(command.getId());
    }
}
