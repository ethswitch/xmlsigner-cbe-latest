package org.ips.xml.signer.xmlsigner.service.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.ips.xml.signer.xmlsigner.utils.DateUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;



@Service
public class RedisTokenCacheServiceImpl implements TokenCacheService {

    private final RedisTemplate<String, TokenInfo> redisTemplate;


    public RedisTokenCacheServiceImpl(RedisTemplate<String, TokenInfo> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public TokenInfo getToken(String clientId) {
        Object data = redisTemplate.opsForValue().get(clientId);

        TokenInfo token = new ObjectMapper().convertValue(data, TokenInfo.class);
        if (token != null) {
            Long remainingDuration = redisTemplate.getExpire(clientId, java.util.concurrent.TimeUnit.SECONDS);
            Long tokenDuration = token.getExpires_in() - remainingDuration;
            token.setExpires_in(remainingDuration);
            token.setRefresh_expires_in(token.getRefresh_expires_in() - tokenDuration);
        }
        return token;
    }

    @Override
    public void saveToken(String clientId, TokenInfo token) {
        String savedTime = DateUtil.iso86CurrentTime();
        token.setCreatedTime(savedTime);
        Duration expiryDuration = Duration.ofSeconds(token.getExpires_in());
        redisTemplate.opsForValue().set(clientId, token, expiryDuration);
    }

    @Override
    public void removeToken(String clientId) {
        redisTemplate.delete(clientId);
    }
}
