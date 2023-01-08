package com.tasktracker.api.factories;

import com.tasktracker.api.dto.TaskStateDto;
import com.tasktracker.api.services.TaskService;
import com.tasktracker.store.entities.TaskStateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskStateDtoFactory {

    private final TaskDtoFactory taskDtoFactory;

    public TaskStateDto makeTaskStateDto(TaskStateEntity taskState){
        return TaskStateDto.builder()
                .id(taskState.getId())
                .name(taskState.getName())
                .leftTaskStateId(taskState.getLeftTaskState().map(TaskStateEntity::getId).orElse(null))
                .rightTaskStateId(taskState.getRightTaskState().map(TaskStateEntity::getId).orElse(null))
                .tasks(
                        taskState.getTasks()
                        .stream()
                        .map(taskDtoFactory::makeTaskDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public List<TaskStateDto> makeTaskStateDtoList(List<TaskStateEntity> list){
        return list.stream()
                .map(this::makeTaskStateDto)
                .collect(Collectors.toList());
    }
}
