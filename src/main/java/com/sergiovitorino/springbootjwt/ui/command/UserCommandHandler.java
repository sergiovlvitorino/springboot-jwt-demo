package com.sergiovitorino.springbootjwt.ui.command;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.service.UserService;
import com.sergiovitorino.springbootjwt.ui.command.user.ListCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import com.sergiovitorino.springbootjwt.infrastructure.DisableUUIDCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.CountCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.ui.command.user.UpdateCommand;

@Component
public class UserCommandHandler {

    @Autowired
    private UserService service;

    public Page<User> execute(ListCommand command) {
        return service.findAll(command.getPageNumber(), command.getPageSize(), command.getOrderBy(), command.getAsc(), command.getUser() == null ? new User() : command.getUser());
    }

    public Long execute(CountCommand command) {
        return service.count(command.getUser() == null ? new User() : command.getUser());
    }

    public User execute(SaveCommand command) {
        User user = new User();
        user.setName(command.getName());
        user.setEmail(command.getEmail());
        user.setPassword(command.getPassword());
        user.setRole(new Role(command.getRoleId()));
        return service.save(user);
    }

    public User execute(UpdateCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setName(command.getName());
        return service.update(user);
    }


    public User execute(DisableUUIDCommand command) {
        return service.disable(command.getId());
    }
}
