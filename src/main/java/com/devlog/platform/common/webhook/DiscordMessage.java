package com.devlog.platform.common.webhook;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Discord Webhook Message
 * @see <a href="https://discord.com/developers/docs/resources/webhook#execute-webhook">요청-응답 명세</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DiscordMessage {
	private String content;
	private String username;
	@JsonProperty("avatar_url")
	private String avatarUrl;
	private boolean tts;
	@JsonProperty("embeds")
	@Builder.Default
	private List<EmbedObject> embedList = new ArrayList<>();

	void addEmbed(EmbedObject embed) {
		this.embedList.add(embed);
	}

	@Data
	static class EmbedObject {
		private String title;
		private String description;
		private String url;
		private Integer color;

		private Footer footer;
		private Thumbnail thumbnail;
		private Image image;
		private Author author;
		private List<Field> fields = new ArrayList<>();

		void addField(String name, String value, boolean inline) {
			this.fields.add(new Field(name, value, inline));
		}

		@Data
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		private static class Footer {
			private String text;
			private String iconUrl;
		}

		@Data
		@Builder
		private static class Thumbnail {
			private String url;
		}

		@Data
		@Builder
		private static class Image {
			private String url;
		}

		@Data
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		private static class Author {
			private String name;
			private String url;
			@JsonProperty("icon_url")
			private String iconUrl;
		}

		@Data
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		static class Field {
			private String name;
			private String value;
			private boolean inline;
		}
	}

	static DiscordMessage toMessage(String title, String description, Integer color) {
		DiscordMessage message = new DiscordMessage();
		EmbedObject embedObject = new DiscordMessage.EmbedObject();
		embedObject.setTitle(title);
		embedObject.setDescription(description);
		embedObject.setColor(color);
		message.addEmbed(embedObject);
		return message;
	}

	static DiscordMessage toMessage(String title, String description, Integer color, List<EmbedObject.Field> fields) {
		DiscordMessage message = new DiscordMessage();
		EmbedObject embedObject = new DiscordMessage.EmbedObject();
		embedObject.setTitle(title);
		embedObject.setDescription(description);
		embedObject.setColor(color);
		for (var field : fields) {
			embedObject.addField(field.name, field.value, field.inline);
		}
		message.addEmbed(embedObject);
		return message;
	}
}
