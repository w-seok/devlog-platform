package com.devlog.platform.common.webhook;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;

import java.lang.management.ManagementFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.devlog.platform.common.enums.AlertType;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 디스코드 웹훅의 rate-limit은 초당 5회로 제한됨.
 * @see <a href="https://discord.com/developers/docs/topics/rate-limits">Rate Limits on Discord's API</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
class DiscordWebhook implements AlertWebhook {

	private static final int COLOR_GREEN = 65280;
	private static final int COLOR_RED = 16711680;

	@Value("${webhook.discord.channel.alert:#{null}}")
	private String alertChannel;
	@Value("${webhook.discord.channel.status:#{null}}")
	private String serverStatusChannel;
	@Value("${webhook.discord.channel.delete:#{null}}")
	private String deleteAlertChannel;
	@Value("${webhook.discord.active:false}")
	private boolean webhookActive;

	@Value("${spring.application.name:#{null}}")
	private String appName;
	@Value("${app.ip.public:#{null}}")
	private String publicIp;
	@Value("${app.ip.local:#{null}}")
	private String localIp;

	private final RestClient restClient;
	private boolean isInit;

	@PostConstruct
	void init() {
		this.isInit = StringUtils.hasText(alertChannel)
			&& StringUtils.hasText(serverStatusChannel)
			&& StringUtils.hasText(deleteAlertChannel);

		log.info("discord webhook 초기화됨. Alert channel: {}, Server status channel: {}, Delete alert channel: {}",
			alertChannel, serverStatusChannel, deleteAlertChannel);
	}

	/**
	 * spring application 시작시 시간 및 ip 기록
	 * @param event
	 */
	@EventListener(ApplicationStartedEvent.class)
	public void onStart(final ApplicationStartedEvent event) {
		if (isInit) {
			String uptime = String.format("%.2f", (double)ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
			sendAlert(AlertType.SERVER_STATUS, "모니터링 디스코드 채널 활성화", "spring start: " + uptime + "초");
		}
	}

	/**
	 * spring application 종료시 시간 및 ip 기록(의도치 않은 종료 체크)
	 * @param event
	 */
	@EventListener(ContextClosedEvent.class)
	public void onClosed(ContextClosedEvent event) {
		if (isInit) {
			String uptime = String.format("%.2f", (double)ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
			sendAlert(AlertType.SERVER_STATUS, "어플리케이션 종료", "spring closed: " + uptime + "초");
		}
	}

	@Async("webhook")
	@Override
	public void alertInfo(String title, String infoMessage) {
		sendAlert(AlertType.INFO, title, infoMessage);
	}

	@Async("webhook")
	@Override
	public void alertError(String title, String errorMessage) {
		sendAlert(AlertType.ERROR, title, errorMessage);
	}

	@Async("webhook")
	@Override
	public void alertDelete(String title, String deleteMessage) {
		sendAlert(AlertType.DELETE, title, deleteMessage);
	}

	/**
	 * 디스코드 웹훅 전송
	 * Todo: 추후 에러 응답이 자주 발생할 경우 retry 로직 추가 및 read timeout 조정 우선은 10초
	 * @param type - 알림 유형
	 * @param title 제목
	 * @param message
	 */
	private void sendAlert(AlertType type, String title, String message) {
		if (!isInit || !webhookActive) {
			log.warn("디스코드 웹훅 초기화 안됨. 스킵");
			return;
		}

		String channel = getChannelForType(type);
		String formattedTitle = type.formatTitle(title, appName);
		String description = formatDescription(type, message);
		int color = getColorForType(type);

		DiscordMessage discordMessage = DiscordMessage.toMessage(formattedTitle, description, color);

		try {
			restClient.post()
				.uri(channel)
				.contentType(APPLICATION_JSON)
				.body(discordMessage)
				.exchange((request, response) -> {
					if (response.getStatusCode().isSameCodeAs(NO_CONTENT)) {
						log.info("디스코드 웹훅 전송 완료");
					} else if (response.getStatusCode().isError()) {
						log.error("디스코드 웹훅 전송 실패 - code: {}. 우선은 skip", response.getStatusCode());
					}
					return response;
				});
		} catch (Exception e) {
			log.error("디스코드 웹훅 전송 실패 우선은 skip", e);
		}
	}

	private String getChannelForType(AlertType type) {
		return switch (type) {
			case INFO, ERROR -> alertChannel;
			case DELETE -> deleteAlertChannel;
			case SERVER_STATUS -> serverStatusChannel;
		};
	}

	private String formatDescription(AlertType type, String message) {
		if (type == AlertType.SERVER_STATUS) {
			return String.format("public: %s | local: %s\n%s", publicIp, localIp, message);
		}
		return String.format("**[%s]**: %s\n**[%s]** **public**: %s | **local**: %s",
			type.getTypeString(), message, appName, publicIp, localIp);
	}

	private int getColorForType(AlertType type) {
		return switch (type) {
			case INFO, SERVER_STATUS -> COLOR_GREEN;
			case ERROR, DELETE -> COLOR_RED;
		};
	}
}
