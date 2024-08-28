package com.devlog.platform.common.interceptor;

import static com.devlog.platform.common.util.RequestUtils.*;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.devlog.platform.common.annotation.CheckAbusing;
import com.devlog.platform.common.exception.AbusingException;
import com.devlog.platform.common.util.InMemoryKeyUtils;
import com.devlog.platform.common.util.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 호출 횟수 제한(초당 3회)에 걸릴 경우 에러 CheckAbusing 어노테이션이 붙은 메소드에 대해 초당 요청 제한을 적용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbusingInterceptor implements HandlerInterceptor {

	@Value("${spring.profiles.active:#{null}}")
	private String stage;

	private final RedisTemplate<String, Integer> redisTemplate;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		AbusingException {
		if (!(handler instanceof HandlerMethod)) {
			log.debug("check handler is not instance of HandlerMethod");
			return true;
		}

		// CheckAbusing 어노테이션이 붙은 메소드의 경우에만 초당 요청 제한 적용
		if (isTarget((HandlerMethod)handler)) {
			String key = getIp(request);

			String countKey = InMemoryKeyUtils.generateAbusingRequestCountKey(stage, getUriPrefix(request), key);
			Long count = redisTemplate.opsForValue().increment(countKey);

			if (count == null) {
				throw new AbusingException("requestCount 키 초기화 실패: " + countKey);
			}

			// 최초 요청인 경우 만료 시간 초기화
			if (count == 1) {
				redisTemplate.expire(countKey, RequestUtils.getAbusingTimeLimitSeconds(), TimeUnit.SECONDS);
			}

			// N 번 이상의 요청인 경우 429 응답
			if (count > RequestUtils.getAbusingCountLimit()) {
				throw new AbusingException("Abusing 에러 발생: " + countKey);
			}

		}
		return true;
	}

	private boolean isTarget(HandlerMethod method) {
		return method.hasMethodAnnotation(CheckAbusing.class) || method.getMethod().getDeclaringClass()
			.isAnnotationPresent(CheckAbusing.class);
	}

}

