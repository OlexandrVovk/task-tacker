package com.tasktracker.api.factories;

import com.tasktracker.api.dto.TaskDto;
import com.tasktracker.store.entities.TaskEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskDtoFactory {

    public TaskDto makeTaskDto(TaskEntity task){
        return TaskDto.builder()
                .id(task.getId())
                .previousTaskId(task.getPreviousTask().map(TaskEntity::getId).orElse(null))
                .nextTaskId(task.getNextTask().map(TaskEntity::getId).orElse(null))
                .name(task.getName())
                .description(task.getDescription())
                .build();
    }

    public List<TaskDto> makeTaskDtoList(List<TaskEntity> list){
        return list.stream()
                .map(this::makeTaskDto)
                .collect(Collectors.toList());
    }
}
