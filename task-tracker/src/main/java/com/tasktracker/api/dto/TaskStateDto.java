package com.tasktracker.api.dto;

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

    private Long previousTaskStateId;

    private Long nextTaskStateId;

    private List<TaskDto> tasks;
}
