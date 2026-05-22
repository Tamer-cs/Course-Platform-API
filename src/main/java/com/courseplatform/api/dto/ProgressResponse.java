package com.courseplatform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressResponse {
    private Long id;
    private String subtopicId;
    private Long userId;
    private String completedAt; // ISO string
}
