package com.blinders.blinders.repository;

import com.blinders.blinders.entity.ExamAttempt;
import com.blinders.blinders.enums.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByUser_UserId(Long userId);

    List<ExamAttempt> findByUser_UserIdAndLevel_Id(Long userId, Long levelId);

    List<ExamAttempt> findByUser_UserIdAndExamType(Long userId, ExamType examType);
}
