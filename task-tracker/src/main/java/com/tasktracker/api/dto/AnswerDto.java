package com.tasktracker.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {

    private boolean answer;

    public static AnswerDto makeDefault(boolean answer){
        return AnswerDto.builder()
                .answer(answer)
                .build();
    }
}
