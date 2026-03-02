package com.sergiovitorino.springbootjwt.infrastructure;

import com.sergiovitorino.springbootjwt.domain.model.Authority;
import com.sergiovitorino.springbootjwt.domain.model.AuthorityConstants;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.AuthorityRepository;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Component
public class Initialize implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initialize.class);

    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    public Initialize(AuthorityRepository authorityRepository,
                     RoleRepository roleRepository,
                     PasswordEncoder passwordEncoder,
                     UserRepository userRepository,
                     TransactionTemplate transactionTemplate) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        transactionTemplate.executeWithoutResult(status -> {
            if (userRepository.count() > 0) {
                log.info("Database already initialized, skipping seed data");
                return;
            }

            log.info("Initializing database with seed data...");

            authorityRepository.saveAndFlush(new Authority(AuthorityConstants.USER_RETRIEVE));
            authorityRepository.saveAndFlush(new Authority(AuthorityConstants.ROLE_RETRIEVE));
            roleRepository.saveAndFlush(new Role("GUEST", authorityRepository.findAll()));
            log.debug("Created GUEST role with authorities: {}, {}", AuthorityConstants.USER_RETRIEVE, AuthorityConstants.ROLE_RETRIEVE);

            authorityRepository.saveAndFlush(new Authority(AuthorityConstants.USER_SAVE));
            Role adminRole = roleRepository.saveAndFlush(new Role("ADMIN", authorityRepository.findAll()));
            log.debug("Created ADMIN role with all authorities");

            User user = new User();
            user.setName("Lorem Ipsum");
            user.setEmail("abc@def.com");
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode("Test@1234"));
            user.setRole(adminRole);
            user.setUserIdCreatedAt(UUID.randomUUID());

            userRepository.saveAndFlush(user);
            log.info("Database initialization completed: created {} authorities, {} roles, {} admin user",
                authorityRepository.count(), roleRepository.count(), userRepository.count());
        });
    }
}
