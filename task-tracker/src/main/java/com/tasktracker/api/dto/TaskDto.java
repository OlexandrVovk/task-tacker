package com.tasktracker.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskDto {

    @NonNull
    private Long id;

    private String name;

    private String description;

    private Long previousTaskId;

    private Long nextTaskId;

}
