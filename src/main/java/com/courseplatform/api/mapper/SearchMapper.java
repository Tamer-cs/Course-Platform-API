package com.courseplatform.api.mapper;

import com.courseplatform.api.dto.SearchResultDTO;

public class SearchMapper {
    public static SearchResultDTO of(String courseId, String courseTitle, String topicTitle,
                                     String subtopicId, String subtopicTitle, String snippet,
                                     double score, boolean fuzzy) {
        return SearchResultDTO.builder()
                .courseId(courseId)
                .courseTitle(courseTitle)
                .topicTitle(topicTitle)
                .subtopicId(subtopicId)
                .subtopicTitle(subtopicTitle)
                .snippet(snippet)
                .score(score)
                .fuzzy(fuzzy)
                .build();
    }
}
