package com.devlog.platform.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.devlog.platform.common.validator.NullableOrNonEmptyListValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 이미지의 nullable 혹은 size가 0보다 큰 경우만 validate 하기위해서
 */
@Documented
@Constraint(validatedBy = NullableOrNonEmptyListValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NullableOrNonEmptyList {
	String message() default "해당 파라미터는 null이 아닌 경우 size가 0이어서는 안됩니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
