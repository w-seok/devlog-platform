package com.devlog.platform.common.service;

import static com.devlog.platform.common.util.InMemoryKeyUtils.*;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.devlog.platform.common.data.TokenRec;
import com.devlog.platform.common.property.TokenProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTaskService {

	@Value("${spring.profiles.active:}")
	private String stage;

	private final TokenProperties tokenProperties;
	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 사용자의 refresh token을 저장
	 * @param tokenRec refreshToken
	 * @param userId 사용자 id
	 */
	public void setRefreshToken(TokenRec tokenRec, String userId) {
		String key = generateRefreshTokenKey(stage, userId);
		redisTemplate.opsForValue()
			.set(key, tokenRec.refreshToken(), tokenProperties.getRefreshTime(), TimeUnit.MILLISECONDS);
		log.info("refresh token 저장 성공 - userId: {}", userId);
	}
}
