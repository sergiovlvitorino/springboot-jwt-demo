package com.sergiovitorino.springbootjwt.controller.ui.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springbootjwt.ui.command.user.SaveCommand;
import com.sergiovitorino.springbootjwt.util.LoginHelper;
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
public class UserControllerTest {

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
    public void testIfListCommandIsOk() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true&user.enabled=true", HttpMethod.GET, entity, String.class);
        JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        List<User> list = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testIfSaveCommandIsOk() throws Exception {
        Role role = roleRepository.findAll().get(0);
        SaveCommand command = new SaveCommand();
        command.setEmail("savecommand@command.com");
        command.setName(UUID.randomUUID().toString());
        command.setPassword("123456");
        command.setRoleId(role.getId());

        HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/user", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        User userCreated = mapper.readValue(responseEntity.getBody(), User.class);
        assertNotNull(userCreated);
        assertNotNull(userCreated.getId());
        assertEquals(command.getName(), userCreated.getName());
    }

}
