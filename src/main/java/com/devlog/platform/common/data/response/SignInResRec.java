package com.devlog.platform.common.data.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SignInResRec(long id, String accessToken, String refreshToken) {
}
