package com.devlog.platform.common.security;

import static com.devlog.platform.common.enums.TokenType.*;
import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.devlog.platform.common.data.TokenRec;
import com.devlog.platform.common.enums.TokenType;
import com.devlog.platform.common.property.TokenProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

	private final TokenProperties tokenProperties;
	private SecretKey accessKey;
	private SecretKey refreshKey;

	private static final String AUTHORITIES_KEY = "auth";

	@PostConstruct
	public void initialize() {
		byte[] accessKeyBytes = Decoders.BASE64.decode(tokenProperties.getAccessKey());
		byte[] secretKeyBytes = Decoders.BASE64.decode(tokenProperties.getRefreshKey());
		this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);
		this.refreshKey = Keys.hmacShaKeyFor(secretKeyBytes);
	}

	public TokenRec generateToken(Authentication authentication) {
		return new TokenRec(buildJwt(ACCESS, authentication), buildJwt(TokenType.REFRESH, authentication));
	}

	private String buildJwt(TokenType type, Authentication authentication) {
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		SecretKey key = getSecretKeyByType(type);
		try {
			return Jwts.builder()
				.subject(authentication.getName())
				.claim(AUTHORITIES_KEY, authorities)
				.expiration(new Date(new Date().getTime() + tokenProperties.getExpiredTime(type)))
				.signWith(key)
				.compact();
		} catch (Exception e) {
			log.error("JWT 토큰 생성 실패 - type: [{}], subject: [{}]", type, authentication.getName(), e);
			throw new RuntimeException("JWT 토큰 생성 실패", e);
		}
	}

	public Authentication getAuthenticationFromAcs(String token) {
		return getAuthentication(ACCESS, token);
	}

	public Authentication getAuthenticationFromRef(String token) {
		return getAuthentication(TokenType.REFRESH, token);
	}

	private Authentication getAuthentication(TokenType type, String token) {
		Claims claims = parseJwt(type, token).getPayload();

		Collection<? extends GrantedAuthority> authorities = Arrays.stream(
				claims.get(AUTHORITIES_KEY).toString().split(","))
			.map(SimpleGrantedAuthority::new)
			.collect(toList());

		return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
	}

	private Jws<Claims> parseJwt(TokenType type, String token) throws ExpiredJwtException, IllegalArgumentException {
		try {
			SecretKey key = getSecretKeyByType(type);
			return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
		} catch (SignatureException e) {
			log.error("유효하지 않은 서명의 토큰입니다");
		} catch (MalformedJwtException e) {
			log.error("유효하지 않은 JWT입니다.");
		} catch (ExpiredJwtException e) {
			log.error("만료된 JWT입니다.");
			throw e;
		} catch (UnsupportedJwtException e) {
			log.error("지원되지 않는 JWT입니다.");
		} catch (IllegalArgumentException e) {
			log.error("jwt claim is empty");
		}
		throw new IllegalArgumentException();
	}

	public boolean isAccessTokenCanBeReissued(String token) {
		try {
			// 토큰 파싱. 만료되었거나 문제가 있는 경우, 예외 발생
			Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token);
			// 토큰이 유효한 경우: 재발급이 필요하지 않으므로 false를 반환
			return false;
		} catch (ExpiredJwtException e) {
			// 토큰이 만료된 경우: 재발급이 가능하므로 true를 반환
			return true;
		} catch (Exception e) {
			// 기타 모든 예외 처리: 재발급이 불가능하므로 false를 반환
			log.error("토큰 재발급 가능 여부 확인 중 오류 발생", e);
			return false;
		}
	}

	private SecretKey getSecretKeyByType(TokenType type) {
		if (ACCESS.equals(type)) {
			return accessKey;
		}
		return refreshKey;
	}

}
