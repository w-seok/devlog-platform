package com.devlog.platform.common.controller;

import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devlog.platform.common.annotation.CheckAbusing;
import com.devlog.platform.common.data.request.PasswordResetReqRec;
import com.devlog.platform.common.data.request.SignInReqRec;
import com.devlog.platform.common.data.request.SignUpReqRec;
import com.devlog.platform.common.data.request.TokenReqRec;
import com.devlog.platform.common.data.response.SignInResRec;
import com.devlog.platform.common.exception.ClientBindException;
import com.devlog.platform.common.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
		"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
	);

	private final AccountService accountService;

	/**
	 * 회원가입
	 * @param signUpReqRec 회원가입 요청 정보
	 * @param result 바인딩 결과
	 * @return
	 * @throws BindException 바인딩 오류
	 */
	@PostMapping("/sign-up")
	public ResponseEntity<String> signUp(@Validated @RequestBody SignUpReqRec signUpReqRec,
		BindingResult result) throws BindException {

		if (result.hasErrors()) {
			throw new BindException(result);
		}
		accountService.signUp(signUpReqRec);
		log.info("{} 회원가입 성공", signUpReqRec.email());
		return ResponseEntity.ok().body("success");
	}

	/**
	 * 로그인
	 * @param signInReqRec 로그인 요청 정보
	 * @param result 바인딩 결과
	 * @return 로그인 성공 시 토큰 반환
	 * @throws ClientBindException client 바인딩 오류
	 */
	@CheckAbusing
	@PostMapping("/sign-in")
	public ResponseEntity<SignInResRec> signIn(@Validated @RequestBody SignInReqRec signInReqRec,
		BindingResult result) throws
		ClientBindException {

		if (result.hasErrors()) {
			throw new ClientBindException(result);
		}
		SignInResRec signInResRec = accountService.signIn(signInReqRec);
		log.debug("{} 로그인 성공", signInReqRec.userId());
		return ResponseEntity.ok().body(signInResRec);
	}

	/**
	 * 비밀번호 재설정
	 * @param passwordResetReqRec 비밀번호 재설정 요청 정보
	 * @param result 바인딩 결과
	 * @throws BindException 바인딩 오류
	 */
	@PutMapping("/password")
	public ResponseEntity<String> modifyPassword(@Validated @RequestBody PasswordResetReqRec passwordResetReqRec,
		BindingResult result) throws
		BindException {

		if (result.hasErrors()) {
			throw new ClientBindException(result);
		}
		accountService.updatePassword(passwordResetReqRec);
		return ResponseEntity.ok().body("success");
	}

	/**
	 * 토큰 재발급
	 * accessToken의 만료 유무 확인 후 만료시 재발급
	 * @param tokenReqRec 토큰 재발급 요청 정보
	 * @return
	 */
	@PostMapping("/token")
	public ResponseEntity<String> reissueToken(@Validated @RequestBody TokenReqRec tokenReqRec,
		BindingResult result) throws BindException {
		if (result.hasErrors()) {
			throw new BindException(result);
		}
		try {
			return ResponseEntity.ok(
				accountService.reissueToken(tokenReqRec.accessToken(), tokenReqRec.refreshToken()));
		} catch (Exception exception) {
			log.error("토큰 재발급 실패", exception);
			return ResponseEntity.badRequest().build();
		}
	}
}
