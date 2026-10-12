
package com.youtubeagent.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthTokenService {
    private static final long LOGIN_CODE_SECONDS = 60;
    private static final long ACCESS_TOKEN_SECONDS = 900;
    private static final long REFRESH_TOKEN_SECONDS = 30L * 24 * 60 * 60;

    private final JdbcTemplate jdbc;
    private final SecureRandom random = new SecureRandom();

    public AuthTokenService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record IssuedTokens(
            String accessToken,
            String refreshToken,
            long expiresIn) {
    }

    public String createLoginCode(String userId) {
        String code = generateToken();

        jdbc.update("""
                INSERT INTO auth.assistube_login_codes
                    (code_hash, user_id, expires_at)
                VALUES (?, ?, CURRENT_TIMESTAMP + (? * INTERVAL '1 second'))
                """,

                hash(code), userId, LOGIN_CODE_SECONDS);

        return code;
    }

    @Transactional
    public IssuedTokens exchangeLoginCode(String code) {
        if (code == null || code.isBlank() || code.length() > 200) {
            throw new InvalidTokenException();
        }

        List<String> users = jdbc.query("""
                UPDATE auth.assistube_login_codes
                   SET consumed_at = CURRENT_TIMESTAMP
                 WHERE code_hash = ?
                   AND consumed_at IS NULL
                   AND expires_at > CURRENT_TIMESTAMP
                RETURNING user_id
                """,
                (rs, row) -> rs.getString("user_id"),
                hash(code));

        if (users.size() != 1) {
            throw new InvalidTokenException();
        }

        return issueTokens(users.getFirst());
    }

    @Transactional
    public IssuedTokens refreshTokens(String refreshToken) {
        if (refreshToken == null
                || refreshToken.isBlank()
                || refreshToken.length() > 200) {
            throw new InvalidTokenException();
        }

        // Atomically consume the existing refresh token.
        // Concurrent refresh attempts cannot both use the same token.
        List<String> users = jdbc.query("""
                UPDATE auth.assistube_refresh_tokens
                   SET revoked_at = CURRENT_TIMESTAMP
                 WHERE token_hash = ?
                   AND revoked_at IS NULL
                   AND expires_at > CURRENT_TIMESTAMP
                RETURNING user_id
                """,
                (rs, row) -> rs.getString("user_id"),
                hash(refreshToken));

        if (users.size() != 1) {
            throw new InvalidTokenException();
        }

        // Revoke the previous access tokens for this user only if desired
        // by your session policy. This implementation leaves other sessions
        // active and issues a new access token for this session.
        return issueTokens(users.getFirst());
    }

    private IssuedTokens issueTokens(String userId) {
        String accessToken = generateToken();
        String refreshToken = generateToken();

        jdbc.update("""
                INSERT INTO auth.assistube_access_tokens
                    (token_hash, user_id, expires_at)
                VALUES (?, ?, CURRENT_TIMESTAMP + (? * INTERVAL '1 second'))
                """,
                hash(accessToken), userId, ACCESS_TOKEN_SECONDS);

        jdbc.update("""
                INSERT INTO auth.assistube_refresh_tokens
                    (token_hash, user_id, expires_at)
                VALUES (?, ?, CURRENT_TIMESTAMP + (? * INTERVAL '1 second'))
                """,
                hash(refreshToken), userId, REFRESH_TOKEN_SECONDS);

        return new IssuedTokens(
                accessToken,
                refreshToken,
                ACCESS_TOKEN_SECONDS);
    }

    public Optional<Authentication> authenticate(String token) {
        if (token == null || token.isBlank() || token.length() > 200) {
            return Optional.empty();
        }

        List<String> users = jdbc.query("""
                SELECT user_id
                  FROM auth.assistube_access_tokens
                 WHERE token_hash = ?
                   AND revoked_at IS NULL
                   AND expires_at > CURRENT_TIMESTAMP
                """,
                (rs, row) -> rs.getString("user_id"),
                hash(token));

        if (users.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(
                new UsernamePasswordAuthenticationToken(
                        users.getFirst(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    public void revokeAccessToken(String token) {
        if (token == null || token.isBlank() || token.length() > 200) {
            return;
        }

        jdbc.update("""
                UPDATE auth.assistube_access_tokens
                   SET revoked_at = CURRENT_TIMESTAMP
                 WHERE token_hash = ?
                   AND revoked_at IS NULL
                """, hash(token));
    }

    public void revokeRefreshToken(String token) {
        if (token == null || token.isBlank() || token.length() > 200) {
            return;
        }

        jdbc.update("""
                UPDATE auth.assistube_refresh_tokens
                   SET revoked_at = CURRENT_TIMESTAMP
                 WHERE token_hash = ?
                   AND revoked_at IS NULL
                """, hash(token));
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        revokeRefreshToken(refreshToken);
        revokeAccessToken(accessToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException() {
            super("Invalid or expired token");
        }
    }
}
