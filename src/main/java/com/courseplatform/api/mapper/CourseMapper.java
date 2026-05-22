package com.courseplatform.api.mapper;

import com.courseplatform.api.dto.CourseResponse;
import com.courseplatform.api.dto.SubtopicResponse;
import com.courseplatform.api.dto.TopicResponse;
import com.courseplatform.api.model.Course;
import com.courseplatform.api.model.Subtopic;
import com.courseplatform.api.model.Topic;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public final class CourseMapper {

    private CourseMapper() {}

    public static CourseResponse toDto(Course course) {
        if (course == null) return null;
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .topics(course.getTopics() == null ? Collections.emptyList() :
                        course.getTopics().stream().map(CourseMapper::toDto).collect(Collectors.toList()))
                .build();
    }

    public static TopicResponse toDto(Topic topic) {
        if (topic == null) return null;
        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .subtopics(Collections.emptyList())
                .build();
    }

    public static TopicResponse toDto(Topic topic, List<Subtopic> subtopics) {
        if (topic == null) return null;
        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .subtopics(subtopics == null ? Collections.emptyList() : subtopics.stream().map(CourseMapper::toDto).collect(Collectors.toList()))
                .build();
    }

    public static CourseResponse toSummaryDto(Course course) {
        if (course == null) return null;
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .topics(course.getTopics() == null ? Collections.emptyList() : course.getTopics().stream().map(CourseMapper::toDto).collect(Collectors.toList()))
                .build();
    }

    public static SubtopicResponse toDto(Subtopic subtopic) {
        if (subtopic == null) return null;
        String snippet = null;
        if (subtopic.getContent() != null) {
            int len = Math.min(160, subtopic.getContent().length());
            snippet = subtopic.getContent().substring(0, len) + (subtopic.getContent().length() > len ? "..." : "");
        }
        return SubtopicResponse.builder()
                .id(subtopic.getId())
                .title(subtopic.getTitle())
                .snippet(snippet)
                .build();
    }
}
