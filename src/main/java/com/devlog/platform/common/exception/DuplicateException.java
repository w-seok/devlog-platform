package com.devlog.platform.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateException extends RuntimeException {
	public DuplicateException(String value) {
		super("중복된 값: (%s)".formatted(value));
	}
}
