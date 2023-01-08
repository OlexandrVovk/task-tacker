package com.tasktracker.api.dto;

import jakarta.persistence.Column;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class BoardDto {

    @NonNull
    private Long id;

    @NonNull
    private String name;
}
