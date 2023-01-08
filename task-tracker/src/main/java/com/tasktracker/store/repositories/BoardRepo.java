package com.tasktracker.store.repositories;

import com.tasktracker.store.entities.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface BoardRepo extends JpaRepository<BoardEntity, Long> {
    Optional<BoardEntity> findByNameAndPersonId(String name, Long personId);
    List<BoardEntity> findAllByNameStartingWithIgnoreCaseAndPersonId(String name, Long personId);

    List<BoardEntity> findAllByPersonId(Long personId);

    Optional<BoardEntity> findByPersonIdAndId(Long personId, Long boardId);

    void deleteByIdAndPersonId(Long boardId, Long personId);
}
