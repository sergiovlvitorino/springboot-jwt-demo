package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoleRestControllerTest {

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
    public void testIfListCommandReturnsOk() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/role?pageNumber=0&pageSize=10000&orderBy=name&asc=true&role.name=GUEST", HttpMethod.GET, entity, String.class);
        JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        List<Role> list = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, Role.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsBadRequest() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/role?pageNumber=0&pageSize=-1&orderBy=name&asc=true&role.name=GUEST", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfListCommandReturnsOk2() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/role?pageNumber=0&pageSize=10000&orderBy=name&asc=true", HttpMethod.GET, entity, String.class);
        JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        List<Role> list = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, Role.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsOk3() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/role?pageNumber=0&pageSize=10000&orderBy=name&asc=false", HttpMethod.GET, entity, String.class);
        JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        List<Role> list = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, Role.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testIfCountCommandReturnsOk() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/role/count?role.name=GUEST", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Long countActual = Long.valueOf(responseEntity.getBody());
        assertTrue(countActual > 0);
    }

    @Test
    public void testIfCountCommandReturnsOk2() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/role/count?role.name=GUEST1", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Long countActual = Long.valueOf(responseEntity.getBody());
        assertTrue(countActual == 0L);
    }

    @Test
    public void testIfCountCommandReturnsOk3() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/role/count", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Long countActual = Long.valueOf(responseEntity.getBody());
        assertTrue(countActual > 0);
    }

}
