package com.tasktracker.store.repositories;

import com.tasktracker.store.entities.BoardEntity;
import com.tasktracker.store.entities.TaskStateEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskStateRepoTest {

    private final TaskStateRepo taskStateRepo;
    private final BoardRepo boardRepo;

    @Autowired
    TaskStateRepoTest(TaskStateRepo taskStateRepo, BoardRepo boardRepo) {
        this.taskStateRepo = taskStateRepo;
        this.boardRepo = boardRepo;
    }

    @Test
    void findTaskStateEntityByBoardIdAndNameIgnoreCase() {
        BoardEntity board = BoardEntity.builder()
                .name("test")
                .personId(0l)
                .build();
        TaskStateEntity taskState = TaskStateEntity.builder()
                .name("test")
                .board(board)
                .build();
        board.setTaskStates(Collections.singletonList(taskState));
        board = boardRepo.saveAndFlush(board);
        taskState = taskStateRepo.saveAndFlush(taskState);

        Optional<TaskStateEntity> optionalTaskState = taskStateRepo.findTaskStateEntityByBoardIdAndNameIgnoreCase(board.getId(), taskState.getName());

        assertTrue(optionalTaskState.isPresent());
        assertTrue(optionalTaskState.get().equals(taskState));
    }

    @Test
    void deleteAllByBoardId() {
        BoardEntity board = BoardEntity.builder()
                .name("test")
                .personId(0l)
                .build();
        TaskStateEntity taskState = TaskStateEntity.builder()
                .name("test")
                .board(board)
                .build();
        board.setTaskStates(Collections.singletonList(taskState));
        board = boardRepo.saveAndFlush(board);
        taskState = taskStateRepo.saveAndFlush(taskState);

        taskStateRepo.deleteAllByBoardId(board.getId());
        List<TaskStateEntity> taskStateList = taskStateRepo.findAll();

        assertTrue(taskStateList.isEmpty());
    }
}