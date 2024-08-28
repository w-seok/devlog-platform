package com.devlog.platform.common.exception;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

/**
 * Bind 예외 중 클라이언트로부터의 요청 처리 중 발생한 예외.
 */
public class ClientBindException extends BindException {
	public ClientBindException(BindingResult bindingResult) {
		super(bindingResult);
	}
}
