package com.infinitio.aivoiceplatform.telephony.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a telephone number returned by a telephony provider.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberResponseDto {

    private String provider;

    private String e164Number;

    private String type;

    private String status;
}