package com.sergiovitorino.springbootjwt.application.command.user;

import com.sergiovitorino.springbootjwt.domain.model.User;
import lombok.Data;

@Data
public class CountCommand {

    private User user;

}
