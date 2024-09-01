package com.devlog.platform.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import com.devlog.platform.common.property.RedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 24 * 60
	* 60, redisNamespace = "${spring.session.redis.namespace}")
public class RedisConfig {

	private final ObjectMapper objectMapper;

	private final RedisProperties redisProperties;

	@PostConstruct
	public void log() {
		log.info("레디스 호스트: {}", redisProperties.getHost());
		log.info("레디스 포트: {}", redisProperties.getPort());
		log.info("SSL 사용 여부: {}", redisProperties.isSslEnabled());
		log.info("레디스 데이터베이스: {}", redisProperties.getDatabase());
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		var redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisProperties.getHost(),
			redisProperties.getPort());

		if (redisProperties.getDatabase() > 0) {
			redisStandaloneConfiguration.setDatabase(redisProperties.getDatabase());
		}

		var lettuceClientConfigurationBuilder = LettuceClientConfiguration.builder();
		if (redisProperties.isSslEnabled()) {
			lettuceClientConfigurationBuilder.useSsl();
		}
		LettuceClientConfiguration lettuceClientConfiguration = lettuceClientConfigurationBuilder.build();

		return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
	}

	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		var redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

		return redisTemplate;
	}

	@Bean
	public RedisTemplate<String, Integer> stringIntegerRedisTemplate(RedisConnectionFactory connectionFactory) {
		var redisTemplate = new RedisTemplate<String, Integer>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	/**
	 * @see <a href="https://github.com/spring-projects/spring-session/issues/124">issue - elastiCache</a>
	 */
	@Bean
	ConfigureRedisAction configureRedisAction() {
		return ConfigureRedisAction.NO_OP;
	}

}
