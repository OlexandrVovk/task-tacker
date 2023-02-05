package com.tasktracker.api.services;

import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.TaskDto;
import com.tasktracker.api.exceptions.BadRequestException;
import com.tasktracker.api.factories.TaskDtoFactory;
import com.tasktracker.store.entities.BoardEntity;
import com.tasktracker.store.entities.TaskEntity;
import com.tasktracker.store.entities.TaskStateEntity;
import com.tasktracker.store.repositories.TaskRepo;
import com.tasktracker.store.repositories.TaskStateRepo;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = TaskServiceTest.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepo taskRepo;

    @Mock
    private TaskDtoFactory taskDtoFactory;

    @Mock
    private TaskStateRepo taskStateRepo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCreateTaskWhenNoMoreTasksInTaskState() {
        Long personId = 1l;
        TaskStateEntity taskStateEntity = spy(TaskStateEntity.builder()
                .name("task state")
                .id(1l)
                .board(BoardEntity.builder()
                        .personId(personId)
                        .name("board")
                        .id(1l)
                        .build())
                .build());
        TaskEntity taskEntity = spy(
                TaskEntity.builder()
                        .name("task")
                        .id(1l)
                        .build()
        );
        when(taskStateRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskStateEntity));
        when(taskRepo.saveAndFlush(any(TaskEntity.class))).thenReturn(taskEntity);

        taskService.createTask(1l, "test", 1l);


        verify(taskEntity, never()).setPreviousTask(any(TaskEntity.class));
        verify(taskRepo, times(2)).saveAndFlush(any(TaskEntity.class));
    }

    @Test
    void shouldCreateTaskWhenTaskStateHasAnotherTasks() {
        Long personId = 1l;
        TaskStateEntity taskStateEntity = spy(TaskStateEntity.builder()
                .name("task state")
                .id(1l)
                .board(BoardEntity.builder()
                        .personId(personId)
                        .name("board")
                        .id(1l)
                        .build())
                .build());
        TaskEntity taskEntity = spy(
                TaskEntity.builder()
                        .name("task")
                        .id(1l)
                        .build()
        );
        TaskEntity firstTask = spy(TaskEntity.builder()
                .id(2l)
                .name("first task")
                .build());
        when(taskStateEntity.getTasks()).thenReturn(List.of(firstTask));
        when(taskStateRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskStateEntity));
        when(taskRepo.saveAndFlush(any(TaskEntity.class))).thenReturn(taskEntity);

        taskService.createTask(1l, "test", 1l);

        verify(taskRepo, times(3)).saveAndFlush(any(TaskEntity.class));
    }

    @Test
    void shouldThrowBadRequestExcWhenTaskNameIsEmptyCreatingTask() {
        assertThrows(BadRequestException.class,()->{
           taskService.createTask(1l, "", 1l);
        });
    }

    @Test
    void shouldThrowBadRequestExcWhenTaskNameReservedCreatingTask() {
        Long personId = 1l;
        TaskStateEntity taskStateEntity = spy(TaskStateEntity.builder()
                .name("task state")
                .id(1l)
                .board(BoardEntity.builder()
                        .personId(personId)
                        .name("board")
                        .id(1l)
                        .build())
                .build());
        TaskEntity taskEntity = spy(TaskEntity.builder()
                .id(2l)
                .name("test")
                .build());
        when(taskStateRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskStateEntity));
        when(taskStateEntity.getTasks()).thenReturn(List.of(taskEntity));

        assertThrows(BadRequestException.class, () -> {
           taskService.createTask(1l, "test", personId);
        });
    }

    @Test
    void shouldGetTasks() {
        TaskStateEntity taskStateEntity = spy(TaskStateEntity.builder()
                .name("task state")
                .id(1l)
                .board(BoardEntity.builder()
                        .personId(1l)
                        .name("board")
                        .id(1l)
                        .build())
                .build());
        when(taskStateRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskStateEntity));

        taskService.getTasks(1l, 1l);

        verify(taskStateEntity).getTasks();
        verify(taskStateRepo).findById(anyLong());
    }

    @Test
    void shouldDeleteTaskWithNoNeighbors() {
        TaskEntity taskEntity = spy(TaskEntity.builder()
                .name("task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .board(BoardEntity.builder()
                                .personId(1l)
                                .name("board")
                                .id(1l)
                                .build())
                        .build())
                .build());
        when(taskRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskEntity));
        when(taskEntity.getPreviousTask()).thenReturn(Optional.empty());
        when(taskEntity.getNextTask()).thenReturn(Optional.empty());

        AnswerDto answerDto = taskService.deleteTask(1l, 1l);

        assertTrue(answerDto.isAnswer());
        verify(taskEntity).getPreviousTask();
        verify(taskEntity).getNextTask();
        verify(taskRepo).deleteById(anyLong());
    }

    @Test
    void shouldDeleteTaskWithBothNeighbors() {
        TaskEntity taskEntity = spy(TaskEntity.builder()
                .name("task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .board(BoardEntity.builder()
                                .personId(1l)
                                .name("board")
                                .id(1l)
                                .build())
                        .build())
                .build());
        TaskEntity previousTask =  spy(TaskEntity.builder()
                .id(2l)
                .name("previous")
                .build());
        TaskEntity nextTask =  spy(TaskEntity.builder()
                .id(3l)
                .name("next")
                .build());
        when(taskRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskEntity));
        when(taskEntity.getPreviousTask()).thenReturn(Optional.ofNullable(previousTask));
        when(taskEntity.getNextTask()).thenReturn(Optional.ofNullable(nextTask));

        AnswerDto answerDto = taskService.deleteTask(1l, 1l);

        assertTrue(answerDto.isAnswer());
        verify(taskEntity).getPreviousTask();
        verify(taskEntity).getNextTask();
        verify(previousTask).setNextTask(any(TaskEntity.class));
        verify(nextTask).setPreviousTask(any(TaskEntity.class));
        verify(taskRepo, times(2)).save(any(TaskEntity.class));
        verify(taskRepo).deleteById(anyLong());
    }

    @Test
    void shouldDeleteTaskWhenItsInTheEnd() {
        TaskEntity taskEntity = spy(TaskEntity.builder()
                .name("task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .board(BoardEntity.builder()
                                .personId(1l)
                                .name("board")
                                .id(1l)
                                .build())
                        .build())
                .build());
        TaskEntity previousTask =  spy(TaskEntity.builder()
                .id(2l)
                .name("previous")
                .build());
        when(taskRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskEntity));
        when(taskEntity.getPreviousTask()).thenReturn(Optional.ofNullable(previousTask));
        when(taskEntity.getNextTask()).thenReturn(Optional.empty());

        AnswerDto answerDto = taskService.deleteTask(1l, 1l);

        assertTrue(answerDto.isAnswer());
        verify(taskEntity).getPreviousTask();
        verify(taskEntity).getNextTask();
        verify(previousTask).setNextTask(null);
        verify(taskRepo).save(any(TaskEntity.class));
        verify(taskRepo).deleteById(anyLong());
    }

    @Test
    void shouldDeleteTaskWhenItsInTheStart() {
        TaskEntity taskEntity = spy(TaskEntity.builder()
                .name("task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .board(BoardEntity.builder()
                                .personId(1l)
                                .name("board")
                                .id(1l)
                                .build())
                        .build())
                .build());
        TaskEntity nextTask =  spy(TaskEntity.builder()
                .id(3l)
                .name("next")
                .build());
        when(taskRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskEntity));
        when(taskEntity.getPreviousTask()).thenReturn(Optional.empty());
        when(taskEntity.getNextTask()).thenReturn(Optional.ofNullable(nextTask));

        AnswerDto answerDto = taskService.deleteTask(1l, 1l);

        assertTrue(answerDto.isAnswer());
        verify(taskEntity).getPreviousTask();
        verify(taskEntity).getNextTask();
        verify(nextTask).setPreviousTask(null);
        verify(taskRepo).save(any(TaskEntity.class));
        verify(taskRepo).deleteById(anyLong());
    }


    @Test
    void shouldUpdateTaskWhenGivenName() {
        TaskEntity taskEntity = spy(TaskEntity.builder()
                .name("task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .board(BoardEntity.builder()
                                .personId(1l)
                                .name("board")
                                .id(1l)
                                .build())
                        .build())
                .build());
        TaskDto taskDto = spy(TaskDto.builder()
                .name("taskDto")
                .id(2l)
                .build());
        when(taskRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskEntity));

        taskService.update(taskDto, 1l);

        verify(taskEntity).setName(anyString());
        verify(taskDto, times(2)).getName();
        verify(taskEntity, never()).setDescription(anyString());
        verify(taskRepo).saveAndFlush(any(TaskEntity.class));
    }
    @Test
    void shouldUpdateTaskWhenGivenNameAndDescription() {
        TaskEntity taskEntity = spy(TaskEntity.builder()
                .name("task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .board(BoardEntity.builder()
                                .personId(1l)
                                .name("board")
                                .id(1l)
                                .build())
                        .build())
                .build());
        TaskDto taskDto = spy(TaskDto.builder()
                .name("taskDto")
                .id(2l)
                .description("bla bla bla")
                .build());
        when(taskRepo.findById(anyLong())).thenReturn(Optional.ofNullable(taskEntity));

        taskService.update(taskDto, 1l);

        verify(taskEntity).setName(anyString());
        verify(taskDto, times(2)).getName();
        verify(taskEntity).setDescription(anyString());
        verify(taskDto, times(3)).getDescription();
        verify(taskRepo).saveAndFlush(any(TaskEntity.class));
    }



    @Test
    void shouldThrowBadRequestExcWhenEmptyPreviousIdAndNextIdChangingTaskPosition() {
        assertThrows(BadRequestException.class, ()->{
           taskService.changeTaskPosition(1l, Optional.empty(), Optional.empty(), 1l);
        });
    }
    @Test
    void shouldThrowBadRequestExcWhenPreviousIdAndNextIdChangingAreSameTaskPosition() {
        assertThrows(BadRequestException.class, ()->{
            taskService.changeTaskPosition(1l, Optional.ofNullable(2l), Optional.ofNullable(2l), 1l);
        });
    }

    @Test
    void shouldThrowBadRequestExcWhenDifferentBoardsChangingAreSameTaskPosition() {
        TaskEntity currTask = spy(TaskEntity.builder()
                .name("curr task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .id(1l)
                        .name("curr task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name("curr board")
                                .personId(1l)
                                .build())
                        .build())
                .build());
        TaskEntity previousTask = TaskEntity.builder()
                .name("previous task")
                .id(2l)
                .taskState(TaskStateEntity.builder()
                        .id(2l)
                        .name(" task state")
                        .board(BoardEntity.builder()
                                .id(2l)
                                .name(" board")
                                .personId(1l)
                                .build())
                        .build())
                .build();
        TaskEntity nextTask = TaskEntity.builder()
                .name("next task")
                .id(3l)
                .taskState(TaskStateEntity.builder()
                        .id(2l)
                        .name(" task state")
                        .board(BoardEntity.builder()
                                .id(2l)
                                .name(" board")
                                .personId(1l)
                                .build())
                        .build())
                .build();

        when(taskRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(currTask))
                .thenReturn(Optional.ofNullable(previousTask))
                .thenReturn(Optional.ofNullable(nextTask));
        when(currTask.getPreviousTask()).thenReturn(Optional.empty());
        when(currTask.getNextTask()).thenReturn(Optional.empty());



        assertThrows(BadRequestException.class, ()->{
            taskService.changeTaskPosition(1l, Optional.ofNullable(2l), Optional.ofNullable(3l), 1l);
        });
    }

    @Test
    void shouldThrowBadRequestExcWhenDifferentBoardsAreSameAndNextTaskIsMissingChangingTaskPosition() {
        TaskEntity currTask = spy(TaskEntity.builder()
                .name("curr task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .id(1l)
                        .name("curr task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name("curr board")
                                .personId(1l)
                                .build())
                        .build())
                .build());
        TaskEntity previousTask = TaskEntity.builder()
                .name("previous task")
                .id(2l)
                .taskState(TaskStateEntity.builder()
                        .id(2l)
                        .name(" task state")
                        .board(BoardEntity.builder()
                                .id(2l)
                                .name(" board")
                                .personId(1l)
                                .build())
                        .build())
                .build();

        when(taskRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(currTask))
                .thenReturn(Optional.ofNullable(previousTask))
                .thenReturn(Optional.empty());
        when(currTask.getPreviousTask()).thenReturn(Optional.empty());
        when(currTask.getNextTask()).thenReturn(Optional.empty());



        assertThrows(BadRequestException.class, ()->{
            taskService.changeTaskPosition(1l, Optional.ofNullable(2l), Optional.ofNullable(3l), 1l);
        });
    }
    @Test
    void shouldThrowBadRequestExcWhenDifferentBoardsAreSameAndNextPreviousTaskIsMissingChangingTaskPositionV3() {
        TaskEntity currTask = spy(TaskEntity.builder()
                .name("curr task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .id(1l)
                        .name("curr task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name("curr board")
                                .personId(1l)
                                .build())
                        .build())
                .build());
        TaskEntity nextTask = TaskEntity.builder()
                .name("next task")
                .id(3l)
                .taskState(TaskStateEntity.builder()
                        .id(2l)
                        .name(" task state")
                        .board(BoardEntity.builder()
                                .id(2l)
                                .name(" board")
                                .personId(1l)
                                .build())
                        .build())
                .build();

        when(taskRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(currTask))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.ofNullable(nextTask));
        when(currTask.getPreviousTask()).thenReturn(Optional.empty());
        when(currTask.getNextTask()).thenReturn(Optional.empty());



        assertThrows(BadRequestException.class, ()->{
            taskService.changeTaskPosition(1l, Optional.ofNullable(2l), Optional.ofNullable(3l), 1l);
        });
    }

    @Test
    void shouldChangeTaskPosition(){
        TaskEntity currTask = spy(TaskEntity.builder()
                .name("curr task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .id(1l)
                        .name("curr task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name("curr board")
                                .personId(1l)
                                .build())
                        .build())
                .build());
        TaskEntity previousTask = spy(TaskEntity.builder()
                .name("previous task")
                .id(2l)
                .taskState(TaskStateEntity.builder()
                        .id(2l)
                        .name(" task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name(" board")
                                .personId(1l)
                                .build())
                        .build())
                .build());
        TaskEntity nextTask = spy(TaskEntity.builder()
                .name("next task")
                .id(3l)
                .taskState(TaskStateEntity.builder()
                        .id(2l)
                        .name(" task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name(" board")
                                .personId(1l)
                                .build())
                        .build())
                .build());

        when(taskRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(currTask))
                .thenReturn(Optional.ofNullable(previousTask))
                .thenReturn(Optional.ofNullable(nextTask));
        when(currTask.getPreviousTask()).thenReturn(Optional.empty());
        when(currTask.getNextTask()).thenReturn(Optional.empty());

        taskService.changeTaskPosition(1l, Optional.ofNullable(2l), Optional.ofNullable(3l), 1l);

        verify(taskRepo, times(3)).save(any(TaskEntity.class));
        verify(currTask).setPreviousTask(any(TaskEntity.class));
        verify(currTask).setNextTask(any(TaskEntity.class));
        verify(previousTask).setNextTask(any(TaskEntity.class));
        verify(nextTask).setPreviousTask(any(TaskEntity.class));
    }

    @Test
    void shouldChangeTaskPositionWhenPreviousTaskIsMissing(){
        TaskEntity currTask = spy(TaskEntity.builder()
                .name("curr task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .id(1l)
                        .name("curr task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name("curr board")
                                .personId(1l)
                                .build())
                        .build())
                .build());
        TaskEntity nextTask = spy(TaskEntity.builder()
                .name("next task")
                .id(3l)
                .taskState(TaskStateEntity.builder()
                        .id(2l)
                        .name(" task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name(" board")
                                .personId(1l)
                                .build())
                        .build())
                .build());

        when(taskRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(currTask))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.ofNullable(nextTask));
        when(currTask.getPreviousTask()).thenReturn(Optional.empty());
        when(currTask.getNextTask()).thenReturn(Optional.empty());

        taskService.changeTaskPosition(1l, Optional.ofNullable(2l), Optional.ofNullable(3l), 1l);

        verify(taskRepo, times(2)).save(any(TaskEntity.class));
        verify(currTask).setPreviousTask(null);
        verify(currTask).setNextTask(any(TaskEntity.class));
        verify(nextTask).setPreviousTask(any(TaskEntity.class));
    }

    @Test
    void shouldChangeTaskPositionWhenNextTaskIsMissing(){
        TaskEntity currTask = spy(TaskEntity.builder()
                .name("curr task")
                .id(1l)
                .taskState(TaskStateEntity.builder()
                        .id(1l)
                        .name("curr task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name("curr board")
                                .personId(1l)
                                .build())
                        .build())
                .build());
        TaskEntity previousTask = spy(TaskEntity.builder()
                .name("previous task")
                .id(2l)
                .taskState(TaskStateEntity.builder()
                        .id(2l)
                        .name(" task state")
                        .board(BoardEntity.builder()
                                .id(1l)
                                .name(" board")
                                .personId(1l)
                                .build())
                        .build())
                .build());
        when(taskRepo.findById(anyLong()))
                .thenReturn(Optional.ofNullable(currTask))
                .thenReturn(Optional.ofNullable(previousTask))
                .thenReturn(Optional.empty());
        when(currTask.getPreviousTask()).thenReturn(Optional.empty());
        when(currTask.getNextTask()).thenReturn(Optional.empty());

        taskService.changeTaskPosition(1l, Optional.ofNullable(2l), Optional.ofNullable(3l), 1l);

        verify(taskRepo, times(2)).save(any(TaskEntity.class));
        verify(currTask).setPreviousTask(any(TaskEntity.class));
        verify(currTask).setNextTask(null);
        verify(previousTask).setNextTask(any(TaskEntity.class));
    }
}