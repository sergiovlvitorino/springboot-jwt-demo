package com.sergiovitorino.springbootjwt.application.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserCommandHandlerTest {

    @Test
    void shouldInstantiateUserCommandHandler() {
        // UserCommandHandler is deprecated but still exists as a component
        // This test ensures it can be instantiated without errors
        var handler = new UserCommandHandler();
        assertNotNull(handler);
    }
}
