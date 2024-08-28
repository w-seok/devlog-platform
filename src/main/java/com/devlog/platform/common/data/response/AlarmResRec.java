package com.devlog.platform.common.data.response;

import lombok.Builder;

@Builder
public record AlarmResRec(String alarmType, String description) {
}
