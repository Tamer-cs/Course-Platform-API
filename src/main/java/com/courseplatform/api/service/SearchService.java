package com.courseplatform.api.service;

import com.courseplatform.api.dto.SearchQueryResultDTO;
import com.courseplatform.api.model.Subtopic;
import com.courseplatform.api.repository.CourseRepository;
import com.courseplatform.api.repository.SubtopicRepository;
import com.courseplatform.api.util.VectorMathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final double DEFAULT_KEYWORD_WEIGHT = 0.5d;
    private static final double DEFAULT_SEMANTIC_WEIGHT = 0.5d;

    private final CourseRepository courseRepository;
    private final SubtopicRepository subtopicRepository;
    private final EmbeddingService embeddingService;
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

    @Transactional(readOnly = true)
    public List<SearchQueryResultDTO> hybridSearch(String q, int limit) {
        return hybridSearch(q, limit, DEFAULT_KEYWORD_WEIGHT, DEFAULT_SEMANTIC_WEIGHT);
    }

    @Transactional(readOnly = true)
    public List<SearchQueryResultDTO> hybridSearch(String q, int limit, double keywordWeight, double semanticWeight) {
        if (q == null || q.isBlank() || limit <= 0) {
            return List.of();
        }

        double safeKeywordWeight = Math.max(0.0d, keywordWeight);
        double safeSemanticWeight = Math.max(0.0d, semanticWeight);

        List<SearchQueryResultDTO> keywordResults = normalizeScores(search(q, limit));

        float[] queryVector;
        try {
            queryVector = embeddingService.generateEmbedding(q);
        } catch (Exception exception) {
            logger.warn("Hybrid semantic embedding generation failed for query '{}': {}", q, exception.getMessage());
            logger.debug("Hybrid semantic embedding generation failure", exception);
            queryVector = new float[0];
        }

        List<SearchQueryResultDTO> semanticResults = normalizeScores(performSemanticSearch(queryVector, limit));

        Map<String, HybridCandidate> merged = new HashMap<>();
        for (SearchQueryResultDTO result : keywordResults) {
            if (result.getSubtopicId() == null) {
                continue;
            }
            HybridCandidate candidate = merged.computeIfAbsent(result.getSubtopicId(), key -> new HybridCandidate());
            candidate.dto = pickPreferred(candidate.dto, result);
            candidate.keywordScore = result.getRelevanceScore();
        }

        for (SearchQueryResultDTO result : semanticResults) {
            if (result.getSubtopicId() == null) {
                continue;
            }
            HybridCandidate candidate = merged.computeIfAbsent(result.getSubtopicId(), key -> new HybridCandidate());
            candidate.dto = pickPreferred(candidate.dto, result);
            candidate.semanticScore = result.getRelevanceScore();
        }

        List<SearchQueryResultDTO> results = new ArrayList<>();
        for (HybridCandidate candidate : merged.values()) {
            double keywordScore = candidate.keywordScore == null ? 0.0d : candidate.keywordScore;
            double semanticScore = candidate.semanticScore == null ? 0.0d : candidate.semanticScore;
            double hybridScore = (keywordScore * safeKeywordWeight) + (semanticScore * safeSemanticWeight);

            SearchQueryResultDTO dto = candidate.dto == null ? null : copyWithScore(candidate.dto, hybridScore);
            if (dto == null) {
                continue;
            }
            results.add(dto);
        }

        results.sort(Comparator.comparingDouble(SearchQueryResultDTO::getRelevanceScore).reversed());
        if (results.size() > limit) {
            return new ArrayList<>(results.subList(0, limit));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<SearchQueryResultDTO> performSemanticSearch(float[] queryVector, int limit) {
        if (queryVector == null || queryVector.length == 0 || limit <= 0) {
            return List.of();
        }

        List<SearchQueryResultDTO> results = new ArrayList<>();
        List<Subtopic> subtopics = subtopicRepository.findAll();

        for (Subtopic subtopic : subtopics) {
            float[] storedVector = VectorMathUtils.fromByteArray(subtopic.getEmbeddingVector());
            if (storedVector.length == 0) {
                continue;
            }

            double score = VectorMathUtils.cosineSimilarity(queryVector, storedVector);
            if (score <= 0.0d) {
                continue;
            }

            String content = subtopic.getContent();
            String plainContent = stripMarkdown(content == null ? "" : content);
            String snippet = extractSnippet(plainContent, subtopic.getTitle());

            SearchQueryResultDTO dto = SearchQueryResultDTO.builder()
                    .courseId(subtopic.getTopic().getCourse().getId())
                    .courseTitle(subtopic.getTopic().getCourse().getTitle())
                    .topicTitle(subtopic.getTopic().getTitle())
                    .subtopicId(subtopic.getId())
                    .subtopicTitle(subtopic.getTitle())
                    .relevanceScore(score)
                    .excerptSnippet(snippet)
                    .isFuzzyMatch(false)
                    .build();

            results.add(dto);
        }

        results.sort(Comparator.comparingDouble(SearchQueryResultDTO::getRelevanceScore).reversed());
        if (results.size() > limit) {
            return new ArrayList<>(results.subList(0, limit));
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

    private List<SearchQueryResultDTO> normalizeScores(List<SearchQueryResultDTO> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        if (results.size() == 1) {
            List<SearchQueryResultDTO> single = new ArrayList<>(1);
            single.add(copyWithScore(results.get(0), 1.0d));
            return single;
        }

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (SearchQueryResultDTO result : results) {
            double score = result.getRelevanceScore() == null ? 0.0d : result.getRelevanceScore();
            min = Math.min(min, score);
            max = Math.max(max, score);
        }

        if (Double.compare(min, max) == 0) {
            List<SearchQueryResultDTO> equal = new ArrayList<>(results.size());
            for (SearchQueryResultDTO result : results) {
                equal.add(copyWithScore(result, 1.0d));
            }
            return equal;
        }

        double range = max - min;
        List<SearchQueryResultDTO> normalized = new ArrayList<>(results.size());
        for (SearchQueryResultDTO result : results) {
            double score = result.getRelevanceScore() == null ? 0.0d : result.getRelevanceScore();
            double normalizedScore = (score - min) / range;
            normalized.add(copyWithScore(result, normalizedScore));
        }
        return normalized;
    }

    private SearchQueryResultDTO copyWithScore(SearchQueryResultDTO source, double score) {
        if (source == null) {
            return null;
        }
        return SearchQueryResultDTO.builder()
                .courseId(source.getCourseId())
                .courseTitle(source.getCourseTitle())
                .topicTitle(source.getTopicTitle())
                .subtopicId(source.getSubtopicId())
                .subtopicTitle(source.getSubtopicTitle())
                .relevanceScore(score)
                .excerptSnippet(source.getExcerptSnippet())
                .isFuzzyMatch(source.getIsFuzzyMatch())
                .build();
    }

    private SearchQueryResultDTO pickPreferred(SearchQueryResultDTO current, SearchQueryResultDTO incoming) {
        if (current == null) {
            return incoming;
        }
        if (current.getExcerptSnippet() == null || current.getExcerptSnippet().isBlank()) {
            return incoming;
        }
        if (incoming != null && incoming.getExcerptSnippet() != null && !incoming.getExcerptSnippet().isBlank()) {
            return incoming.getRelevanceScore() != null && current.getRelevanceScore() != null
                    && incoming.getRelevanceScore() > current.getRelevanceScore()
                    ? incoming
                    : current;
        }
        return current;
    }

    private static final class HybridCandidate {
        private SearchQueryResultDTO dto;
        private Double keywordScore;
        private Double semanticScore;
    }
}
