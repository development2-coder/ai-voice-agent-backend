package com.infinitio.aivoiceplatform.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents record status.
 *
 * Maps with database active flag.
 *
 * 1 = ACTIVE
 * 0 = INACTIVE
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum RecordStatus {

    ACTIVE(1),

    INACTIVE(0);

    private final Integer value;

}