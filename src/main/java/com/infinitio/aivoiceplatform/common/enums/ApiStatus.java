package com.infinitio.aivoiceplatform.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiStatus {

    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    ERROR("ERROR"),
    WARNING("WARNING");

    private final String value;
}