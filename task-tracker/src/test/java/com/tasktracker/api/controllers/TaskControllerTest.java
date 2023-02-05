package com.tasktracker.api.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.TaskDto;
import com.tasktracker.api.util.JWTUtil;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

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
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-data.sql"})
    void shouldCreateTask() throws Exception {
        String token = jwtUtil.generateToken(1l);
        ObjectMapper objectMapper = new ObjectMapper();
        String taskName = "BlaBlaName";

        RequestBuilder request = MockMvcRequestBuilders
                .post( String.format("/api/task-states/1/tasks?task_name=%s", taskName))
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();


        TaskDto resultTaskDto = objectMapper.readValue(jsonResponse, TaskDto.class);

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(resultTaskDto.getName().equals(taskName));
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-data.sql"})
    void shouldGetTasks() throws Exception {
        String token = jwtUtil.generateToken(1l);
        ObjectMapper objectMapper = new ObjectMapper();

        RequestBuilder request = MockMvcRequestBuilders
                .get("/api/task-states/1/tasks")
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        List<TaskDto> resultList = objectMapper.readValue(jsonResponse, new TypeReference<List<TaskDto>>() {});

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(resultList.size() != 0);
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-data.sql"})
    void shouldDeleteTask() throws Exception {
        String token = jwtUtil.generateToken(1l);
        ObjectMapper objectMapper = new ObjectMapper();
        int taskId = 1;

        RequestBuilder request = MockMvcRequestBuilders
                .delete( String.format("/api/tasks/%d", taskId))
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        AnswerDto answerDto = objectMapper.readValue(jsonResponse, AnswerDto.class);

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(answerDto.isAnswer());
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-data.sql"})
    void shouldUpdateTask() throws Exception {
        String token = jwtUtil.generateToken(1l);
        ObjectMapper objectMapper = new ObjectMapper();
        TaskDto taskDto = TaskDto.builder()
                .id(1l)
                .name("test")
                .description("bla bla")
                .build();
        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = objectWriter.writeValueAsString(taskDto);

        RequestBuilder request = MockMvcRequestBuilders
                .patch( "/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        TaskDto resultTaskDto = objectMapper.readValue(jsonResponse, TaskDto.class);

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(resultTaskDto.getName().equals("test") &&
                resultTaskDto.getDescription().equals("bla bla"));
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-data.sql"})
    void shouldChangeTaskPosition() throws Exception {
        String token = jwtUtil.generateToken(1l);
        ObjectMapper objectMapper = new ObjectMapper();
        Long previousTaskId = 2l;
        Long nextTaskId = 3l;

        RequestBuilder request = MockMvcRequestBuilders
                .patch(String.format("/api/tasks/1/position/change?previous_task_id=%d&next_task_id=%d",
                                previousTaskId,
                                nextTaskId))
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();


        TaskDto resultTaskDto = objectMapper.readValue(jsonResponse, TaskDto.class);

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(resultTaskDto.getPreviousTaskId().equals(previousTaskId) &&
                resultTaskDto.getNextTaskId().equals(nextTaskId));
    }
}