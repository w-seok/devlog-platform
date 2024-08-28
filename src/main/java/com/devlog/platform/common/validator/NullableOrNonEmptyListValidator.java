package com.devlog.platform.common.validator;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.devlog.platform.common.annotation.NullableOrNonEmptyList;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullableOrNonEmptyListValidator
	implements ConstraintValidator<NullableOrNonEmptyList, List<MultipartFile>> {

	@Override
	public void initialize(NullableOrNonEmptyList constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(List<MultipartFile> valueList, ConstraintValidatorContext context) {
		if (valueList == null || valueList.isEmpty()) {
			return true;
		}
		for (MultipartFile value : valueList) {
			if (value.getSize() <= 0) {
				return false;
			}
		}
		return true;
	}
}
