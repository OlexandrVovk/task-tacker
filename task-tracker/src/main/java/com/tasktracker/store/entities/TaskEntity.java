package com.tasktracker.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Entity
@Table(name ="task")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "task_state_id" ,referencedColumnName = "id")
    private TaskStateEntity taskState;

    @OneToOne
//    @Column(name = "left_task_id")
    private TaskEntity previousTask;

    @OneToOne
//    @Column(name = "right_task_id")
    private TaskEntity nextTask;

    public Optional<TaskEntity> getPreviousTask(){
        return Optional.ofNullable(previousTask);
    }

    public Optional<TaskEntity> getNextTask(){
        return Optional.ofNullable(nextTask);
    }
}
