package com.devlog.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class DevLogApplication {

	public static void main(String[] args) {
		var app = new SpringApplication(DevLogApplication.class);
		app.addListeners(new ApplicationPidFileWriter());
		app.run(args);
	}
}
