package com.tasktracker.api.services;

import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.TaskDto;
import com.tasktracker.api.exceptions.BadRequestException;
import com.tasktracker.api.exceptions.NotFoundException;
import com.tasktracker.api.factories.TaskDtoFactory;
import com.tasktracker.store.entities.TaskEntity;
import com.tasktracker.store.entities.TaskStateEntity;
import com.tasktracker.store.repositories.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepo taskRepo;
    private final TaskDtoFactory taskDtoFactory;

    private final TaskStateService taskStateService;

    @Transactional
    public TaskDto createTask(Long taskStateId, String taskName, Long personId) {
        TaskStateEntity taskStateEntity = taskStateService.getTaskStateOrThrowException(taskStateId, personId);

        Optional<TaskEntity> optionalTaskEntity = Optional.empty();
        for (TaskEntity task : taskStateEntity.getTasks()){
            if (task.getName().equals(taskName)){
                throw new BadRequestException(String.format("Task with name %s already exists", taskName));
            }

            if (!task.getRightTask().isPresent()){
                optionalTaskEntity = Optional.of(task);
            }
        }

        TaskEntity taskEntity = taskRepo.saveAndFlush(
                TaskEntity.builder()
                        .name(taskName)
                        .taskState(taskStateEntity)
                        .build()
        );

        optionalTaskEntity.ifPresent(anotherTask ->{
             taskEntity.setLeftTask(anotherTask);
             anotherTask.setRightTask(taskEntity);
             taskRepo.saveAndFlush(anotherTask);
        });

        final TaskEntity savedTask = taskRepo.saveAndFlush(taskEntity);

        return taskDtoFactory.makeTaskDto(savedTask);
    }

    public List<TaskEntity> getTasks(Long taskStateId, Long personId) {
        TaskStateEntity taskStateEntity = taskStateService.getTaskStateOrThrowException(taskStateId, personId);
        return taskStateEntity.getTasks();
    }

    @Transactional
    public AnswerDto deleteTask(Long taskId, Long personId) {
        TaskEntity taskEntity = getTaskOrThrowException(taskId, personId);

        Optional<TaskEntity> optionalLeftTask = taskEntity.getLeftTask();
        Optional<TaskEntity> optionalRightTask = taskEntity.getRightTask();

        if (optionalLeftTask.isEmpty() && optionalRightTask.isEmpty()){
            //один елемент в списку
            taskRepo.deleteById(taskId);
        } else if (optionalLeftTask.isPresent() && optionalRightTask.isPresent()){
            TaskEntity leftTask = optionalLeftTask.get();
            TaskEntity rightTask = optionalRightTask.get();

            leftTask.setRightTask(rightTask);
            rightTask.setLeftTask(leftTask);
            taskRepo.save(leftTask);
            taskRepo.save(rightTask);

            taskRepo.deleteById(taskId);
            // середній
        } else if (optionalLeftTask.isPresent() && optionalRightTask.isEmpty()){
            TaskEntity leftTask = optionalLeftTask.get();

            leftTask.setRightTask(null);
            taskRepo.save(leftTask);

            taskRepo.deleteById(taskId);
            // кінець
        }else if (optionalLeftTask.isEmpty() && optionalRightTask.isPresent()){
            TaskEntity rightTask = optionalRightTask.get();

            rightTask.setLeftTask(null);
            taskRepo.save(rightTask);

            taskRepo.deleteById(taskId);
        }

        return AnswerDto.makeDefault(true);
    }

    public TaskEntity getTaskOrThrowException(Long taskId){
       return taskRepo.findById(taskId).orElseThrow(
               () -> {
                   throw new NotFoundException(String.format("Task with id %d was not found", taskId));
               });
    }

    public TaskEntity getTaskOrThrowException(Long taskId, Long personId){
        TaskEntity taskEntity  = taskRepo.findById(taskId).orElseThrow(
                () -> {
                    throw new NotFoundException(String.format("Task with id %d was not found", taskId));
                });
        if (!taskEntity.getTaskState().getBoard().getPersonId().equals(personId)){
            throw new NotFoundException(String.format("Task with id %d was not found", taskId));
        }
        return taskEntity;
    }

    @Transactional
    public TaskDto update(TaskDto taskDto, Long personId) {
        TaskEntity taskEntity = getTaskOrThrowException(taskDto.getId(), personId);

        if (taskEntity.getName()==null){
            if (!(taskDto.getName()==null)){
                taskEntity.setName(taskDto.getName());
            }
        }else {
            if (!(taskDto.getName()==null)){
                taskEntity.setName(taskDto.getName());
            }
        }

        if (taskDto.getDescription()==null){
            if (!(taskDto.getDescription()==null)){
                taskEntity.setDescription(taskDto.getDescription());
            }
        }else {
            if (!(taskDto.getDescription()==null)){
                taskEntity.setDescription(taskDto.getDescription());
            }
        }

        final TaskEntity savedTask = taskRepo.saveAndFlush(taskEntity);
        return TaskDto.builder()
                .id(taskEntity.getId())
                .name(taskEntity.getName())
                .description(taskEntity.getDescription())
                .build();
    }


    @Transactional
    public TaskDto changeTaskPosition(Long taskId,
                                       Optional<Long> previousTaskId,
                                       Optional<Long> nextTaskId,
                                       Long personId ){
        if (previousTaskId.isEmpty() && nextTaskId.isEmpty()){
            throw new BadRequestException("Previous task id and next task id should not be empty");
        }

        TaskEntity currTask = getTaskOrThrowException(taskId, personId);
        Optional<TaskEntity> previousCurrTask = currTask.getLeftTask();
        Optional<TaskEntity> nextCurrTask = currTask.getRightTask();
        previousCurrTask.ifPresent(task -> {
            if (nextCurrTask.isPresent()){
                task.setRightTask(nextCurrTask.get());
            }else {
                task.setRightTask(null);
            }
            taskRepo.save(task);
        });

        nextCurrTask.ifPresent(task -> {
            if (previousCurrTask.isPresent()){
                task.setLeftTask(previousCurrTask.get());
            } else {
                task.setLeftTask(null);
            }
            taskRepo.save(task);
        });

        Optional<TaskEntity> previousTask = (previousTaskId.isPresent()) ?
                taskRepo.findById(previousTaskId.get()) : Optional.empty();

        Optional<TaskEntity> nextTask = (nextTaskId.isPresent()) ?
                taskRepo.findById(nextTaskId.get()) : Optional.empty();

        if (previousTask.isEmpty()){
            nextTask.ifPresent(task -> {
                task.setLeftTask(currTask);
                currTask.setRightTask(task);
                taskRepo.save(currTask);
            });
            currTask.setLeftTask(null);
            taskRepo.save(currTask);

        }else if (nextTask.isEmpty()){
            previousTask.ifPresent(task -> {
                task.setRightTask(currTask);
                currTask.setLeftTask(task);
                taskRepo.save(task);
            });
            currTask.setRightTask(null);
            taskRepo.save(currTask);
        }else {
            previousTask.get().setRightTask(currTask);
            nextTask.get().setLeftTask(currTask);
            taskRepo.save(previousTask.get());
            taskRepo.save(nextTask.get());

            currTask.setLeftTask(previousTask.get());
            currTask.setRightTask(nextTask.get());
            taskRepo.save(currTask);
        }

        return taskDtoFactory.makeTaskDto(currTask);
    }
    public List<TaskEntity> toSort(List<TaskEntity> unsortedList){
        List<TaskEntity> sortedList=new ArrayList<>();

        TaskEntity firstTask = unsortedList
                .stream()
                .filter(task -> task.getLeftTask().isEmpty())
                .findFirst()
                .orElse(unsortedList.get(0));

        sortedList.add(firstTask);
        TaskEntity currTask = firstTask.getRightTask().orElse(null);

        int i = 0;
        while (i != unsortedList.size() - 1){
            if (currTask != null){
                sortedList.add(currTask);
                currTask = currTask.getRightTask().orElse(null);
                i++;
            }else break;
        }

        return sortedList;
    }

}
