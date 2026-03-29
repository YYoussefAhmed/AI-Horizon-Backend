package com.blinders.blinders.repository;

import com.blinders.blinders.entity.Level;
import com.blinders.blinders.enums.LevelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    Optional<Level> findByName(LevelType name);

    List<Level> findAllByOrderByOrderAsc();
}
