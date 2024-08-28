package com.devlog.platform.common.data.request;

import static com.devlog.platform.common.util.PatternUtils.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpReqRec(
	@NotBlank @Email(message = "이메일 형식을 지켜주세요.") String email,
	@Pattern(
		regexp = PASSWORD_REGEX,
		message = "패스워드 형식을 지켜주세요.") String password
) {
}
