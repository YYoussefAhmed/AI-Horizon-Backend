package com.blinders.blinders.repository;

import com.blinders.blinders.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUser_UserId(Long userId);

    Optional<UserProgress> findByUser_UserIdAndLesson_Id(Long userId, Long lessonId);

    List<UserProgress> findByUser_UserIdAndCompletedTrue(Long userId);
}
