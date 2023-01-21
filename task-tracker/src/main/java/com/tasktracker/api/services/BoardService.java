package com.tasktracker.api.services;

import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.BoardDto;
import com.tasktracker.api.exceptions.BadRequestException;
import com.tasktracker.api.exceptions.NotFoundException;
import com.tasktracker.api.factories.BoardDtoFactory;
import com.tasktracker.store.entities.BoardEntity;
import com.tasktracker.store.repositories.BoardRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepo boardRepo;

    private final BoardDtoFactory boardDtoFactory;

    public BoardEntity getBoardOrThrowException(Long boardId, Long personId) {
        BoardEntity boardEntity = boardRepo.findByPersonIdAndId(personId, boardId)
                .orElseThrow(() -> {
                    throw new NotFoundException(String.format("Board with id \"%d\" was no found", boardId));
                });
        return boardEntity;
    }

    @Transactional
    public AnswerDto deleteBoard(Long boardId, Long personId) {
        getBoardOrThrowException(boardId, personId);

        boardRepo.deleteByIdAndPersonId(boardId, personId);
        return AnswerDto.makeDefault(true);
    }

    @Transactional
    public List<BoardDto> fetchBoard(Optional<String> prefixName, Long personId) {
        prefixName = prefixName.filter(name -> !name.trim().isEmpty());

        List<BoardEntity> boardEntityList;
        if (prefixName.isPresent()){
            boardEntityList = boardRepo.findAllByNameStartingWithIgnoreCaseAndPersonId(prefixName.get(), personId);
        }else {
            boardEntityList = boardRepo.findAllByPersonId(personId);
        }

        return boardEntityList.stream()
                .filter(boardEntity -> boardEntity!=null)
                .map(boardDtoFactory::makeBoardDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardDto createBoardOrUpdateBoard(Optional<Long> boardId,
                                             Optional<String> boardName,
                                             Long personId) {
        boolean exists = !boardId.isPresent();
        boardName = boardName.filter(name -> !name.trim().isEmpty());

        if (exists && !boardName.isPresent()){
            throw new BadRequestException("Board name can't be empty");
        }

        BoardEntity boardEntity;
        try {
            boardEntity = (boardId.isPresent()) ?  getBoardOrThrowException(boardId.get(), personId) :
                    BoardEntity.builder().build();
        }catch (BadRequestException exception){
            boardEntity  = BoardEntity.builder().build();
        }


        BoardEntity finalBoardEntity = boardEntity;
        boardName.ifPresent(name -> {
                    Optional<BoardEntity> board = boardRepo.findByNameAndPersonId(name, personId)
                    .filter(anotherBoard->!Objects.equals(anotherBoard.getId(), finalBoardEntity.getId()));

                    board.ifPresent(anotherBoard->{
                        throw new BadRequestException(
                                String.format("Board \"%s\" already exists ", name));
                    });
                    finalBoardEntity.setName(name);
                    finalBoardEntity.setPersonId(personId);
        });
        BoardEntity savedBoard = boardRepo.saveAndFlush(finalBoardEntity);
        return boardDtoFactory.makeBoardDto(savedBoard);
    }
}
