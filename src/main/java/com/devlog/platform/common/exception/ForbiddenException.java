package com.devlog.platform.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
	public ForbiddenException(String address, String path) {
		super("제한구역 접근(%s): [%s]".formatted(address, path));
	}
}
