package com.devlog.platform.common.security;

import static com.devlog.platform.common.util.RequestUtils.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.util.MimeTypeUtils.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {

	private final TokenProvider tokenProvider;
	private final RedisTemplate<String, String> redisTemplate;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain chain) throws ServletException, IOException {
		String token = request.getHeader(AUTHORIZATION);
		String ip = getIp(request);
		String uri = request.getRequestURI();

		if (!StringUtils.hasText(token)) {
			log.debug("토큰 없음, ip: {}, uri: {}", ip, uri);
			chain.doFilter(request, response);
			return;
		}
		try {
			Authentication authentication = tokenProvider.getAuthenticationFromAcs(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.debug("{}: 인증 정보 security context 저장, uri: {}", authentication.getName(), uri);

		} catch (ExpiredJwtException expiredJwtException) {
			log.debug("만료된 토큰, ip: {}, uri: {}", ip, uri);
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.setContentType(APPLICATION_JSON_VALUE);
			return;
		} catch (Exception e) {
			log.debug("인가 처리 실패 기록 : ip: {}, uri: {} - {}", ip, uri, e.getMessage());
		}
		chain.doFilter(request, response);
	}
}
