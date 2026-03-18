package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.application.command.user.UpdateCommand;
import com.sergiovitorino.springbootjwt.application.command.user.UserResponse;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springbootjwt.infrastructure.ErrorBean;
import com.sergiovitorino.springbootjwt.infrastructure.util.LoginHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRestControllerTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private TestRestTemplate restTemplete;
    @LocalServerPort
    private Integer port;
    @Autowired
    private RoleRepository roleRepository;
    private static HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        if (headers == null)
            headers = new LoginHelper().createAuthenticatedHeader(restTemplete, port);
    }

    @Test
    public void testIfListCommandReturnsBadRequestWhenPageNumberIsMinusOne() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=-1&pageSize=10000&orderBy=name&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsBadRequestWhenPageSizeIsMinusOne() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=1&pageSize=-1&orderBy=name&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsBadRequestWhenOrderByIsEmpty() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=1&pageSize=10000&orderBy=&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsBadRequestWhenAscIsInvalid() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=1&pageSize=10000&orderBy=name&asc=aaa&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }


    @Test
    public void testIfListCommandReturnsOk() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        assertTrue(jsonObject.has("content"));
        final List<UserResponse> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, UserResponse.class));
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsOk2() throws Exception {
        createUser();
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=false&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final var jsonObject = new JSONObject(responseEntity.getBody());
        assertTrue(jsonObject.has("content"));
        final List<UserResponse> list = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, UserResponse.class));
        assertNotNull(list);
    }

    @Test
    public void testIfListCommandReturnsOk3() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        assertTrue(jsonObject.has("content"));
        final List<UserResponse> list = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, UserResponse.class));
        assertNotNull(list);
    }

    @Test
    public void testIfSaveCommandReturnsOk() throws Exception {
        final var role = roleRepository.findAll().get(0);
        final var command = new SaveCommand(UUID.randomUUID().toString(), "savecommand@command.com", "Test@1234", role.getId());

        final var entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        final var userCreated = mapper.readValue(responseEntity.getBody(), UserResponse.class);
        assertNotNull(userCreated);
        assertNotNull(userCreated.id());
        assertEquals(command.name(), userCreated.name());
        // Ensure password is NOT in response
        assertFalse(responseEntity.getBody().contains("password"));
    }

    @Test
    public void testIfSaveCommandReturnsBadRequest() throws Exception {
        final var role = roleRepository.findAll().get(0);
        final var command = new SaveCommand(null, "savecommand@command.com", "Test@1234", role.getId());

        final var entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandReturnsBadRequest2() throws Exception {
        final var role = roleRepository.findAll().get(0);
        final var command = new SaveCommand("<html>lorem ipsum</html>", "savecommand@command.com", "<html>lorem ipsum</html>", role.getId());

        final var entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandReturnsBadRequestWhenEmailAlready() throws Exception {
        final var role = roleRepository.findAll().stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Role not found"));
        final var command = new SaveCommand(UUID.randomUUID().toString(), "email_already@command.com", "Test@1234", role.getId());

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        final String exceptionMessageExpected = "E-mail already";
        final String exceptionMessageActual = errors.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("ErrorBean not found")).message();
        assertEquals(exceptionMessageExpected, exceptionMessageActual);
    }

    @Test
    public void testIfUpdateCommandReturnsOk() throws Exception {
        final UserResponse userSaved = createUser();
        final UpdateCommand command = new UpdateCommand(userSaved.id(), UUID.randomUUID().toString());

        final var entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final UserResponse userUpdated = mapper.readValue(responseEntity.getBody(), UserResponse.class);
        assertNotNull(userUpdated);
        assertNotNull(userUpdated.id());
        assertEquals(userSaved.id(), userUpdated.id());
        assertNotEquals(userSaved.name(), userUpdated.name());
    }

    @Test
    public void testIfUpdateCommandReturnsBadRequest() throws Exception {
        final UserResponse userSaved = createUser();
        final UpdateCommand command = new UpdateCommand(userSaved.id(), null);

        final var entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfUpdateCommandReturnsBadRequest2() throws Exception {
        final UpdateCommand command = new UpdateCommand(UUID.randomUUID(), UUID.randomUUID().toString());
        final var entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testIfDisableUUIDCommandReturnsOk() throws Exception {
        final UserResponse userSaved = createUser();
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/" + userSaved.id().toString(), HttpMethod.DELETE, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final UserResponse userDisabled = mapper.readValue(responseEntity.getBody(), UserResponse.class);
        assertNotNull(userDisabled);
        assertNotNull(userDisabled.id());
        assertEquals(userSaved.id(), userDisabled.id());
        assertFalse(userDisabled.enabled());
    }

    @Test
    public void testIfDisableUUIDCommandReturnsBadRequest() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/aaa", HttpMethod.DELETE, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfDisableUUIDCommandReturnsNotFound() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/" + UUID.randomUUID().toString(), HttpMethod.DELETE, entity, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testIfCountCommandReturnsOk() throws Exception {
        final var entity = new HttpEntity<String>(null, headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/count?user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        final Long countActual = Long.valueOf(responseEntity.getBody());
        assertTrue(countActual > 0);
    }

    @Test
    public void testIfCountCommandReturnsOk2() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/count", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testIfResponseDoesNotContainInternalClassNames() throws Exception {
        // Attempt to create user with duplicate email to trigger error handler
        final var role = roleRepository.findAll().get(0);
        final var command = new SaveCommand(UUID.randomUUID().toString(), "classname_test@command.com", "Test@1234", role.getId());

        var entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        // Second attempt — same email
        entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        // Error response should NOT contain internal class names
        assertFalse(responseEntity.getBody().contains("com.sergiovitorino"));
        assertFalse(responseEntity.getBody().contains("Exception"));
    }

    @Test
    public void testIfActuatorHealthIsAccessibleWithoutAuth() {
        var responseEntity = this.restTemplete.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testIfActuatorEnvIsNotAccessibleWithoutAuth() {
        var responseEntity = this.restTemplete.getForEntity("http://localhost:" + port + "/actuator/env", String.class);
        assertNotEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private UserResponse createUser() throws Exception {
        Role role = roleRepository.findAll().get(0);
        SaveCommand command = new SaveCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString() + "@command.com", "Test@1234", role.getId());

        HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            return mapper.readValue(responseEntity.getBody(), UserResponse.class);
        } else {
            throw new RuntimeException("Failed to create user: " + responseEntity.getBody());
        }
    }

}
