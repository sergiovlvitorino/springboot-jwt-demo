package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springbootjwt.infrastructure.ErrorBean;
import com.sergiovitorino.springbootjwt.application.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.application.command.user.UpdateCommand;
import com.sergiovitorino.springbootjwt.infrastructure.util.LoginHelper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRestControllerTest {

    @Autowired private ObjectMapper mapper;
    @Autowired private TestRestTemplate restTemplete;
    @LocalServerPort private Integer port;
    @Autowired private RoleRepository roleRepository;
    private static HttpHeaders headers;

    @Before
    public void setUp() {
        if (headers == null)
            headers = new LoginHelper().createAuthenticatedHeader(restTemplete, port);
    }

    @Test
    public void testIfListCommandReturnsBadRequestWhenPageNumberIsMinusOne() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=-1&pageSize=10000&orderBy=name&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST,responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsBadRequestWhenPageSizeIsMinusOne() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=1&pageSize=-1&orderBy=name&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST,responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }
    @Test
    public void testIfListCommandReturnsBadRequestWhenOrderByIsEmpty() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=1&pageSize=10000&orderBy=&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST,responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsBadRequestWhenAscIsInvalid() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=1&pageSize=10000&orderBy=name&asc=aaa&user.enabled=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST,responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }


    @Test
    public void testIfListCommandReturnsOk() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsOk2() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=false&user.enabled=true", HttpMethod.GET, entity, String.class);
        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> list = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsOk3() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true", HttpMethod.GET, entity, String.class);
        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> list = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(list);
    }

    @Test
    public void testIfSaveCommandReturnsOk() throws Exception {
        final Role role = roleRepository.findAll().get(0);
        final SaveCommand command = new SaveCommand();
        command.setEmail("savecommand@command.com");
        command.setName(UUID.randomUUID().toString());
        command.setPassword("123456");
        command.setRoleId(role.getId());

        final HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        final User userCreated = mapper.readValue(responseEntity.getBody(), User.class);
        assertNotNull(userCreated);
        assertNotNull(userCreated.getId());
        assertEquals(command.getName(), userCreated.getName());
    }

    @Test
    public void testIfSaveCommandReturnsBadRequest() throws Exception {
        final Role role = roleRepository.findAll().get(0);
        final SaveCommand command = new SaveCommand();
        command.setEmail("savecommand@command.com");
        command.setName(null);
        command.setPassword("123456");
        command.setRoleId(role.getId());

        final HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandReturnsBadRequest2() throws Exception {
        final Role role = roleRepository.findAll().get(0);
        final SaveCommand command = new SaveCommand();
        command.setEmail("savecommand@command.com");
        command.setName("<html>lorem ipsum</html>");
        command.setPassword("<html>lorem ipsum</html>");
        command.setRoleId(role.getId());

        final HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandReturnsBadRequestWhenEmailAlready() throws Exception {
        final Role role = roleRepository.findAll().get(0);
        final SaveCommand command = new SaveCommand();
        command.setEmail("email_already@command.com");
        command.setName(UUID.randomUUID().toString());
        command.setPassword("123456");
        command.setRoleId(role.getId());

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        final List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        final String exceptionMessageExpected = "E-mail already";
        final String exceptionMessageActual = errors.get(0).getMessage();
        assertEquals(exceptionMessageExpected, exceptionMessageActual);
    }

    @Test
    public void testIfUpdateCommandReturnsOk() throws Exception {
        final User userSaved = createUser();
        final UpdateCommand command = new UpdateCommand();
        command.setId(userSaved.getId());
        command.setName(UUID.randomUUID().toString());

        final HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final User userUpdated = mapper.readValue(responseEntity.getBody(), User.class);
        assertNotNull(userUpdated);
        assertNotNull(userUpdated.getId());
        assertEquals(userSaved.getId(), userUpdated.getId());
        assertNotEquals(userSaved.getName(), userUpdated.getName());
    }

    @Test
    public void testIfUpdateCommandReturnsBadRequest() throws Exception {
        final User userSaved = createUser();
        final UpdateCommand command = new UpdateCommand();
        command.setId(userSaved.getId());
        command.setName(null);

        final HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfUpdateCommandReturnsBadRequest2() throws Exception {
        final UpdateCommand command = new UpdateCommand();
        command.setId(UUID.randomUUID());
        command.setName(UUID.randomUUID().toString());
        final HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }

    @Test
    public void testIfDisableUUIDCommandReturnsOk() throws Exception {
        final User userSaved = createUser();
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/" + userSaved.getId().toString(), HttpMethod.DELETE, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final User userDisabled = mapper.readValue(responseEntity.getBody(), User.class);
        assertNotNull(userDisabled);
        assertNotNull(userDisabled.getId());
        assertEquals(userSaved.getId(), userDisabled.getId());
        assertFalse(userDisabled.isEnabled());
    }

    @Test
    public void testIfDisableUUIDCommandReturnsBadRequest() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/aaa", HttpMethod.DELETE, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfDisableUUIDCommandReturnsNotFound() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/" + UUID.randomUUID().toString(), HttpMethod.DELETE, entity, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testIfCountCommandReturnsOk() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user/count?user.enabled=true", HttpMethod.GET, entity, String.class);
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


    private User createUser() throws Exception {
        Role role = roleRepository.findAll().get(0);
        SaveCommand command = new SaveCommand();
        command.setEmail(UUID.randomUUID().toString() + "@command.com");
        command.setName(UUID.randomUUID().toString());
        command.setPassword("123456");
        command.setRoleId(role.getId());

        HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        return mapper.readValue(responseEntity.getBody(), User.class);
    }

}
