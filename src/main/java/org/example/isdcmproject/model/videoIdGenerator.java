package org.example.isdcmproject.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class videoIdGenerator {
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String generate(video video) {
        String timestamp = Long.toString(Instant.now().toEpochMilli(), 36).toUpperCase(Locale.ROOT);
        String random = randomPart(6);
        String metadataHash = hashPart(video.getTitulo() + "|" + video.getCategoria() + "|" + video.getFormato());
        return "VID-" + timestamp + "-" + random + "-" + metadataHash;
    }

    private String randomPart(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int position = ThreadLocalRandom.current().nextInt(ALPHANUM.length());
            builder.append(ALPHANUM.charAt(position));
        }
        return builder.toString();
    }

    private String hashPart(String metadata) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(metadata.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : hash) {
                hex.append(String.format("%02X", value));
            }
            return hex.substring(0, 6);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar hash para el identificador de video.", e);
        }
    }
}
