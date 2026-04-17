package org.example.isdcmproject.model;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public class videoValidator {
    public Map<String, String> validate(Map<String, String> formData) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        String identificador = sanitize(formData.get("identificador"));
        String titulo = sanitize(formData.get("titulo"));
        String fechaCreacion = sanitize(formData.get("fechaCreacion"));
        String duracion = sanitize(formData.get("duracion"));
        String reproducciones = sanitize(formData.get("reproducciones"));
        String descripcion = sanitize(formData.get("descripcion"));
        String formato = sanitize(formData.get("formato"));
        String url = sanitize(formData.get("url"));
        String categoria = sanitize(formData.get("categoria"));

        if (isBlank(identificador)) {
            fieldErrors.put("identificador", "El identificador es obligatorio.");
        }
        if (isBlank(titulo)) {
            fieldErrors.put("titulo", "El título es obligatorio.");
        }
        if (isBlank(fechaCreacion)) {
            fieldErrors.put("fechaCreacion", "La fecha de creación es obligatoria.");
        } else {
            try {
                LocalDate.parse(fechaCreacion);
            } catch (DateTimeParseException e) {
                fieldErrors.put("fechaCreacion", "La fecha de creación no es válida.");
            }
        }
        validateNonNegativeNumber("duracion", "La duración", duracion, fieldErrors);
        validateNonNegativeNumber("reproducciones", "Las reproducciones", reproducciones, fieldErrors);
        if (isBlank(descripcion)) {
            fieldErrors.put("descripcion", "La descripción es obligatoria.");
        }
        if (isBlank(formato)) {
            fieldErrors.put("formato", "El formato es obligatorio.");
        }
        if (isBlank(url)) {
            fieldErrors.put("url", "La URL es obligatoria.");
        } else if (!isValidUrl(url)) {
            fieldErrors.put("url", "La URL del video no es válida.");
        }
        if (isBlank(categoria)) {
            fieldErrors.put("categoria", "La categoría es obligatoria.");
        }
        return fieldErrors;
    }

    public video toVideo(Map<String, String> formData) {
        return new video(
                sanitize(formData.get("identificador")),
                sanitize(formData.get("titulo")),
                LocalDate.parse(sanitize(formData.get("fechaCreacion"))),
                Integer.parseInt(sanitize(formData.get("duracion"))),
                Integer.parseInt(sanitize(formData.get("reproducciones"))),
                sanitize(formData.get("descripcion")),
                sanitize(formData.get("formato")),
                sanitize(formData.get("url")),
                sanitize(formData.get("categoria"))
        );
    }

    private void validateNonNegativeNumber(String fieldName, String fieldLabel, String value, Map<String, String> fieldErrors) {
        if (isBlank(value)) {
            fieldErrors.put(fieldName, fieldLabel + " es obligatoria.");
            return;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                fieldErrors.put(fieldName, fieldLabel + " no puede ser negativa.");
            }
        } catch (NumberFormatException e) {
            fieldErrors.put(fieldName, fieldLabel + " debe ser un número entero.");
        }
    }

    private boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
