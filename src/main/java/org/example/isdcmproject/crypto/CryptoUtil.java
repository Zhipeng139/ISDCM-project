package org.example.isdcmproject.crypto;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

public final class CryptoUtil {

    private static final String MAGIC = "ISDCM1";
    private static final byte[] MAGIC_BYTES = MAGIC.getBytes();
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int KEY_LEN_BITS = 256;
    private static final int PBKDF2_ITERATIONS = 200_000;
    private static final int GCM_TAG_BITS = 128;
    private static final int BUFFER = 64 * 1024;

    private CryptoUtil() {}

    public static void encrypt(InputStream in, OutputStream out, char[] passphrase) throws IOException {
        SecureRandom rng = new SecureRandom();
        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        rng.nextBytes(salt);
        rng.nextBytes(iv);

        SecretKey key = deriveKey(passphrase, salt);
        Cipher cipher = newCipher(Cipher.ENCRYPT_MODE, key, iv);

        out.write(MAGIC_BYTES);
        out.write(salt);
        out.write(iv);

        try (CipherOutputStream cos = new CipherOutputStream(out, cipher)) {
            copy(in, cos);
        }
    }

    public static void decrypt(InputStream in, OutputStream out, char[] passphrase) throws IOException {
        byte[] magic = in.readNBytes(MAGIC_BYTES.length);
        if (magic.length != MAGIC_BYTES.length)
            throw new IOException("Archivo inválido: cabecera incompleta.");
        for (int i = 0; i < MAGIC_BYTES.length; i++) {
            if (magic[i] != MAGIC_BYTES[i])
                throw new IOException("Archivo no reconocido (cabecera ISDCM1 ausente).");
        }
        byte[] salt = in.readNBytes(SALT_LEN);
        byte[] iv = in.readNBytes(IV_LEN);
        if (salt.length != SALT_LEN || iv.length != IV_LEN)
            throw new IOException("Archivo cifrado corrupto.");

        SecretKey key = deriveKey(passphrase, salt);
        Cipher cipher = newCipher(Cipher.DECRYPT_MODE, key, iv);

        try (CipherInputStream cis = new CipherInputStream(in, cipher)) {
            copy(cis, out);
        }
    }

    private static SecretKey deriveKey(char[] passphrase, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_LEN_BITS);
            byte[] raw = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return new SecretKeySpec(raw, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo derivar la clave: " + e.getMessage(), e);
        }
    }

    private static Cipher newCipher(int mode, SecretKey key, byte[] iv) {
        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(mode, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return c;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar AES/GCM: " + e.getMessage(), e);
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUFFER];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
    }
}
