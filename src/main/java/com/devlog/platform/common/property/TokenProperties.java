package com.devlog.platform.common.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.devlog.platform.common.enums.TokenType;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
	private String accessKey;

	private String refreshKey;

	private long accessTime;

	private long refreshTime;

	@PostConstruct
	public void log() {
		log.info("access token 만료시간: [{} s]", this.accessTime);
		log.info("refresh token 만료시간: [{} s]", this.refreshTime);
	}

	public long getExpiredTime(TokenType type) {
		if (type == null) {
			throw new IllegalArgumentException("Token type must not be null");
		}
		return type == TokenType.ACCESS ? this.accessTime : this.refreshTime;
	}

}
