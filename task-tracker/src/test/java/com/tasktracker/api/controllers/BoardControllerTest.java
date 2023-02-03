package com.tasktracker.api.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.BoardDto;
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

import static org.junit.Assert.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

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
    @Sql({"classpath:empty/reset.sql", "classpath:init/board-data.sql"})
    void shouldFetchBoardsWhenGivenName() throws Exception {
        String token = jwtUtil.generateToken(1l);
        RequestBuilder request = MockMvcRequestBuilders
                .get("/api/boards?prefix_name=test")
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        List<BoardDto> resultList = objectMapper.readValue(jsonResponse, new TypeReference<List<BoardDto>>() {});

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(resultList.size() != 0);
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/board-data.sql"})
    void shouldGetBoardsWhenNameIsNotGiven() throws Exception {
        String token = jwtUtil.generateToken(1l);
        RequestBuilder request = MockMvcRequestBuilders
                .get("/api/boards")
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        List<BoardDto> resultList = objectMapper.readValue(jsonResponse, new TypeReference<List<BoardDto>>() {});

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(resultList.size() != 0);
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/board-data.sql"})
    void shouldUpdateBoard() throws Exception {
        String boardNameToUpdate = "BlaBlaBoard";
        ObjectMapper objectMapper = new ObjectMapper();
        String token = jwtUtil.generateToken(1l);

        RequestBuilder updateBoardRequest = MockMvcRequestBuilders
                .put(String.format("/api/boards?board_name=%s&board_id=1", boardNameToUpdate))
                .header("Authorization", "Bearer " + token);
        MvcResult updateBoardMvcResult = mvc.perform(updateBoardRequest).andReturn();
        String updateBoardJsonResponse = updateBoardMvcResult.getResponse().getContentAsString();
        RequestBuilder getBoardsRequest = MockMvcRequestBuilders
                .get("/api/boards")
                .header("Authorization", "Bearer " + token);
        MvcResult getBoardsMvcResult = mvc.perform(getBoardsRequest).andReturn();
        String getBoardJsonResponse = getBoardsMvcResult.getResponse().getContentAsString();

        BoardDto updatedBoard = objectMapper.readValue(updateBoardJsonResponse, BoardDto.class);

        assertEquals(200, updateBoardMvcResult.getResponse().getStatus());
        assertTrue(updatedBoard.getName().equals(boardNameToUpdate));
    }

    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/board-data.sql"})
    void shouldCreateBoard() throws Exception {
        String token = jwtUtil.generateToken(1l);
        String boardName = "board";
        ObjectMapper objectMapper = new ObjectMapper();

        RequestBuilder request = MockMvcRequestBuilders
                .put(String.format("/api/boards?board_name=%s", boardName))
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        BoardDto resultBoardDto = objectMapper.readValue(jsonResponse, BoardDto.class);

        assertEquals(200, mvcResult.getResponse().getStatus());
        assertTrue(resultBoardDto.getName().equals(boardName));
        assertTrue(resultBoardDto.getId() != null);
    }


    @Test
    @Sql({"classpath:empty/reset.sql", "classpath:init/board-data.sql"})
    void shouldDeleteBoard() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String token = jwtUtil.generateToken(1l);

        RequestBuilder request = MockMvcRequestBuilders
                .delete("/api/boards/1")
                .header("Authorization", "Bearer " + token);
        MvcResult mvcResult = mvc.perform(request).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        AnswerDto answerDto = objectMapper.readValue(jsonResponse, AnswerDto.class);

        assertEquals(200 , mvcResult.getResponse().getStatus());
        assertTrue(answerDto.isAnswer());
    }
}