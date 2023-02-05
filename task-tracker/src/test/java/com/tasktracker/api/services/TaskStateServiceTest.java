package com.tasktracker.api.services;

import com.tasktracker.api.dto.TaskStateDto;
import com.tasktracker.api.exceptions.BadRequestException;
import com.tasktracker.api.factories.TaskStateDtoFactory;
import com.tasktracker.store.entities.BoardEntity;
import com.tasktracker.store.entities.TaskStateEntity;
import com.tasktracker.store.repositories.BoardRepo;
import com.tasktracker.store.repositories.TaskStateRepo;
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
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@SpringBootTest
@ContextConfiguration(classes = TaskStateServiceTest.class)
class TaskStateServiceTest {

    @InjectMocks
    private TaskStateService taskStateService;

    @Mock
    private TaskStateRepo taskStateRepo;

    @Mock
    private TaskStateDtoFactory taskStateDtoFactory;

    @Mock
    private BoardRepo boardRepo;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldGetTaskStates() {
        BoardEntity board = mock(BoardEntity.class);
        when(boardRepo.findByPersonIdAndId(anyLong(), anyLong())).thenReturn(Optional.ofNullable(board));

        taskStateService.getTaskStates(1l, 1l);

        Mockito.verify(boardRepo).findByPersonIdAndId( anyLong(), anyLong());
        Mockito.verify(board).getTaskStates();
    }

