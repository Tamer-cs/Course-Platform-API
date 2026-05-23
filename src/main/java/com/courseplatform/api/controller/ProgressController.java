package com.courseplatform.api.controller;

import com.courseplatform.api.dto.ProgressResponse;
import com.courseplatform.api.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/complete/{subtopicId}")
    public ResponseEntity<ProgressResponse> completeSubtopic(@PathVariable String subtopicId) {
        return ResponseEntity.ok(progressService.completeSubtopic(subtopicId));
    }
}