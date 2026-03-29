package com.blinders.blinders.repository;

import com.blinders.blinders.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findBySkillIdOrderByOrderAsc(Long skillId);
}
