package com.tasktracker.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name ="task_state")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TaskStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne
    private TaskStateEntity leftTaskState;

    @OneToOne
    private TaskStateEntity rightTaskState;

    @ManyToOne
    @JoinColumn(name = "board_id", referencedColumnName = "id")
    private BoardEntity board;

    @OneToMany(mappedBy = "taskState", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<TaskEntity> tasks = new ArrayList<>();

    public Optional<TaskStateEntity> getLeftTaskState(){
        return Optional.ofNullable(leftTaskState);
    }
    public Optional<TaskStateEntity> getRightTaskState(){
        return Optional.ofNullable(rightTaskState);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskStateEntity that = (TaskStateEntity) o;
        return id.equals(that.id) && Objects.equals(name, that.name) && Objects.equals(leftTaskState, that.leftTaskState) && Objects.equals(rightTaskState, that.rightTaskState) && board.equals(that.board) && Objects.equals(tasks, that.tasks);
    }

}
