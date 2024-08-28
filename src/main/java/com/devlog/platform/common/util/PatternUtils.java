package com.devlog.platform.common.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PatternUtils {

	public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[\\d!@#~^*&%]).{8,}$";
}

