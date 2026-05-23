package com.courseplatform.api.service;

import com.courseplatform.api.dto.SearchQueryResultDTO;
import com.courseplatform.api.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final CourseRepository courseRepository;
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    public List<SearchQueryResultDTO> search(String q) {
        return search(q, 20);
    }

    public List<SearchQueryResultDTO> search(String q, int limit) {
        if (q == null || q.isBlank()) return List.of();

        String term = q.trim();
        List<Object[]> rows = courseRepository.searchWeighted(term, limit);
        boolean fuzzy = false;
        if (rows == null || rows.isEmpty()) {
            // fallback to trigram similarity fuzzy search if available
            try {
                rows = courseRepository.searchFuzzy(term, limit);
                fuzzy = true;
            } catch (Exception e) {
                // Likely pg_trgm is not installed or database doesn't support similarity(); log and continue with empty results
                logger.warn("Fuzzy search failed for query '{}': {}", term, e.getMessage());
                rows = List.of();
            }
        }
        List<SearchQueryResultDTO> results = new ArrayList<>();

        for (Object[] row : rows) {
            String courseId = asString(row, 0);
            String courseTitle = asString(row, 1);
            String topicTitle = asString(row, 2);
            String subtopicId = asString(row, 3);
            String subtopicTitle = asString(row, 4);
            String subtopicContent = asString(row, 5);
            Number scoreNum = (Number) row[6];
            double score = scoreNum == null ? 0.0 : scoreNum.doubleValue();

            String snippetSource;
            if (fuzzy) {
                // For fuzzy results prefer the most specific title available to create a readable snippet
                snippetSource = firstNonNull(subtopicTitle, topicTitle, courseTitle, subtopicContent);
            } else {
                snippetSource = chooseSnippetSource(term, courseTitle, topicTitle, subtopicTitle, subtopicContent);
            }
            String plain = stripMarkdown(snippetSource == null ? "" : snippetSource);
            String snippet = extractSnippet(plain, term);

            SearchQueryResultDTO dto = SearchQueryResultDTO.builder()
                    .courseId(courseId)
                    .courseTitle(courseTitle)
                    .topicTitle(topicTitle)
                    .subtopicId(subtopicId)
                    .subtopicTitle(subtopicTitle)
                    .relevanceScore(score)
                    .excerptSnippet(snippet)
                    .isFuzzyMatch(fuzzy)
                    .build();

            results.add(dto);
        }

        return results;
    }

    private String asString(Object[] row, int idx) {
        if (row == null || idx < 0 || idx >= row.length) return null;
        Object o = row[idx];
        return o == null ? null : String.valueOf(o);
    }

    private String chooseSnippetSource(String term, String courseTitle, String topicTitle, String subtopicTitle, String subtopicContent) {
        String lower = term.toLowerCase();
        if (courseTitle != null && courseTitle.toLowerCase().contains(lower)) return courseTitle;
        if (topicTitle != null && topicTitle.toLowerCase().contains(lower)) return topicTitle;
        if (subtopicTitle != null && subtopicTitle.toLowerCase().contains(lower)) return subtopicTitle;
        return subtopicContent;
    }

    private String stripMarkdown(String input) {
        if (input == null) return null;
        // Remove common markdown tokens and excessive whitespace
        String cleaned = input.replaceAll("(?m)```.*?```", " ") // code blocks
                .replaceAll("(?m)`.+?`", " ") // inline code
                .replaceAll("[#>*\\-]{1,3}", " ") // headers, blockquotes, bullets
                .replaceAll("\\!\\[.*?\\]\\(.*?\\)", " ") // images
                .replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "$1") // links -> text
                .replaceAll("\"|\\*\\*|\\*|_", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Normalize unicode
        return Normalizer.normalize(cleaned, Normalizer.Form.NFKC);
    }

    private String extractSnippet(String text, String query) {
        if (text == null || text.isBlank()) return "";
        String lower = text.toLowerCase();
        String q = query == null ? "" : query.toLowerCase();
        int idx = lower.indexOf(q);
        if (idx < 0) {
            // fallback: return beginning
            return shorten(text, 0, Math.min(140, text.length()));
        }

        int start = Math.max(0, idx - 50);
        int end = Math.min(text.length(), idx + Math.max(100, q.length() + 50));
        return shorten(text, start, end);
    }

    private String shorten(String text, int start, int end) {
        if (text == null) return "";
        String prefix = start > 0 ? "..." : "";
        String suffix = end < text.length() ? "..." : "";
        return prefix + text.substring(start, end).trim() + suffix;
    }

    private String firstNonNull(String... candidates) {
        if (candidates == null) return null;
        for (String c : candidates) {
            if (c != null && !c.isBlank()) return c;
        }
        return null;
    }
}
