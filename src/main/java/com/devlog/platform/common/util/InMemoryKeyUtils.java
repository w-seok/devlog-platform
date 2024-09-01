package com.devlog.platform.common.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InMemoryKeyUtils {

	/**
	 * 사용자의 abusing check를 위한 key 생성 <br>
	 * {prefix}:spring:abusingCheck:{requestUri}:{key}
	 * @param prefix
	 * @param requestUri
	 * @param key
	 * @return
	 */
	public static String generateAbusingRequestCountKey(String prefix, String requestUri, String key) {
		return String.format("%s:spring:abusingCheck:%s:%s", prefix, requestUri, key);
	}
}
