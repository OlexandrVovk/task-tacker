package com.tasktracker.api.services;

import com.sun.source.util.TaskListener;
import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.TaskStateDto;
import com.tasktracker.api.exceptions.BadRequestException;
import com.tasktracker.api.exceptions.NotFoundException;
import com.tasktracker.api.factories.TaskStateDtoFactory;
import com.tasktracker.store.entities.BoardEntity;
import com.tasktracker.store.entities.TaskStateEntity;
import com.tasktracker.store.repositories.TaskStateRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskStateService {
    private final TaskStateRepo taskStateRepo;
    private final TaskStateDtoFactory taskStateDtoFactory;
    private final BoardService boardService;

    public List<TaskStateEntity> getTaskStates(Long boardId, Long personId) {
        return  boardService.getBoardOrThrowException(boardId, personId).getTaskStates();
    }

    @Transactional
    public TaskStateDto createTaskState(Long boardId, String taskStateName, Long personId) {
        if (taskStateName.isBlank()) {
            throw new BadRequestException("Task state name can't be empty.");
        }

        BoardEntity board = boardService.getBoardOrThrowException(boardId, personId);


        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();
        for (TaskStateEntity taskState: board.getTaskStates()) {

            if (taskState.getName().equalsIgnoreCase(taskStateName)) {
                throw new BadRequestException(String.format("Task state %s already exists.", taskStateName));
            }

            if (!taskState.getRightTaskState().isPresent()) {
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }
        }

        TaskStateEntity taskState = taskStateRepo.saveAndFlush(
                TaskStateEntity.builder()
                        .name(taskStateName)
                        .board(board)
                        .build()
        );

        optionalAnotherTaskState
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);
                    anotherTaskState.setRightTaskState(taskState);
                    taskStateRepo.saveAndFlush(anotherTaskState);
                });

        final TaskStateEntity savedTaskState = taskStateRepo.saveAndFlush(taskState);
        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);
    }

    @Transactional
    public TaskStateDto updateTaskState(Long taskStateId, String updatedTaskStateName, Long personId) {
        if (updatedTaskStateName.isBlank()) {
            throw new BadRequestException("Task state name can't be empty.");
        }
        TaskStateEntity taskStateEntity = getTaskStateOrThrowException(taskStateId, personId);

        taskStateRepo
                .findTaskStateEntityByBoardIdAndNameIgnoreCase(
                        taskStateEntity.getBoard().getId(), updatedTaskStateName)
                .filter(anotherTaskState->!anotherTaskState.getId().equals(taskStateId))
                .ifPresent(it->{
                    throw new BadRequestException(
                            String.format("Board %s already exists. Can't change name to the same name",updatedTaskStateName)
                    );
                });


        taskStateEntity.setName(updatedTaskStateName);
        final TaskStateEntity savedTaskState = taskStateRepo.saveAndFlush(taskStateEntity);
        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);
    }

    @Transactional
    public AnswerDto deleteTuskState(Long taskStateId, Optional<Boolean> deleteAll, Long personId) {
        TaskStateEntity taskStateEntity = getTaskStateOrThrowException(taskStateId, personId);

        if (deleteAll.isPresent()){
            taskStateRepo.deleteAllByBoardId(taskStateEntity.getBoard().getId());
            return AnswerDto.makeDefault(true);
        }

        Optional<TaskStateEntity> leftValue = taskStateEntity.getLeftTaskState();
        Optional<TaskStateEntity> rightValue = taskStateEntity.getRightTaskState();

        if (leftValue.isEmpty() && rightValue.isEmpty()){
            taskStateRepo.deleteById(taskStateId);
        } else if (leftValue.isPresent() && rightValue.isPresent()){
            TaskStateEntity rightTaskState = rightValue.get();
            TaskStateEntity leftTaskState = leftValue.get();

            leftTaskState.setRightTaskState(rightTaskState);
            rightTaskState.setLeftTaskState(leftTaskState);

            taskStateEntity.setRightTaskState(null);
            taskStateEntity.setRightTaskState(null);
            taskStateRepo.save(leftTaskState);
            taskStateRepo.save(rightTaskState);

            taskStateRepo.deleteById(taskStateId);
        }else if(leftValue.isEmpty()){
            TaskStateEntity rightTaskState = rightValue.get();
            rightTaskState.setLeftTaskState(null);
            taskStateRepo.save(rightTaskState);
            taskStateRepo.deleteById(taskStateId);
        }  else {
            TaskStateEntity leftTaskState =  leftValue.get();
            leftTaskState.setRightTaskState(null);
            taskStateRepo.save(leftTaskState);
            taskStateRepo.deleteById(taskStateId);
        }
        return AnswerDto.makeDefault(true);
    }

    public TaskStateEntity getTaskStateOrThrowException(Long id){
        return taskStateRepo.findById(id)
                .orElseThrow(()->{
                    throw new NotFoundException(
                            String.format("No task state with id %d exists", id)
                    );
                });
    }
    public TaskStateEntity getTaskStateOrThrowException(Long id, Long personId){

        TaskStateEntity taskStateEntity = taskStateRepo.findById(id)
                .orElseThrow(()->{
                    throw new NotFoundException(
                            String.format("No task state with id %d exists", id)
                    );
                });
        if (!taskStateEntity.getBoard().getPersonId().equals(personId)) {
            throw new NotFoundException(
                    String.format("No task state with id %d exists", id)
            );
        }
        return taskStateEntity;
    }

    @Transactional
    public List<TaskStateEntity> changeTaskStatePosition(Long taskStateId, Long anotherTuskStateId, Long personId) {
        if (anotherTuskStateId==null){
            TaskStateEntity firstTaskState = setTaskStateToFirstPos(taskStateId, personId);
            return firstTaskState.getBoard()
                    .getTaskStates();
        }

        if (taskStateId.equals(anotherTuskStateId)) {
            throw new BadRequestException("Both id are equal");
        }

        TaskStateEntity firstTaskState = getTaskStateOrThrowException(taskStateId, personId);
        TaskStateEntity secondTaskState= getTaskStateOrThrowException(anotherTuskStateId, personId);

        if (!firstTaskState.getBoard().getId().equals(secondTaskState.getBoard().getId())){
            throw new BadRequestException("Task states are from different boards");
        }

        List<TaskStateEntity> taskStateEntityList = toSort(firstTaskState.getBoard().getTaskStates());
        TaskStateEntity resultTaskState;
        for (TaskStateEntity taskState : taskStateEntityList) {
            if(taskState.getId().equals(firstTaskState.getId())){
                resultTaskState = setTaskStateToTheRight(firstTaskState,secondTaskState);
                break;
            }else if (taskState.getId().equals(secondTaskState.getId())){
                resultTaskState = setTaskStateToTheLeft(firstTaskState,secondTaskState);
                break;
            }
        }

        return firstTaskState.getBoard().getTaskStates();
    }

    @Transactional
    TaskStateEntity setTaskStateToFirstPos(Long taskStateId, Long personId){
        TaskStateEntity taskStateEntity = getTaskStateOrThrowException(taskStateId, personId);

        if (taskStateEntity.getBoard().getTaskStates().size() == 1) return taskStateEntity;
        if (taskStateEntity.getLeftTaskState().isEmpty()) return taskStateEntity;

        Optional<TaskStateEntity> leftTaskState = taskStateEntity.getLeftTaskState();
        Optional<TaskStateEntity> rightTaskState = taskStateEntity.getRightTaskState();

        leftTaskState.ifPresent(taskState ->{
            rightTaskState.ifPresentOrElse(
                    rightTaskStateEntity -> taskState.setRightTaskState(rightTaskStateEntity),
                    () -> taskState.setRightTaskState(null)
            );
            taskStateRepo.saveAndFlush(taskState);
        });

        rightTaskState.ifPresent(taskState ->{
            leftTaskState.ifPresentOrElse(
                    leftTaskStateEntity -> taskState.setLeftTaskState(leftTaskStateEntity),
                    () -> taskState.setRightTaskState(null)
            );
            taskStateRepo.saveAndFlush(taskState);
        });

        Optional<TaskStateEntity> firstTaskState = Optional.ofNullable(taskStateEntity
                .getBoard()
                .getTaskStates()
                .stream()
                .filter(taskState -> {
                    return taskState.getLeftTaskState().isEmpty();
                })
                .findFirst()
                .orElse(null));

        firstTaskState.ifPresent(taskState->{
            taskState.setLeftTaskState(taskStateEntity);
            taskStateEntity.setRightTaskState(taskState);
            taskStateEntity.setLeftTaskState(null);
            taskStateRepo.save(taskState);
        });

        return taskStateRepo.saveAndFlush(taskStateEntity);
    }

    @Transactional
    TaskStateEntity setTaskStateToTheRight(TaskStateEntity firstTaskState, TaskStateEntity secondTaskState){
        //перший зліва, другий справа
        //змінити право першого на ліво першого
        //змінити право другого на перший, ліве правого другого на перший

        Optional<TaskStateEntity> firstLeftTaskState = firstTaskState.getLeftTaskState();
        Optional<TaskStateEntity> firstRightTaskState = firstTaskState.getRightTaskState();

        firstLeftTaskState.ifPresent(taskState -> {
            if (firstRightTaskState.isPresent()){
                taskState.setRightTaskState(firstRightTaskState.get());
            }else {
                taskState.setRightTaskState(null);
            }
            taskStateRepo.save(taskState);
        });

        firstRightTaskState.ifPresent(taskState -> {
            if (firstLeftTaskState.isPresent()){
                taskState.setLeftTaskState(firstLeftTaskState.get());
            }else {
                taskState.setLeftTaskState(null);
            }
            taskStateRepo.save(taskState);
        });

        Optional<TaskStateEntity> secondRightTaskState = secondTaskState.getRightTaskState();

        secondRightTaskState.ifPresentOrElse(
                taskState -> {
                    taskState.setLeftTaskState(firstTaskState);
                    firstTaskState.setRightTaskState(taskState);
                    taskState = taskStateRepo.saveAndFlush(taskState);
                }, () -> {
                    firstTaskState.setRightTaskState(null);
                }
        );

        secondTaskState.setRightTaskState(firstTaskState);
        firstTaskState.setLeftTaskState(secondTaskState);
        taskStateRepo.save(secondTaskState);
        taskStateRepo.save(firstTaskState);

        return firstTaskState;
    }

    @Transactional
    TaskStateEntity setTaskStateToTheLeft(TaskStateEntity firstTaskState, TaskStateEntity secondTaskState){
        //другий зліва, перший зправа
        //змінити право першого на ліво першого
        //змінити ліво другого на перший, право лівого другого на перший

        Optional<TaskStateEntity> fistLeftTaskState = firstTaskState.getLeftTaskState();
        Optional<TaskStateEntity> fistRightTaskState = firstTaskState.getRightTaskState();

        fistLeftTaskState.ifPresent(taskState -> {
            if ((fistRightTaskState.isPresent())) {
                taskState.setRightTaskState(fistRightTaskState.get());
            } else {
                taskState.setRightTaskState(null);
            }
            taskStateRepo.save(taskState);
        });

        fistRightTaskState.ifPresent(taskState -> {
            if(fistLeftTaskState.isPresent()){
                taskState.setLeftTaskState(fistLeftTaskState.get());
            }else {
                taskState.setLeftTaskState(null);
            }
            taskStateRepo.save(taskState);
        });

        Optional<TaskStateEntity> secondLeftTaskState = secondTaskState.getLeftTaskState();

        secondLeftTaskState.ifPresent(taskState -> {
            taskState.setRightTaskState(firstTaskState);
            firstTaskState.setLeftTaskState(taskState);
            taskState = taskStateRepo.saveAndFlush(taskState);
        });

        secondLeftTaskState.ifPresentOrElse(
                taskState -> {
                    taskState.setRightTaskState(firstTaskState);
                    firstTaskState.setLeftTaskState(taskState);
                    taskState = taskStateRepo.saveAndFlush(taskState);
                }, () -> {
                    firstTaskState.setLeftTaskState(null);
                }
        );

        secondTaskState.setLeftTaskState(firstTaskState);
        firstTaskState.setRightTaskState(secondTaskState);
        taskStateRepo.save(secondTaskState);
        taskStateRepo.save(firstTaskState);

        return firstTaskState;

    }

    public List<TaskStateEntity> toSort(List<TaskStateEntity> unsortedList){
        List<TaskStateEntity> sortedList=new ArrayList<>();

        TaskStateEntity firstTaskState = unsortedList
                .stream()
                .filter(taskState -> taskState.getLeftTaskState().isEmpty())
                .findFirst()
                .get();

        sortedList.add(firstTaskState);
        TaskStateEntity currTaskState = firstTaskState.getRightTaskState().orElse(null);

        int i = 0;
        while (i != unsortedList.size() - 1){
            if (currTaskState != null){
                sortedList.add(currTaskState);
                currTaskState = currTaskState.getRightTaskState().orElse(null);
                i++;
            }else break;
        }

        return sortedList;
    }

}
