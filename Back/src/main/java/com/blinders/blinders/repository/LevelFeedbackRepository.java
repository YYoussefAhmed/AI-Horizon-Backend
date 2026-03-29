package com.blinders.blinders.repository;

import com.blinders.blinders.entity.LevelFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelFeedbackRepository extends JpaRepository<LevelFeedback, Long> {
    List<LevelFeedback> findByLevelId(Long levelId);

    Optional<LevelFeedback> findByUser_UserIdAndLevel_Id(Long userId, Long levelId);
}
