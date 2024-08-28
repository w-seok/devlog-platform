package com.devlog.platform.common.config;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.*;

import java.util.TimeZone;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.devlog.platform.common.interceptor.AbusingInterceptor;
import com.devlog.platform.common.interceptor.PrivateNetworkInterceptor;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final PrivateNetworkInterceptor privateNetworkInterceptor;
	private final AbusingInterceptor abusingInterceptor;

	/**
	 * 서버 시작시 한국 시간대로 설정
	 */
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	/**
	 * hikariCp 설정을 위한 Config
	 * @return
	 */
	@Bean(name = "datasource")
	@Profile("!test")
	@ConfigurationProperties("spring.datasource.hikari")
	public DataSource dataSourceProperties() {
		return DataSourceBuilder.create()
			.type(HikariDataSource.class)
			.build();
	}

	@Profile("!test")
	@Bean
	public FlywayMigrationStrategy cleanMigrateStrategy() {
		return flyway -> {
			flyway.repair();
			flyway.migrate();
		};
	}

	@Profile("test")
	@Bean
	public FlywayMigrationStrategy cleanMigrateStrategyForTest() {
		return flyway -> {
			flyway.clean();
			flyway.repair();
			flyway.migrate();
		};
	}

	/**
	 * 기본적인 RestClient timeout 설정
	 * 추가적인 설정 필요시 해당 클래스에서 설정
	 * @return
	 */
	@Bean
	RestClient restClient() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(10_000);
		factory.setReadTimeout(10_000);
		return RestClient.builder()
			.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.requestFactory(factory)
			.build();
	}

	@Override
	public void addInterceptors(@NonNull InterceptorRegistry registry) {
		WebMvcConfigurer.super.addInterceptors(registry);
		registry.addInterceptor(privateNetworkInterceptor);
		registry.addInterceptor(abusingInterceptor);
	}
}
