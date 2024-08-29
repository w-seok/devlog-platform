package com.devlog.platform.common.config;

import static org.springframework.http.HttpStatus.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.devlog.platform.common.data.response.ErrorResDto;
import com.devlog.platform.common.exception.AbusingException;
import com.devlog.platform.common.exception.ClientBindException;
import com.devlog.platform.common.exception.DuplicateException;
import com.devlog.platform.common.exception.ForbiddenException;
import com.devlog.platform.common.webhook.AlertWebhook;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ErrorControllerAdvice {
	private final AlertWebhook alertWebhook;

	private static final String BAD_REQUEST_BODY = "잘못된 요청입니다!";
	private static final String FORBIDDEN_403_BODY = "접근할 수 없어요!";
	private static final String ERROR_500_BODY = "잠시 후 다시 확인해주세요!";

	@ExceptionHandler(value = {ForbiddenException.class, AccessDeniedException.class})
	public ResponseEntity<Object> redirect403(Exception exception) {
		log.debug("403 분석용", exception);
		return ResponseEntity.status(FORBIDDEN).body(FORBIDDEN_403_BODY);
	}

	@ExceptionHandler(value = DataIntegrityViolationException.class)
	public ResponseEntity<Object> handle422(Exception exception) {
		log.debug("422 에러분석용", exception);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ERROR_500_BODY);
	}

	/**
	 * 인증 예외의 핸들링
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResDto> handleAuthenticationException(BadCredentialsException exception) {
		log.debug("유효하지 않은 계정정보입니다.", exception);
		return ResponseEntity.badRequest().body(new ErrorResDto("유효하지않은 계정정보입니다"));
	}

	/**
	 * 클라이언트로부터의 요청 데이터 처리 중 발생한 바인딩 예외의 핸들링
	 * 예외 세부 정보를 응답으로 전달하지 않음
	 *
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(value = ClientBindException.class)
	public ResponseEntity<ErrorResDto> handleClientBindException(ClientBindException exception) {
		log.error("Client 바인딩 중 오류 발생", exception);
		return ResponseEntity.badRequest().body(new ErrorResDto("Client 바인딩 중 오류 발생"));
	}

	/**
	 * 쿼리 스트링 요청 데이터 처리 중 발생한 예외의 핸들링
	 * 예외 세부 정보를 응답으로 전달함
	 *
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(value = ConstraintViolationException.class)
	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException exception) {
		log.error("쿼리스트링 예외 핸들링", exception);
		return ResponseEntity.badRequest().body(BAD_REQUEST_BODY);
	}

	/**
	 * 관리자의(백오피스) 내부에서 요청 데이터 처리 중 발생한 바인딩 예외의 핸들링
	 * 예외 세부 정보를 응답으로 전달함
	 *
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(value = BindException.class)
	public ResponseEntity<ErrorResDto> handleBindException(BindException exception) {
		log.error("바인딩 중 오류 발생", exception);
		ErrorResDto response = new ErrorResDto("바인딩 중 오류 발생", exception);
		return ResponseEntity.badRequest().body(response);
	}

	/**
	 * 중복된 값이 발생했을 때의 핸들링
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(value = DuplicateException.class)
	public ResponseEntity<ErrorResDto> handleDuplicateException(DuplicateException exception) {
		log.debug("중복된 값 발생", exception);
		ErrorResDto response = new ErrorResDto(exception);
		return ResponseEntity.status(CONFLICT).body(response);
	}

	/**
	 * 동일 사용자의 과다 요청이 발생하면 429 응답을 보냄
	 */
	@ExceptionHandler(value = AbusingException.class)
	public ResponseEntity<Object> handleAbusing(AbusingException exception) {
		log.warn(exception.getMessage());
		return ResponseEntity.status(TOO_MANY_REQUESTS).body(ERROR_500_BODY);
	}


	/**
	 * 그 외의 예외의 핸들링
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<Object> handle(Exception exception) {
		log.debug("500 에러분석용", exception);
		alertWebhook.alertError("500 에러 발생 분석을 하라", exception.getMessage());
		return ResponseEntity.internalServerError().body(ERROR_500_BODY);
	}
}