    @Test
    void shouldCreateTaskState() {
        BoardEntity board = mock(BoardEntity.class);
        Long boardId = 1l;
        String taskStateName = "test";
        Long personId = 1l;
        TaskStateEntity taskStateEntity = TaskStateEntity.builder()
                .id(1l)
                .name(taskStateName)
                .board(board)
                .build();
        TaskStateDto taskStateDto = TaskStateDto.builder()
                .id(1l)
                .name(taskStateName)
                .build();
        when(boardRepo.findByPersonIdAndId(anyLong(), anyLong())).thenReturn(Optional.ofNullable(board));
        when(board.getTaskStates()).thenReturn(Collections.emptyList());
        when(taskStateRepo.saveAndFlush(any(TaskStateEntity.class)))
                .thenReturn(taskStateEntity)
                .thenReturn(taskStateEntity);
        when(taskStateDtoFactory.makeTaskStateDto(any(TaskStateEntity.class)))
                .thenReturn(taskStateDto);

        TaskStateDto resultTaskStateDto = taskStateService.createTaskState(boardId, taskStateName, personId);

        assertEquals(taskStateDto, resultTaskStateDto);
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmptyCreatingTaskState() {
        assertThrows(BadRequestException.class, () -> {
            taskStateService.createTaskState(1l, "", 1l);
        });
    }

    @Test
    void shouldThrowExceptionWhenNameIsSameCreatingTaskState() {
        BoardEntity board = mock(BoardEntity.class);
        when(boardRepo.findByPersonIdAndId(anyLong(), anyLong())).thenReturn(Optional.ofNullable(board));
        when(board.getTaskStates()).thenReturn(List.of(
               TaskStateEntity.builder()
                       .name("test")
                       .build() ));


        assertThrows(BadRequestException.class, () -> {
            taskStateService.createTaskState(1l, "test", 1l);
        });
    }


    @Test
    void shouldUpdateTaskState() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .name("test")
                .id(1l)
                .board(BoardEntity.builder()
                        .id(1l)
                        .personId(1l)
                        .build())
                .build());
        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState));
        when(taskStateRepo.findTaskStateEntityByBoardIdAndNameIgnoreCase(anyLong(), anyString()))
                .thenReturn(Optional.empty());

        taskStateService.updateTaskState(1l, "test 2", 1l);

        verify(taskState).setName(anyString());
        verify(taskStateRepo).saveAndFlush(any(TaskStateEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmptyUpdatingTaskState() {
        assertThrows(BadRequestException.class, () -> {
            taskStateService.updateTaskState(1l, "", 1l);
        });
    }

    @Test
    void  shouldThrowExceptionWhenNameIsSameUpdatingTaskState() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .name("test")
                .id(1l)
                .board(BoardEntity.builder()
                        .id(1l)
                        .personId(1l)
                        .build())
                .build());
        TaskStateEntity foundTaskState = spy(TaskStateEntity.builder()
                .name("test 2")
                .id(2l)
                .build());
        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState));

        when(taskStateRepo.findTaskStateEntityByBoardIdAndNameIgnoreCase(anyLong(), anyString()))
                .thenReturn(Optional.of(foundTaskState));

        assertThrows(BadRequestException.class, () -> {
            taskStateService.updateTaskState(1l, "test 2", 1l);
        });
    }

    @Test
    void shouldDeleteAllTuskStates() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .name("test")
                .id(1l)
                .board(BoardEntity.builder()
                        .id(1l)
                        .personId(1l)
                        .build())
                .build());
        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState));

        taskStateService.deleteTuskState(1l, Optional.of(true), 1l);

        verify(taskStateRepo).deleteAllByBoardId(anyLong());
    }

    @Test
    void shouldDeleteTuskStateWithNoNeighbors() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .name("test")
                .id(1l)
                .board(BoardEntity.builder()
                        .id(1l)
                        .personId(1l)
                        .build())
                .build());
        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState));

        taskStateService.deleteTuskState(1l, Optional.of(false), 1l);

        verify(taskState).getPreviousTaskState();
        verify(taskState).getNextTaskState();
        verify(taskStateRepo, never()).save(any(TaskStateEntity.class));
        verify(taskStateRepo).deleteById(anyLong());
    }
    @Test
    void shouldDeleteTuskStateWithLeftNeighbor() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
            .name("test")
            .id(1l)
            .board(BoardEntity.builder()
                    .id(1l)
                    .personId(1l)
                    .build())
            .build());
        TaskStateEntity leftTaskState = spy(TaskStateEntity.builder()
                .name("test 2")
                .id(2l)
                .build());
        when(taskState.getPreviousTaskState()).thenReturn(Optional.ofNullable(leftTaskState));
        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState));

        taskStateService.deleteTuskState(1l, Optional.of(false), 1l);

        verify(leftTaskState).setNextTaskState(null);
        verify(taskStateRepo).deleteById(anyLong());

    }
    @Test
    void shouldDeleteTuskStateWithBothNeighbors() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .name("test")
                .id(1l)
                .board(BoardEntity.builder()
                        .id(1l)
                        .personId(1l)
                        .build())
                .build());
        TaskStateEntity leftTaskState = spy(TaskStateEntity.builder()
                .name("test 2")
                .id(2l)
                .build());
        TaskStateEntity rightTaskState = spy(TaskStateEntity.builder()
                .name("test 3")
                .id(3l)
                .build());
        when(taskState.getPreviousTaskState()).thenReturn(Optional.ofNullable(leftTaskState));
        when(taskState.getNextTaskState()).thenReturn(Optional.ofNullable(rightTaskState));
        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState));

        taskStateService.deleteTuskState(1l, Optional.of(false), 1l);

        verify(taskState).setNextTaskState(null);
        verify(taskState).setPreviousTaskState(null);
        verify(taskStateRepo).deleteById(anyLong());
    }

    @Test
    void shouldChangeTaskStatePosition() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .id(1l)
                .name("test")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        TaskStateEntity previousTaskState = spy(TaskStateEntity.builder()
                .id(2l)
                .name("previous")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        TaskStateEntity nextTaskState = spy(TaskStateEntity.builder()
                .id(2l)
                .name("next")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState))
                .thenReturn(Optional.ofNullable(previousTaskState))
                .thenReturn(Optional.ofNullable(nextTaskState));


        taskStateService.changeTaskStatePosition(1l, Optional.of( 2l), Optional.of(3l), 1l);

        verify(previousTaskState).setNextTaskState(any(TaskStateEntity.class));
        verify(nextTaskState).setPreviousTaskState(any(TaskStateEntity.class));
        verify(taskStateRepo, times(3)).save(any(TaskStateEntity.class));
        verify(taskState).setNextTaskState(any(TaskStateEntity.class));
        verify(taskState).setPreviousTaskState(any(TaskStateEntity.class));
    }
    @Test
    void shouldChangeTaskStatePositionWhenNextTaskStateIsMissing() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .id(1l)
                .name("test")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        TaskStateEntity previousTaskState = spy(TaskStateEntity.builder()
                .id(2l)
                .name("previous")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());


        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState))
                .thenReturn(Optional.ofNullable(previousTaskState));


        taskStateService.changeTaskStatePosition(1l, Optional.of( 2l), Optional.empty(), 1l);

        verify(previousTaskState).setNextTaskState(any(TaskStateEntity.class));
        verify(taskStateRepo, times(2)).save(any(TaskStateEntity.class));
        verify(taskState).setNextTaskState(null);
        verify(taskState).setPreviousTaskState(any(TaskStateEntity.class));
    }

    @Test
    void shouldChangeTaskStatePositionWhenPreviousTaskStateIsMissing() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .id(1l)
                .name("test")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        TaskStateEntity nextTaskState = spy(TaskStateEntity.builder()
                .id(2l)
                .name("next")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState))
                .thenReturn(Optional.ofNullable(nextTaskState));


        taskStateService.changeTaskStatePosition(1l, Optional.empty(), Optional.of(3l), 1l);

        verify(nextTaskState).setPreviousTaskState(any(TaskStateEntity.class));
        verify(taskStateRepo, times(2)).save(any(TaskStateEntity.class));
        verify(taskState).setNextTaskState(any(TaskStateEntity.class));
        verify(taskState).setPreviousTaskState(null);
    }

    @Test
    void shouldThrowExceptionWhenSameIdChangingTaskStatePosition() {
        assertThrows(BadRequestException.class, () -> {
            taskStateService.changeTaskStatePosition(1l, Optional.of(1l), Optional.of(1l), 1l);
        });
    }

    @Test
    void shouldThrowExceptionWhenTaskStateIdsAreEmptyChangingTaskStatePosition(){
        assertThrows(BadRequestException.class, () -> {
            taskStateService.changeTaskStatePosition(1l,
                    Optional.empty(),
                    Optional.empty(),
                    1l);
        });
    }

    @Test
    void shouldThrowExceptionWhenDifferentBoardsChangingTaskStatePosition() {
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .id(1l)
                .name("test")
                .board(BoardEntity.builder()
                        .id(1l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        TaskStateEntity previousTaskState = spy(TaskStateEntity.builder()
                .id(2l)
                .name("previous")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        TaskStateEntity nextTaskState = spy(TaskStateEntity.builder()
                .id(2l)
                .name("next")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState))
                .thenReturn(Optional.ofNullable(previousTaskState))
                .thenReturn(Optional.ofNullable(nextTaskState));


        assertThrows(BadRequestException.class, () -> {
            taskStateService.changeTaskStatePosition(1l, Optional.of( 2l), Optional.of(1l), 1l);
        });
    }
    @Test
    void shouldThrowExceptionWhenDifferentBoardsChangingTaskStatePositionAndPreviousTaskStateIsMissing() {
        boolean exceptionThrown = false;
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .id(1l)
                .name("test")
                .board(BoardEntity.builder()
                        .id(1l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        TaskStateEntity nextTaskState = spy(TaskStateEntity.builder()
                .id(2l)
                .name("next")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());
        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState))
                .thenReturn(Optional.ofNullable(nextTaskState));

        try {
            taskStateService.changeTaskStatePosition(1l, Optional.empty(), Optional.of(1l), 1l);
        }catch (BadRequestException ex){
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

    }

    @Test
    void shouldThrowExceptionWhenDifferentBoardsChangingTaskStatePositionAndNextTaskStateIsMissing() {
        boolean exceptionThrown = false;
        TaskStateEntity taskState = spy(TaskStateEntity.builder()
                .id(1l)
                .name("test")
                .board(BoardEntity.builder()
                        .id(1l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());

        TaskStateEntity previousTaskState = spy(TaskStateEntity.builder()
                .id(2l)
                .name("next")
                .board(BoardEntity.builder()
                        .id(2l)
                        .name("test board")
                        .personId(1l)
                        .build())
                .build());
        when(taskStateRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(taskState))
                .thenReturn(Optional.ofNullable(previousTaskState));

        try {
            taskStateService.changeTaskStatePosition(1l, Optional.of(2l), Optional.empty(), 1l);
        }catch (BadRequestException ex){
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

    }
}