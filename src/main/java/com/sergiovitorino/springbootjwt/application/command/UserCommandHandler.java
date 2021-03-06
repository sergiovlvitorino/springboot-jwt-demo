package com.sergiovitorino.springbootjwt.application.command;

import com.sergiovitorino.springbootjwt.application.command.user.CountCommand;
import com.sergiovitorino.springbootjwt.application.command.user.ListCommand;
import com.sergiovitorino.springbootjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.application.command.user.UpdateCommand;
import com.sergiovitorino.springbootjwt.application.service.UserService;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

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
