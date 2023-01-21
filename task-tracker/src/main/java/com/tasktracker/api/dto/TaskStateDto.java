package com.tasktracker.api.dto;

import com.tasktracker.store.entities.TaskEntity;
import jakarta.persistence.Column;
import lombok.*;


import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TaskStateDto {

    @NonNull
    private Long id;

    @NonNull
    private String name;

    private Long leftTaskStateId;

    private Long rightTaskStateId;

    private List<TaskDto> tasks;
}
