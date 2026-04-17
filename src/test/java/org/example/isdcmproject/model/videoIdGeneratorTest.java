package org.example.isdcmproject.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class videoIdGeneratorTest {
    @Test
    void shouldGenerateDistinctIdsWithMetadataAndRandomness() {
        videoIdGenerator generator = new videoIdGenerator();
        video baseVideo = new video(
                "",
                "Concierto de prueba",
                LocalDate.of(2026, 3, 21),
                180,
                0,
                "Prueba de generación de id",
                "mp4",
                "https://example.com/v/test",
                "musica",
                "1080p"
        );

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            ids.add(generator.generate(baseVideo));
        }

        assertEquals(30, ids.size());
        for (String id : ids) {
            assertTrue(id.startsWith("VID-"));
            assertTrue(id.split("-").length >= 4);
        }
    }
}
