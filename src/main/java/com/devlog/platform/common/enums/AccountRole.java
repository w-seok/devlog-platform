package com.devlog.platform.common.enums;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 유저의 역할을 정의한 Enum 클래스
 */
@Getter
@AllArgsConstructor
public enum AccountRole {

	ADMIN("ROLE_ADMIN", List.of(
		com.devlog.platform.common.enums.AccountAuthority.CHANGE_ROLE,
		com.devlog.platform.common.enums.AccountAuthority.ACTIVE_USER,
		com.devlog.platform.common.enums.AccountAuthority.READ_ARTICLE,
		com.devlog.platform.common.enums.AccountAuthority.UPDATE_PROFILE,
		com.devlog.platform.common.enums.AccountAuthority.UPDATE_ARTICLE
	)),

	USER("ROLE_USER", Collections.singletonList(
		AccountAuthority.READ_ARTICLE
	));

	private final String value;
	private final List<AccountAuthority> authorities;

	private static final AccountRole[] VALUES = values();

	public boolean hasAuthority(AccountAuthority authority) {
		return authorities.contains(authority);
	}

	public boolean match(String value) {
		return this.value.equals(value);
	}

	public static AccountRole toAccountRole(String roleName) {
		for (var role : VALUES) {
			if (role.value.equals(roleName)) {
				return role;
			}
		}
		throw new IllegalArgumentException("유효하지 않은 ROLE 이름: " + roleName);
	}

}

