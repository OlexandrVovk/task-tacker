package com.tasktracker.api.factories;

import com.tasktracker.api.dto.BoardDto;
import com.tasktracker.store.entities.BoardEntity;
import org.springframework.stereotype.Component;

@Component
public class BoardDtoFactory {

    public BoardDto makeBoardDto(BoardEntity board){
        return BoardDto.builder()
                .id(board.getId())
                .name(board.getName())
                .build();
    }
}
