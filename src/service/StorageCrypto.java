package service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Encrypts / decrypts one text blob for disk storage.
 * <p><b>How it works:</b> AES-256-GCM (same key for encrypt+decrypt). Each line on disk is
 * {@code ENC1:} + Base64( random IV + ciphertext ). Key = SHA-256( {@code SPORTCONNECT_STORAGE_KEY}
 * env or system property ); if missing, a dev default is used (see stderr warning).
 */
public final class StorageCrypto {

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final String KEY_NAME = "SPORTCONNECT_STORAGE_KEY";
    private static boolean warnedDevKey;

    private StorageCrypto() {}

    /** Turns readable text into one line starting with ENC1: (safe to put in a .txt file). */
    public static String encryptUtf8(String text) {
        if (text == null) text = "";
        try {
            byte[] key = secretKeyBytes();
            byte[] iv = new byte[IV_LEN];
            new SecureRandom().nextBytes(iv);

            Cipher c = Cipher.getInstance(CIPHER);
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            byte[] cipherBytes = c.doFinal(text.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buf.put(iv).put(cipherBytes);
            return "ENC1:" + Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new IllegalStateException("encrypt failed: " + e.getMessage(), e);
        }
    }

    /** Reverse of {@link #encryptUtf8}; returns null if wrong key, corrupt data, or not ENC1:. */
    public static String decryptUtf8(String line) {
        if (line == null || !line.startsWith("ENC1:")) return null;
        try {
            byte[] all = Base64.getDecoder().decode(line.substring(5).trim());
            if (all.length < IV_LEN + 16) return null;
            byte[] iv = Arrays.copyOfRange(all, 0, IV_LEN);
            byte[] ct = Arrays.copyOfRange(all, IV_LEN, all.length);

            Cipher c = Cipher.getInstance(CIPHER);
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKeyBytes(), "AES"), new GCMParameterSpec(128, iv));
            return new String(c.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] secretKeyBytes() throws Exception {
        String s = System.getenv(KEY_NAME);
        if (s == null || s.isBlank()) s = System.getProperty(KEY_NAME);
        if (s == null || s.isBlank()) {
            if (!warnedDevKey) {
                System.err.println("[StorageCrypto] Set " + KEY_NAME + " for production. Using dev key.");
                warnedDevKey = true;
            }
            // Must stay stable so old data/player_signups.txt ENC1: lines still decrypt.
            s = "SportConnect-DEV-only-change-via-" + KEY_NAME;
        } else {
            s = s.trim();
        }
        return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    }
}
