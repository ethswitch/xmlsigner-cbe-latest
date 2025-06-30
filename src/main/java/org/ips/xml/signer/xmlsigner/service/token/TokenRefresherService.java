package org.ips.xml.signer.xmlsigner.service.token;


import lombok.extern.slf4j.Slf4j;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.ips.xml.signer.xmlsigner.service.TokenGenerationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@Slf4j
public class TokenRefresherService {



    private final RedisTemplate<String, TokenInfo> redisTemplate;
    private final TokenGenerationManager authServerClient;

    // Tolerance in seconds (e.g., refresh if token expires in < 2 mins)
    private static final long ACCESS_TOKEN_TOLERANCE = 120;
    private static final long REFRESH_TOKEN_TOLERANCE = 120;

    @Value("${token.refresh.fixedDelay}")
    private long fixedDelay;

    public TokenRefresherService(RedisTemplate<String, TokenInfo> redisTemplate,
                                 TokenGenerationManager authServerClient) {
        this.redisTemplate = redisTemplate;
        this.authServerClient = authServerClient;
    }

    @Scheduled(fixedDelayString = "${token.refresh.fixedDelay}")
    public void refreshExpiringTokens() {
        log.info("Running scheduled token refresher task...");

        Set<String> keys = redisTemplate.keys("token:*");
        if (keys.isEmpty() ) {
            return;
        }

        for (String key : keys) {
            TokenInfo token = redisTemplate.opsForValue().get(key);

            if (token == null) continue;

            long remainingAccessTokenTime = redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.SECONDS);

            if (remainingAccessTokenTime >ACCESS_TOKEN_TOLERANCE) continue;

            if (remainingAccessTokenTime <= ACCESS_TOKEN_TOLERANCE) {
                try {
                    TokenInfo refreshedToken;

                    long remainingRefreshTokenTime = token.getRefresh_expires_in();

                    if (remainingRefreshTokenTime > REFRESH_TOKEN_TOLERANCE) {
                        // Try to refresh using refresh token
                        refreshedToken = authServerClient.refreshToken(token.getRefresh_token(),key);
                        log.info("Token refreshed for key: {}", key);
                    } else {
                        // Refresh via full credential flow
                        refreshedToken = authServerClient.getNewToken(key);
                        log.info("Token re-issued via credentials for key: {}", key);
                    }

                    redisTemplate.opsForValue().set(key, refreshedToken,
                            Duration.ofSeconds(refreshedToken.getExpires_in()));
                } catch (Exception e) {
                    log.error("Failed to refresh token for key: {}", key, e);
                }
            }
        }
    }
}