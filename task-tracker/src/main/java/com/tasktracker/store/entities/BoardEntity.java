package com.tasktracker.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name ="board")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "person_id")
    private Long personId;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private List<TaskStateEntity> taskStates;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardEntity that = (BoardEntity) o;
        return id.equals(that.id) && name.equals(that.name) && Objects.equals(taskStates, that.taskStates);
    }

}
