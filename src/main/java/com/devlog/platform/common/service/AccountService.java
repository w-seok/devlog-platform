package com.devlog.platform.common.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devlog.platform.common.data.request.PasswordResetReqRec;
import com.devlog.platform.common.data.request.SignInReqRec;
import com.devlog.platform.common.data.request.SignUpReqRec;
import com.devlog.platform.common.entity.AccountEntity;
import com.devlog.platform.common.enums.AccountRole;
import com.devlog.platform.common.exception.DuplicateException;
import com.devlog.platform.common.repository.AccountRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final PasswordEncoder passwordEncoder;

	/**
	 * @param signUpReqRec 회원가입 요청 정보
	 */
	@Transactional
	public void signUp(SignUpReqRec signUpReqRec) {
		if (accountRepository.existsByUserId(signUpReqRec.email())) {
			throw new DuplicateException(signUpReqRec.email());
		}
		String encodedPassword = passwordEncoder.encode(signUpReqRec.password());

		accountRepository.save(AccountEntity.builder()
			.userId(signUpReqRec.email())
			.password(encodedPassword)
			.role(AccountRole.USER)
			.active(true)
			.build());
	}

	/**
	 * 사용자 로그인을 시도하고, 토큰을 생성 후 인 메모리에 저장하고 반환
	 * 비활성화된 유저가 보관 기간내 로그인 시 활성화 처리
	 * @param signInReqRec 로그인 요청 정보
	 * @return
	 */
	@Transactional
	public Long signIn(SignInReqRec signInReqRec) {
		AccountEntity account = accountRepository.findByUserId(signInReqRec.email())
			.orElseThrow(() -> new EntityNotFoundException("Account not found: " + signInReqRec.email()));
		Authentication authentication = attemptAuthentication(signInReqRec);
		log.info("로그인 성공: {}", authentication.getName());
		return account.getId();
	}

	@Transactional
	public void updatePassword(PasswordResetReqRec passwordResetReqRec) {
		AccountEntity account = accountRepository.findByUserId(passwordResetReqRec.email())
			.orElseThrow(() -> new EntityNotFoundException("Account not found: " + passwordResetReqRec.email()));

		account.updatePassword(passwordEncoder.encode(passwordResetReqRec.newPassword()));
	}

	/**
	 * 사용자 인증을 시도하고, 인증된 Authentication 객체를 반환
	 * @param signInReqRec 사용자 로그인 요청 정보
	 * @return
	 */
	private Authentication attemptAuthentication(SignInReqRec signInReqRec) {
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			signInReqRec.email(), signInReqRec.password());
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return authentication;
	}
}
