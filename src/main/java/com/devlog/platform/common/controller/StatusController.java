package com.devlog.platform.common.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class StatusController {

	@Value("${server.port}")
	int port;

	@Value("${app.ip.public:#{null}}")
	String publicIp;

	@GetMapping("/api/local/delay")
	public ResponseEntity<String> delayGet() throws InterruptedException {
		log.info("30초간 작업하는척 ");

		for (int i = 0; i < 30; i++) {
			Thread.sleep(1000);
			if (i % 5 == 0) {
				log.info("{}, {} 초 지남", port, i);
			}
		}

		return ResponseEntity.ok("success");
	}

	@GetMapping("/api/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.status(200).body("hello!, im " + publicIp);
	}

	/**
	 * metric 에러로그 수집 테스트
	 * @return
	 */
	@GetMapping("/error-test")
	public ResponseEntity<Long> errorTest() {
		try {
			List<String> list = new ArrayList<>();
			list.get(2).toString();
		} catch (Exception e) {
			log.error("에러로그 테스트중. 리스트에서 잘못된 접근!", e);
		}

		var date = System.currentTimeMillis();
		return ResponseEntity.ok(date);
	}
}
