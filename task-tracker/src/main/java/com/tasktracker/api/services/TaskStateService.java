package com.tasktracker.api.services;

import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.TaskStateDto;
import com.tasktracker.api.exceptions.BadRequestException;
import com.tasktracker.api.exceptions.NotFoundException;
import com.tasktracker.api.factories.TaskStateDtoFactory;
import com.tasktracker.store.entities.BoardEntity;
import com.tasktracker.store.entities.TaskStateEntity;
import com.tasktracker.store.repositories.BoardRepo;
import com.tasktracker.store.repositories.TaskStateRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskStateService {
    private final TaskStateRepo taskStateRepo;
    private final TaskStateDtoFactory taskStateDtoFactory;

    private final BoardRepo boardRepo;

     private BoardEntity getBoardOrThrowException(Long boardId, Long personId) {
        BoardEntity boardEntity = boardRepo.findByPersonIdAndId(personId, boardId)
                .orElseThrow(() -> {
                    throw new NotFoundException(String.format("Board with id \"%d\" was no found", boardId));
                });
        return boardEntity;
    }

    public List<TaskStateEntity> getTaskStates(Long boardId, Long personId) {
        return getBoardOrThrowException(boardId, personId).getTaskStates();
    }

    @Transactional
    public TaskStateDto createTaskState(Long boardId, String taskStateName, Long personId) {
        if (taskStateName.isBlank()) {
            throw new BadRequestException("Task state name can't be empty.");
        }

        BoardEntity board = getBoardOrThrowException(boardId, personId);


        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();
        for (TaskStateEntity taskState: board.getTaskStates()) {

            if (taskState.getName().equalsIgnoreCase(taskStateName)) {
                throw new BadRequestException(String.format("Task state %s already exists.", taskStateName));
            }

            if (!taskState.getNextTaskState().isPresent()) {
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
                    taskState.setPreviousTaskState(anotherTaskState);
                    anotherTaskState.setNextTaskState(taskState);
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


        taskStateRepo.findTaskStateEntityByBoardIdAndNameIgnoreCase(
                        taskStateEntity.getBoard().getId(), updatedTaskStateName)
        .filter(anotherTaskState->!anotherTaskState.getId().equals(taskStateId))
                .ifPresent(it->{
                    throw new BadRequestException(
                            String.format("Board %s already exists.",updatedTaskStateName)
                    );
                });


        taskStateEntity.setName(updatedTaskStateName);
        final TaskStateEntity savedTaskState = taskStateRepo.saveAndFlush(taskStateEntity);
        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);
    }

    @Transactional
    public AnswerDto deleteTuskState(Long taskStateId, Optional<Boolean> deleteAll, Long personId) {
        TaskStateEntity taskStateEntity = getTaskStateOrThrowException(taskStateId, personId);

        if (deleteAll.isPresent() && deleteAll.get().equals(true)){
            taskStateRepo.deleteAllByBoardId(taskStateEntity.getBoard().getId());
            return AnswerDto.makeDefault(true);
        }

        Optional<TaskStateEntity> leftValue = taskStateEntity.getPreviousTaskState();
        Optional<TaskStateEntity> rightValue = taskStateEntity.getNextTaskState();

        if (leftValue.isEmpty() && rightValue.isEmpty()){
            taskStateRepo.deleteById(taskStateId);
        } else if (leftValue.isPresent() && rightValue.isPresent()){
            TaskStateEntity rightTaskState = rightValue.get();
            TaskStateEntity leftTaskState = leftValue.get();

            leftTaskState.setNextTaskState(rightTaskState);
            rightTaskState.setPreviousTaskState(leftTaskState);

            taskStateEntity.setNextTaskState(null);
            taskStateEntity.setPreviousTaskState(null);
            taskStateRepo.save(leftTaskState);
            taskStateRepo.save(rightTaskState);

            taskStateRepo.deleteById(taskStateId);
        }else if(leftValue.isEmpty()){
            TaskStateEntity rightTaskState = rightValue.get();
            rightTaskState.setPreviousTaskState(null);
            taskStateRepo.save(rightTaskState);
            taskStateRepo.deleteById(taskStateId);
        }  else {
            TaskStateEntity leftTaskState =  leftValue.get();
            leftTaskState.setNextTaskState(null);
            taskStateRepo.save(leftTaskState);
            taskStateRepo.deleteById(taskStateId);
        }
        return AnswerDto.makeDefault(true);
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

    public List<TaskStateEntity> toSort(List<TaskStateEntity> unsortedList){
        List<TaskStateEntity> sortedList=new ArrayList<>();

        TaskStateEntity firstTaskState = unsortedList
                .stream()
                .filter(taskState -> taskState.getPreviousTaskState().isEmpty())
                .findFirst()
                .get();

        sortedList.add(firstTaskState);
        TaskStateEntity currTaskState = firstTaskState.getNextTaskState().orElse(null);

        int i = 0;
        while (i != unsortedList.size() - 1){
            if (currTaskState != null){
                sortedList.add(currTaskState);
                currTaskState = currTaskState.getNextTaskState().orElse(null);
                i++;
            }else break;
        }

        return sortedList;
    }

    @Transactional
    public List<TaskStateEntity> changeTaskStatePosition(Long taskStateId,
                                                           Optional<Long> previousTaskStateId,
                                                           Optional<Long> nextTaskStateId,
                                                           Long personId)
    {
        if (previousTaskStateId.isEmpty() && nextTaskStateId.isEmpty()){
            throw new BadRequestException("previous_task_state_id and next_task_state_id should not be empty");
        }
        if (previousTaskStateId.isPresent() && nextTaskStateId.isPresent()){
            if (previousTaskStateId.get().equals(nextTaskStateId.get())){
                throw new BadRequestException("previous_task_state_id and next_task_state_id should not be same");
            }
        }

        TaskStateEntity currTaskState = getTaskStateOrThrowException(taskStateId, personId);
        Optional<TaskStateEntity> previousCurrTaskState = currTaskState.getPreviousTaskState();
        Optional<TaskStateEntity> nextCurrTaskState = currTaskState.getNextTaskState();

        previousCurrTaskState.ifPresent(taskState -> {
            if (nextCurrTaskState.isPresent()){
                taskState.setNextTaskState(nextCurrTaskState.get());
            }else {
                taskState.setNextTaskState(null);
            }
            taskStateRepo.save(taskState);
        });

       nextCurrTaskState.ifPresent(taskState -> {
            if (previousCurrTaskState.isPresent()){
                taskState.setPreviousTaskState(previousCurrTaskState.get());
            }else {
                taskState.setPreviousTaskState(null);
            }
            taskStateRepo.save(taskState);
        });


       Optional<TaskStateEntity> previousTaskState = (previousTaskStateId.isPresent()) ?
               Optional.ofNullable(getTaskStateOrThrowException(previousTaskStateId.get(), personId)) : Optional.empty();

       Optional<TaskStateEntity> nextTaskState = (nextTaskStateId.isPresent()) ?
                Optional.ofNullable(getTaskStateOrThrowException(nextTaskStateId.get(), personId)) : Optional.empty();


       if (previousTaskState.isEmpty()){
           nextTaskState.ifPresent(taskState -> {
               if(!taskState.getBoard().getId().equals(currTaskState.getBoard().getId())){
                   throw new BadRequestException(String.format("Task state with id %d is from different board", taskState.getId()));
               }
               taskState.setPreviousTaskState(currTaskState);
               currTaskState.setNextTaskState(taskState);
               taskStateRepo.save(taskState);
           });
           currTaskState.setPreviousTaskState(null);
           taskStateRepo.save(currTaskState);
       }else if (nextTaskState.isEmpty()){
           previousTaskState.ifPresent(taskState -> {
               if(!taskState.getBoard().getId().equals(currTaskState.getBoard().getId())){
                   throw new BadRequestException(String.format("Task state with id %d is from different board", taskState.getId()));
               }
               taskState.setNextTaskState(currTaskState);
               currTaskState.setPreviousTaskState(taskState);
               taskStateRepo.save(taskState);
           });
           currTaskState.setNextTaskState(null);
           taskStateRepo.save(currTaskState);
       }else {
           if(!previousTaskState.get().getBoard().getId().equals(currTaskState.getBoard().getId())){
               throw new BadRequestException(String.format("Task state with id %d is from different board",
                      previousTaskState.get().getId()));
           }
           if(!nextTaskState.get().getBoard().getId().equals(currTaskState.getBoard().getId())){
               throw new BadRequestException(String.format("Task state with id %d is from different board",
                      nextCurrTaskState.get().getId()));
           }
           previousTaskState.get().setNextTaskState(currTaskState);
           nextTaskState.get().setPreviousTaskState(currTaskState);
           taskStateRepo.save(previousTaskState.get());
           taskStateRepo.save(nextTaskState.get());

           currTaskState.setPreviousTaskState(previousTaskState.get());
           currTaskState.setNextTaskState(nextTaskState.get());
           taskStateRepo.save(currTaskState);
       }

       return currTaskState.getBoard().getTaskStates();
    }


}
