package com.tasktracker.api.controllers;

import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.BoardDto;
import com.tasktracker.api.factories.BoardDtoFactory;
import com.tasktracker.api.services.BoardService;
import com.tasktracker.api.util.JWTUtil;
import com.tasktracker.store.entities.BoardEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class BoardController {

    private final JWTUtil jwtUtil;
    private final BoardService boardService;
    public static final String DELETE_BOARD="/api/boards/{board_id}";
    public static final String FETCH_BOARD="/api/boards";
    public static final String CREATE_OR_UPDATE_BOARD="/api/boards";

    @GetMapping(FETCH_BOARD)
    public List<BoardDto> fetchBoards(
            @RequestParam(value = "prefix_name", required = false)Optional<String> prefixName,
            HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return boardService.fetchBoard(prefixName, personId);
    }

    @PutMapping(CREATE_OR_UPDATE_BOARD)
    public BoardDto createBoardOrUpdateBoard(
            @RequestParam(value = "board_id", required = false) Optional<Long> boardId,
            @RequestParam(value = "board_name", required = false) Optional<String> boardName,
            HttpServletRequest request
    ){
        Long personId = jwtUtil.getPersonId(request);
        return boardService.createBoardOrUpdateBoard(boardId, boardName, personId);
    }


    @DeleteMapping(DELETE_BOARD)
    public AnswerDto deleteBoard(@PathVariable("board_id") Long boardId,
                                 HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return boardService.deleteBoard(boardId, personId);
    }
}
