package com.youtubeagent.youtube.token;

import com.youtubeagent.youtube.YouTubeProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class YouTubeTokenCrypto {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKeySpec encryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public YouTubeTokenCrypto(YouTubeProperties properties) {
        byte[] keyBytes = Base64.getDecoder().decode(properties.tokenStorage().encryptionKey());

        if (keyBytes.length != 32) {
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

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] storedData = new byte[iv.length + ciphertext.length];

            System.arraycopy(iv, 0, storedData, 0, iv.length);
            System.arraycopy(ciphertext, 0, storedData, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(storedData);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt YouTube token", e);
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null) {
            return null;
        }

        try {
            byte[] storedData = Base64.getDecoder().decode(encryptedValue);

            if (storedData.length <= IV_LENGTH) {
                throw new IllegalStateException("Invalid encrypted YouTube token");
            }

            byte[] iv = java.util.Arrays.copyOfRange(storedData, 0, IV_LENGTH);
            byte[] ciphertext = java.util.Arrays.copyOfRange(storedData, IV_LENGTH, storedData.length);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt YouTube token", e);
        }
    }
}