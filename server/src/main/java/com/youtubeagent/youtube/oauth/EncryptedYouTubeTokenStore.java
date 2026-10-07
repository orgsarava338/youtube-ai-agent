package com.youtubeagent.youtube.oauth;

import com.youtubeagent.youtube.YouTubeProperties;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class EncryptedYouTubeTokenStore {

    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final ObjectMapper objectMapper;
    private final Path tokenFile;
    private final SecretKeySpec encryptionKey;

    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptedYouTubeTokenStore(ObjectMapper objectMapper, YouTubeProperties properties) {

        this.objectMapper = objectMapper;
        this.tokenFile = Path.of(properties.tokenStorage().filePath());

        byte[] keyBytes = Base64.getDecoder().decode(properties.tokenStorage().encryptionKey());

        if (keyBytes.length != 32) {
            throw new IllegalStateException("YOUTUBE_TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes");
        }

        this.encryptionKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    public void save(YouTubeToken token) {

        try {
            byte[] plaintext = objectMapper.writeValueAsBytes(token);

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] storedData = new byte[iv.length + ciphertext.length];

            System.arraycopy(iv, 0, storedData, 0, iv.length);
            System.arraycopy(ciphertext, 0, storedData, iv.length, ciphertext.length);

            Path parent = tokenFile.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.write(tokenFile, storedData);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to save YouTube OAuth token", e);
        }
    }

    public Optional<YouTubeToken> load() {

        if (!Files.exists(tokenFile)) {
            return Optional.empty();
        }

        try {
            byte[] storedData = Files.readAllBytes(tokenFile);

            if (storedData.length <= IV_LENGTH) {
                throw new IllegalStateException("Invalid YouTube token file");
            }

            byte[] iv = new byte[IV_LENGTH];

            System.arraycopy(storedData, 0, iv, 0, IV_LENGTH);

            byte[] ciphertext = new byte[storedData.length - IV_LENGTH];

            System.arraycopy(storedData, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] plaintext = cipher.doFinal(ciphertext);

            return Optional.of(objectMapper.readValue(plaintext, YouTubeToken.class));

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load YouTube OAuth token", e);
        }
    }

    public void delete() {
        try {
            Files.deleteIfExists(tokenFile);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete YouTube OAuth token", e);
        }
    }
}