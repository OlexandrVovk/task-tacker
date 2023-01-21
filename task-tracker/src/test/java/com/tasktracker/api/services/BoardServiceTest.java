package com.tasktracker.api.services;

import com.tasktracker.api.dto.BoardDto;
import com.tasktracker.api.exceptions.BadRequestException;
import com.tasktracker.api.factories.BoardDtoFactory;
import com.tasktracker.store.entities.BoardEntity;
import com.tasktracker.store.repositories.BoardRepo;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = BoardServiceTest.class)
class BoardServiceTest{
    @Mock
    private BoardRepo boardRepo;

    @Mock
    private BoardDtoFactory boardDtoFactory;

    @InjectMocks
    private BoardService boardService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldDeleteBoard() {
        BoardEntity board = mock(BoardEntity.class);
        Mockito.when(boardRepo.findByPersonIdAndId(anyLong(), anyLong()))
                .thenReturn(Optional.ofNullable(board));

        boardService.deleteBoard(1l, 1l);

        Mockito.verify(boardRepo).deleteByIdAndPersonId(anyLong() , anyLong());
    }

    @Test
    void shouldBoardByName(){
        BoardEntity board = mock(BoardEntity.class);
        BoardDto boardDto = mock(BoardDto.class);
        when(boardRepo.findAllByNameStartingWithIgnoreCaseAndPersonId(anyString(),anyLong()))
                .thenReturn(Collections.singletonList(board));
        when(boardDtoFactory.makeBoardDto(any(BoardEntity.class))).thenReturn(boardDto);

        List<BoardDto> resultList = boardService.fetchBoard(Optional.of("test"), 1l);

        assertEquals(boardDto, resultList.get(0));
        Mockito.verify(boardRepo).findAllByNameStartingWithIgnoreCaseAndPersonId(anyString(), anyLong());
    }

    @Test
    void shouldGetAllBoards() {
        BoardEntity board = mock(BoardEntity.class);
        BoardDto boardDto = mock(BoardDto.class);
        when(boardRepo.findAllByPersonId(anyLong())).thenReturn(Collections.singletonList(board));
        when(boardDtoFactory.makeBoardDto(any(BoardEntity.class))).thenReturn(boardDto);

        List<BoardDto> resultList = boardService.fetchBoard(Optional.empty(), 1l);

        assertEquals(boardDto, resultList.get(0));
        Mockito.verify(boardRepo).findAllByPersonId(anyLong());
    }


    @Test
    void shouldCreateBoard() {
        BoardEntity board = mock(BoardEntity.class);
        BoardDto boardDto = mock(BoardDto.class);
        when(boardRepo.saveAndFlush(any(BoardEntity.class))).thenReturn(board);
        when(boardDtoFactory.makeBoardDto(any(BoardEntity.class))).thenReturn(boardDto);

        BoardDto resultBoard = boardService.createBoardOrUpdateBoard(Optional.empty(),
                Optional.of("test"),
                1l);

        assertEquals(resultBoard ,boardDto);
        Mockito.verify(boardRepo, times(1)).findByNameAndPersonId(anyString(), anyLong());
    }

    @Test
    void shouldThrowBadRequestExcWhenEmptyNameCreatingBoard(){
        assertThrows(BadRequestException.class, () -> {
            boardService.createBoardOrUpdateBoard(Optional.empty(), Optional.empty(), 1l);
        });
    }

    @Test
    void shouldThrowBadRequestExcWhenNameIsTakenUpdatingBoard(){
        BoardEntity firstBoard = BoardEntity.builder()
                .id(1l)
                .name("test")
                .personId(1l)
                .build();
        BoardEntity secondBoard = BoardEntity.builder()
                .id(2l)
                .name("test 2")
                .personId(1l)
                .build();
        when(boardRepo.findByPersonIdAndId(anyLong(),anyLong()))
                .thenReturn(Optional.ofNullable(firstBoard));
        when(boardRepo.findByNameAndPersonId(anyString(), anyLong()))
                .thenReturn(Optional.ofNullable(secondBoard));


        assertThrows(BadRequestException.class, () -> {
            boardService.createBoardOrUpdateBoard(
                    Optional.of(1l), Optional.of("test 2"), 1l);
        });
    }

    @Test
    void shouldUpdateBoard() {
        BoardEntity board = mock(BoardEntity.class);

        when(boardRepo.findByPersonIdAndId(anyLong(),anyLong()))
                .thenReturn(Optional.ofNullable(board));

            boardService.createBoardOrUpdateBoard(
                    Optional.of(1l), Optional.of("test 2"), 1l);

        verify(board).setName("test 2");
    }
}