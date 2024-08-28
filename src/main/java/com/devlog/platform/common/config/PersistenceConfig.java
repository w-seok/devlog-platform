package com.devlog.platform.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableJpaAuditing
public class PersistenceConfig {

	@Value("${spring.datasource.hikari.schema:}")
	private String schema;

	@PostConstruct
	public void print() {
		log.info("활성화된 DB 스키마: {}", schema);
	}

}
