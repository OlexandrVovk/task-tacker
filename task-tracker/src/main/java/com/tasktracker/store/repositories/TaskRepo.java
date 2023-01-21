package com.tasktracker.store.repositories;

import com.tasktracker.store.entities.TaskEntity;
import com.tasktracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepo extends JpaRepository<TaskEntity, Long> {
}
