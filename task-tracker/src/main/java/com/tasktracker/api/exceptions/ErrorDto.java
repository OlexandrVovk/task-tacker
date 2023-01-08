package com.tasktracker.api.exceptions;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ErrorDto {

    private String error;
    private String errorDescription;
}
