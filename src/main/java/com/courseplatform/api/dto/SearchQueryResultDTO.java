package com.courseplatform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchQueryResultDTO {
    private String courseId;
    private String courseTitle;
    private String topicTitle;
    private String subtopicId;
    private String subtopicTitle;
    private Double relevanceScore;
    private String excerptSnippet;
    private Boolean isFuzzyMatch;
}
