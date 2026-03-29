package com.blinders.blinders.controller;

import com.blinders.blinders.dto.ApiResponse;
import com.blinders.blinders.entity.Level;
import com.blinders.blinders.entity.Skill;
import com.blinders.blinders.repository.LevelRepository;
import com.blinders.blinders.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelRepository levelRepository;
    private final SkillRepository skillRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Level>>> getAllLevels() {
        List<Level> levels = levelRepository.findAllByOrderByOrderAsc();
        return ResponseEntity.ok(new ApiResponse<>(true, "Levels retrieved", levels));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Level>> getLevelById(@PathVariable Long id) {
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Level not found"));
        return ResponseEntity.ok(new ApiResponse<>(true, "Level retrieved", level));
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByLevel(@PathVariable Long id) {
        List<Skill> skills = skillRepository.findByLevelId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Skills retrieved", skills));
    }
}
