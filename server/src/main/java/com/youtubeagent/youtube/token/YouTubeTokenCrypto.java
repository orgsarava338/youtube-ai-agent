package com.youtubeagent.youtube.token;

import com.youtubeagent.youtube.YouTubeProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class YouTubeTokenCrypto {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 32;

    private final SecretKeySpec encryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public YouTubeTokenCrypto(YouTubeProperties properties) {
        if (properties.tokenEncryption() == null
                || properties.tokenEncryption().encryptionKey() == null
                || properties.tokenEncryption().encryptionKey().isBlank()) {
            throw new IllegalStateException("YouTube token encryption key is not configured");
        }

        final byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(properties.tokenEncryption().encryptionKey());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("YouTube token encryption key must be valid Base64", e);
        }

        if (keyBytes.length != KEY_LENGTH) {
            throw new IllegalStateException("YOUTUBE_TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes");
        }

        this.encryptionKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] storedData = new byte[IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, storedData, 0, IV_LENGTH);
            System.arraycopy(ciphertext, 0, storedData, IV_LENGTH, ciphertext.length);

            return Base64.getEncoder().encodeToString(storedData);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt YouTube token", e);
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null) {
            return null;
        }

        try {
            byte[] storedData = Base64.getDecoder().decode(encryptedValue);

            // A valid value must contain an IV and at least a GCM tag.
            if (storedData.length < IV_LENGTH + GCM_TAG_LENGTH / 8) {
                throw new IllegalArgumentException("Invalid encrypted YouTube token");
            }

            byte[] iv = Arrays.copyOfRange(storedData, 0, IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(storedData, IV_LENGTH, storedData.length);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Failed to decrypt YouTube token. Check the encryption key and stored data.", e);
        }
    }
}