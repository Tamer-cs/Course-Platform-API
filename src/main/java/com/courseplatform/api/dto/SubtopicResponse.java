package com.courseplatform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtopicResponse {
    private String id;
    private String title;
    // snippet used for search/list responses
    private String snippet;
}
