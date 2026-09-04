package com.infinitio.aivoiceplatform.telephony.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provider-independent request for searching available
 * telephone numbers.
 *
 * <p>
 * The fields represent capabilities supported by the platform.
 * Provider-specific parameters must be mapped inside the
 * corresponding {@code TelephonyProvider} implementation.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberSearchRequestDto {

    /**
     * ISO country code in which the number is required.
     *
     * <p>
     * Example: IN, SG, MY.
     * </p>
     */
    @NotBlank
    private String countryCode;

    /**
     * Optional region or state in which the number is required.
     */
    private String region;

    /**
     * Requested telephone number type.
     *
     * <p>
     * The value represents the platform-level number type
     * and is mapped to the provider-specific value by the
     * provider implementation.
     * </p>
     */
    @NotBlank
    private String numberType;

    /**
     * Indicates whether voice capability is required.
     */
    private Boolean voiceEnabled;

    /**
     * Indicates whether SMS capability is required.
     */
    private Boolean smsEnabled;

    /**
     * Optional pattern that the telephone number should contain.
     */
    private String searchPattern;
}