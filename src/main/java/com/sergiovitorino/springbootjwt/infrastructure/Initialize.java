package com.sergiovitorino.springbootjwt.infrastructure;

import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.sergiovitorino.springbootjwt.domain.model.Authority;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.AuthorityRepository;

import javax.annotation.PostConstruct;

@Component
public class Initialize {

    @Autowired private AuthorityRepository authorityRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;

    @PostConstruct
    public void execute(){

        authorityRepository.save(new Authority(AuthorityConstants.USER_SAVE));
        authorityRepository.save(new Authority(AuthorityConstants.USER_RETREAVE));

        final Role role = roleRepository.save(new Role("ADMIN", authorityRepository.findAll()));

        final User user = new User();
        user.setName("Sérgio Vitorino");
        user.setEmail("sergio@gmail.com");
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(role);
        userRepository.save(user);

    }

}
