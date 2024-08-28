package com.devlog.platform.common.webhook;

/**
 * 서버 알림 전송 서비스 - 추후 다양한 모듈에서 사용 및 discord뿐 아니라 slack 등 다양한 서비스 사용 목적
 */
public interface AlertWebhook {

	void alertInfo(String title, String infoMessage);

	void alertError(String title, String errorMessage);

	void alertDelete(String title, String deleteMessage);
}
