package com.devlog.platform.common.enums;

import lombok.Getter;

@Getter
public enum AlertType {
	INFO(":white_check_mark:", "알림"),
	ERROR(":warning:", "예외"),
	DELETE(":wastebasket:", "삭제"),
	SERVER_STATUS("", "서버 상태");

	private final String emoji;
	private final String typeString;

	AlertType(String emoji, String typeString) {
		this.emoji = emoji;
		this.typeString = typeString;
	}

	public String formatTitle(String title, String appName) {
		return this == SERVER_STATUS ? "[%s] %s".formatted(appName, title) : "%s %s".formatted(emoji, title);
	}
}
