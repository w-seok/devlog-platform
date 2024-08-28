package com.devlog.platform.common.data.request;

import static com.devlog.platform.common.util.PatternUtils.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetReqRec(
	@NotBlank
	String email,

	@NotBlank
	@Pattern(
		regexp = PASSWORD_REGEX,
		message = "패스워드 형식을 지켜주세요."
	)
	String newPassword
) {
}
