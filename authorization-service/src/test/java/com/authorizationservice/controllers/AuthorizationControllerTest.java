package com.authorizationservice.controllers;

import com.authorizationservice.dto.PersonDto;
import com.authorizationservice.util.JWTUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationControllerTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private JWTUtil jwtUtil;
    private MockMvc mvc;

    @BeforeEach
    public void setup(){
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @Sql({"classpath:empty/reset.sql"})
    void shouldPerformRegistration() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        PersonDto personDto = PersonDto.builder()
                .name("test")
                .password("test")
                .build();
        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = objectWriter.writeValueAsString(personDto);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/api/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        MvcResult mvcResult = mvc.perform(request).andReturn();

        assertEquals(202 , mvcResult.getResponse().getStatus());
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/person-data.sql"})
    void shouldPerformLogin() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        PersonDto personDto = PersonDto.builder()
                .name("test user")
                .password("test password")
                .build();
        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = objectWriter.writeValueAsString(personDto);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        Map<String,String> jwtTokenMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, String>>() {});
        String jwtToken = jwtTokenMap.entrySet().iterator().next().getValue();
        Long personId = jwtUtil.validateTokenAndRetrieveClaim(jwtToken);

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(personId.equals(1l));
    }
}