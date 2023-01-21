package com.tasktracker.store.repositories;

import com.tasktracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TaskStateRepo extends JpaRepository<TaskStateEntity, Long> {

    Optional<TaskStateEntity> findTaskStateEntityByBoardIdAndNameIgnoreCase(Long boardId, String name);
    void deleteAllByBoardId(Long boardId);

}
