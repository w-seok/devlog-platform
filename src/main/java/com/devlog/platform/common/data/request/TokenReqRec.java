package com.devlog.platform.common.data.request;

public record TokenReqRec(
	String accessToken,
	String refreshToken
) {
}
