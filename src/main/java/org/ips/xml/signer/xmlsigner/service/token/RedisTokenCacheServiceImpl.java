package org.ips.xml.signer.xmlsigner.service.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


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
        Long remainingDuration=redisTemplate.getExpire(clientId,java.util.concurrent.TimeUnit.SECONDS);
        Long tokenDuration= token.getExpires_in()-remainingDuration;
        token.setExpires_in( remainingDuration);
        token.setRefresh_expires_in(token.getRefresh_expires_in()-tokenDuration);
        return token;
    }

    @Override
    public void saveToken(String clientId, TokenInfo token) {
        redisTemplate.opsForValue().set(clientId, token,token.getExpires_in());
    }

    @Override
    public void removeToken(String clientId) {
        redisTemplate.delete(clientId);
    }
}
