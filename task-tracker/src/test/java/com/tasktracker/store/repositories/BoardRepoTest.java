package com.tasktracker.store.repositories;

import com.tasktracker.store.entities.BoardEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BoardRepoTest {

    private final BoardRepo boardRepo;

    @Autowired
    BoardRepoTest(BoardRepo boardRepo) {
        this.boardRepo = boardRepo;
    }

    @Test
    void findByNameAndPersonId() {
        BoardEntity board = BoardEntity.builder()
                .name("test")
                .personId(1l)
                .build();
        board = boardRepo.saveAndFlush(board);

        Optional<BoardEntity> foundBoard = boardRepo.findByNameAndPersonId("test", 1l);

        assertTrue(foundBoard.get().equals(board));
        assertTrue(foundBoard.get().getName() == "test");
        assertTrue(foundBoard.get().getPersonId() == 1l);

    }

    @Test
    void findAllByNameStartingWithIgnoreCaseAndPersonId() {
        BoardEntity firstBoard = BoardEntity.builder()
                .name("test")
                .personId(1l)
                .build();
        BoardEntity secondBoard = BoardEntity.builder()
                .name("tomato")
                .personId(1l)
                .build();
        firstBoard = boardRepo.saveAndFlush(firstBoard);
        secondBoard = boardRepo.saveAndFlush(secondBoard);

        List<BoardEntity> boards = boardRepo.findAllByNameStartingWithIgnoreCaseAndPersonId("te", 1l);

        assertTrue(boards.contains(firstBoard));
        assertTrue(!boards.contains(secondBoard));
    }

    @Test
    void findAllByPersonId() {
        BoardEntity firstBoard = BoardEntity.builder()
                .name("test")
                .personId(1l)
                .build();
        BoardEntity secondBoard = BoardEntity.builder()
                .name("test")
                .personId(2l)
                .build();
        firstBoard = boardRepo.saveAndFlush(firstBoard);
        secondBoard = boardRepo.saveAndFlush(secondBoard);

        List<BoardEntity> boards = boardRepo.findAllByPersonId(1l);

        assertTrue(boards.contains(firstBoard));
        assertTrue(!boards.contains(secondBoard));
    }

    @Test
    void findByPersonIdAndId() {
        BoardEntity firstBoard = BoardEntity.builder()
                .name("test")
                .personId(1l)
                .build();
        BoardEntity secondBoard = BoardEntity.builder()
                .name("tomato")
                .personId(1l)
                .build();
        firstBoard = boardRepo.saveAndFlush(firstBoard);
        secondBoard = boardRepo.saveAndFlush(secondBoard);

        Optional<BoardEntity> board  = boardRepo.findByPersonIdAndId(1l, firstBoard.getId());

        assertTrue(board.get().equals(firstBoard));
    }

    @Test
    void deleteByIdAndPersonId() {
        BoardEntity firstBoard = BoardEntity.builder()
                .name("test")
                .personId(1l)
                .build();
        BoardEntity secondBoard = BoardEntity.builder()
                .name("test")
                .personId(1l)
                .build();
        firstBoard = boardRepo.saveAndFlush(firstBoard);
        secondBoard = boardRepo.saveAndFlush(secondBoard);

        boardRepo.deleteByIdAndPersonId(firstBoard.getId(), 1l);

        List<BoardEntity> boards = boardRepo.findAll();
        assertTrue(boards.size()==1);
        assertTrue(boards.contains(secondBoard));
    }
}