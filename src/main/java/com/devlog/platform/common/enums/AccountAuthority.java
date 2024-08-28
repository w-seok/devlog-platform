package com.devlog.platform.common.enums;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 유저가 가질 수 있는 권한을 정의한 Enum 클래스
 */
@Getter
@AllArgsConstructor
public enum AccountAuthority {

	CHANGE_ROLE,
	ACTIVE_USER,
	READ_ARTICLE,
	UPDATE_PROFILE,
	UPDATE_ARTICLE;

	public String getValue() {
		return this.name();
	}

	private final GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(this.name());

}
