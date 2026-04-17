package org.example.isdcmproject.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.example.isdcmproject.model.video;
import org.example.isdcmproject.model.videoIdGenerator;
import org.example.isdcmproject.model.videoRepository;
import org.example.isdcmproject.model.videoSearchCriteria;

public class VideoService {
    private static final Logger LOGGER = Logger.getLogger(VideoService.class.getName());
    private final videoRepository repository;
    private final videoIdGenerator idGenerator;

    public VideoService() {
        this(new videoRepository(), new videoIdGenerator());
    }

    public VideoService(videoRepository repository, videoIdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public video registerVideo(video video) throws SQLException {
        int attempts = 0;
        while (attempts < 5) {
            attempts++;
            String generated = idGenerator.generate(video);
            if (repository.existsById(generated)) {
                continue;
            }
            video.setIdentificador(generated);
            videoRepository.SaveVideoResult result = repository.save(video);
            if (result == videoRepository.SaveVideoResult.SUCCESS) {
                return video;
            }
            if (result != videoRepository.SaveVideoResult.DUPLICATE_ID) {
                break;
            }
        }
        LOGGER.warning("No se pudo generar un identificador único para el video.");
        throw new SQLException("No se pudo generar un identificador único para el video.");
    }

    public List<video> search(videoSearchCriteria criteria) throws SQLException {
        List<video> candidates = repository.findByFilters(criteria);
        if (criteria.getConsultaLibre().isBlank()) {
            return candidates;
        }
        List<ScoredVideo> scored = new ArrayList<>();
        for (video item : candidates) {
            int score = score(item, criteria.getConsultaLibre());
            if (score > 0) {
                scored.add(new ScoredVideo(item, score));
            }
        }
        scored.sort(Comparator.comparingInt(ScoredVideo::score).reversed()
                .thenComparing(scoredVideo -> scoredVideo.video().getFechaCreacion(), Comparator.reverseOrder()));
        List<video> result = new ArrayList<>();
        for (ScoredVideo scoredVideo : scored) {
            result.add(scoredVideo.video());
        }
        return result;
    }

    public List<String> suggest(String term) {
        if (term == null || term.trim().isEmpty()) {
            return List.of();
        }
        try {
            return repository.suggestTitles(term.trim(), 8);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible generar sugerencias de títulos.", e);
            return List.of();
        }
    }

    private int score(video item, String query) {
        String normalizedQuery = query.toLowerCase(Locale.ROOT).trim();
        String title = safe(item.getTitulo()).toLowerCase(Locale.ROOT);
        String description = safe(item.getDescripcion()).toLowerCase(Locale.ROOT);
        String category = safe(item.getCategoria()).toLowerCase(Locale.ROOT);
        String composite = title + " " + description + " " + category;

        int score = 0;
        if (title.equals(normalizedQuery)) {
            score += 140;
        } else if (title.startsWith(normalizedQuery)) {
            score += 100;
        } else if (title.contains(normalizedQuery)) {
            score += 70;
        }
        if (composite.contains(normalizedQuery)) {
            score += 40;
        }

        String[] terms = normalizedQuery.split("\\s+");
        for (String term : terms) {
            if (term.isBlank()) {
                continue;
            }
            if (title.contains(term)) {
                score += 20;
            }
            if (description.contains(term) || category.contains(term)) {
                score += 12;
            }
            if (levenshteinDistance(term, title) <= 2) {
                score += 15;
            } else if (levenshteinDistance(term, category) <= 2) {
                score += 10;
            }
        }
        return score;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int levenshteinDistance(String left, String right) {
        if (left.isBlank() || right.isBlank()) {
            return Integer.MAX_VALUE;
        }
        int n = left.length();
        int m = right.length();
        int[] previous = new int[m + 1];
        int[] current = new int[m + 1];
        for (int j = 0; j <= m; j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= n; i++) {
            current[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                );
            }
            int[] tmp = previous;
            previous = current;
            current = tmp;
        }
        return previous[m];
    }

    private record ScoredVideo(video video, int score) {
    }
}
