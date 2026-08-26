package com.infinitio.aivoiceplatform.telephony.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provider-neutral response after initiating a telephony call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelephonyCallResponse {

    /**
     * Provider name.
     *
     * <p>
     * Example: EXOTEL.
     * </p>
     */
    private String provider;

    /**
     * Provider-generated call identifier.
     */
    private String providerCallId;

    /**
     * Current provider call status.
     *
     * <p>
     * Example: INITIATED, RINGING, ANSWERED,
     * COMPLETED, FAILED.
     * </p>
     */
    private String status;

    /**
     * Provider failure reason when applicable.
     */
    private String failureReason;
}