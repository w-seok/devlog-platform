package com.devlog.platform.common.data.response;

import static java.util.stream.Collectors.*;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResDto {

	private String description;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("bindErrors")
	private List<BindErrorResponseDto> bindErrors;

	public ErrorResDto(String description) {
		this.description = description;
	}

	public ErrorResDto(String description, Errors errors) {
		this(description);
		setCustomFieldErrors(errors.getFieldErrors());
	}

	public ErrorResDto(Exception exception) {
		this.description = exception.getMessage();
	}

	private void setCustomFieldErrors(List<FieldError> fieldErrors) {
		this.bindErrors = fieldErrors.stream().map(error -> BindErrorResponseDto.builder()
				.field(error.getField())
				.input(error.getRejectedValue())
				.message(error.getDefaultMessage())
				.build())
			.collect(toList());
	}

	/**
	 * 컨트롤러에서 요청 데이터 처리 중 발생한 바인딩 예외의 핸들링을 위한 DTO
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	private static class BindErrorResponseDto {
		String field;
		Object input;
		String message;
	}
}
