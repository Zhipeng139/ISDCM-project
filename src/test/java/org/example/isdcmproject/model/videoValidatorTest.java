package org.example.isdcmproject.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class videoValidatorTest {
    private final videoValidator validator = new videoValidator();

    @Test
    void shouldRejectEmptyFields() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("titulo", "");
        form.put("fechaCreacion", "");
        form.put("duracion", "");
        form.put("reproducciones", "");
        form.put("descripcion", "");
        form.put("formato", "");
        form.put("url", "");
        form.put("categoria", "");
        form.put("resolucion", "");

        Map<String, String> errors = validator.validate(form);

        assertEquals(9, errors.size());
        assertTrue(errors.containsKey("titulo"));
        assertTrue(errors.containsKey("fechaCreacion"));
        assertTrue(errors.containsKey("duracion"));
        assertTrue(errors.containsKey("reproducciones"));
        assertTrue(errors.containsKey("descripcion"));
        assertTrue(errors.containsKey("formato"));
        assertTrue(errors.containsKey("url"));
        assertTrue(errors.containsKey("categoria"));
        assertTrue(errors.containsKey("resolucion"));
    }

    @Test
    void shouldRejectInvalidFormats() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("titulo", "Video");
        form.put("fechaCreacion", "2026-20-55");
        form.put("duracion", "-10");
        form.put("reproducciones", "abc");
        form.put("descripcion", "Descripción");
        form.put("formato", "mp4");
        form.put("url", "no-url");
        form.put("categoria", "music");
        form.put("resolucion", "1080p");

        Map<String, String> errors = validator.validate(form);

        assertTrue(errors.containsKey("fechaCreacion"));
        assertTrue(errors.containsKey("duracion"));
        assertTrue(errors.containsKey("reproducciones"));
        assertTrue(errors.containsKey("url"));
    }

    @Test
    void shouldValidateCorrectForm() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("titulo", "Video");
        form.put("fechaCreacion", "2026-03-20");
        form.put("duracion", "120");
        form.put("reproducciones", "10");
        form.put("descripcion", "Descripción");
        form.put("formato", "mp4");
        form.put("url", "https://example.com/video");
        form.put("categoria", "music");
        form.put("resolucion", "1080p");

        Map<String, String> errors = validator.validate(form);

        assertTrue(errors.isEmpty());
        video video = validator.toVideo(form);
        assertEquals("", video.getIdentificador());
        assertEquals("Video", video.getTitulo());
        assertEquals(120, video.getDuracion());
        assertEquals(10, video.getReproducciones());
        assertEquals("1080p", video.getResolucion());
    }
}
