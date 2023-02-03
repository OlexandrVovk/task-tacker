package com.tasktracker.api.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.BoardDto;
import com.tasktracker.api.dto.TaskStateDto;
import com.tasktracker.api.util.JWTUtil;
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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class TaskStateControllerTest {

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
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-state-data.sql"})
    void shouldGetTaskStates() throws Exception {
        String token = jwtUtil.generateToken(1l);
        RequestBuilder request = MockMvcRequestBuilders
                .get("/api/boards/1/task-states")
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        List<TaskStateDto> resultList = objectMapper.readValue(jsonResponse, new TypeReference<List<TaskStateDto>>() {});

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(resultList.size() != 0);
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-state-data.sql"})
    void shouldCreateTaskState() throws Exception {
        String taskStateName = "BlaBlaName";
        String token = jwtUtil.generateToken(1l);
        RequestBuilder request = MockMvcRequestBuilders
                .post(String.format("/api/boards/1/task-states?task_state_name=%s", taskStateName))
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        TaskStateDto resultTaskStateDto = objectMapper.readValue(jsonResponse, TaskStateDto.class);

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(resultTaskStateDto.getName().equals(taskStateName));
        assertTrue(resultTaskStateDto.getLeftTaskStateId()!=null);
        assertTrue(resultTaskStateDto.getId().equals(4l));
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-state-data.sql"})
    void shouldUpdateTaskState() throws Exception {
        String taskStateName = "BlaBlaName";
        String token = jwtUtil.generateToken(1l);
        ObjectMapper objectMapper = new ObjectMapper();

        RequestBuilder request = MockMvcRequestBuilders
                .patch(String.format("/api/task-states/1?updated_name=%s", taskStateName))
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        RequestBuilder getRequestForTaskStates = MockMvcRequestBuilders
                .get("/api/boards/1/task-states")
                .header("Authorization", "Bearer " + token);
        String getTaskStatesResponse = mvc.perform(getRequestForTaskStates).andReturn().getResponse().getContentAsString();

        TaskStateDto resultTaskStateDto = objectMapper.readValue(jsonResponse, TaskStateDto.class);
        List<TaskStateDto> taskStates = objectMapper.readValue(getTaskStatesResponse, new TypeReference<List<TaskStateDto>>() {});


        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(taskStates.stream().anyMatch(taskState ->{
            return taskState.getId().equals(resultTaskStateDto.getId()) &&
                    taskState.getName().equals(taskStateName);
        }));
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-state-data.sql"})
    void shouldDeleteTuskState() throws Exception {
        Long taskStateId = 1l;
        String token = jwtUtil.generateToken(1l);
        ObjectMapper objectMapper = new ObjectMapper();

        RequestBuilder request = MockMvcRequestBuilders
                .delete(String.format("/api/task-states/%d", taskStateId))
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        RequestBuilder getRequestForTaskStates = MockMvcRequestBuilders
                .get("/api/boards/1/task-states")
                .header("Authorization", "Bearer " + token);
        String getTaskStatesResponse = mvc.perform(getRequestForTaskStates).andReturn().getResponse().getContentAsString();

        AnswerDto answerDto = objectMapper.readValue(jsonResponse, AnswerDto.class);
        List<TaskStateDto> taskStates = objectMapper.readValue(getTaskStatesResponse, new TypeReference<List<TaskStateDto>>() {});


        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(answerDto.isAnswer());
        assertTrue(taskStates.stream().filter(taskState ->{
            return taskState.getId().equals(taskStateId);
        }).findFirst().isEmpty());
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/task-state-data.sql"})
    void shouldChangeTaskStatePosition() throws Exception {
        Long taskStateId = 1l;
        Long previousTaskStateId = 2l;
        Long nextTaskStateId = 3l;
        String token = jwtUtil.generateToken(1l);
        ObjectMapper objectMapper = new ObjectMapper();

        RequestBuilder request = MockMvcRequestBuilders
                .patch(String.format("/api/task-states/%d/position/change?previous_task_state_id=%d&next_task_state_id=%d&to_sort=true",
                        taskStateId,
                        previousTaskStateId,
                        nextTaskStateId))
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        List<TaskStateDto> changedPositionTaskStates = objectMapper.readValue(jsonResponse, new TypeReference<List<TaskStateDto>>() {});

        TaskStateDto updatedTaskState = changedPositionTaskStates.stream().filter(taskStateDto -> {
            return taskStateDto.getId().equals(taskStateId);
        }).findFirst().get();

        Optional<TaskStateDto> newLeftTaskState = changedPositionTaskStates.stream().filter(taskStateDto -> {
            return taskStateDto.getId().equals(updatedTaskState.getLeftTaskStateId());
        }).findFirst();
        Optional<TaskStateDto> newRightTaskState = changedPositionTaskStates.stream().filter(taskStateDto -> {
            return taskStateDto.getId().equals(updatedTaskState.getRightTaskStateId());
        }).findFirst();
        newLeftTaskState.ifPresent(leftTaskState -> {
            assertTrue(leftTaskState.getId().equals(previousTaskStateId));
        });
        newRightTaskState.ifPresent(rightTaskState ->{
            assertTrue(rightTaskState.getId().equals(nextTaskStateId));
        });
        assertTrue(updatedTaskState.getRightTaskStateId()==nextTaskStateId &&
                updatedTaskState.getLeftTaskStateId() == previousTaskStateId);
        assertEquals(200 , mvcResult.getResponse().getStatus());

    }
}