package com.infinitio.aivoiceplatform.telephony.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provider-neutral request for initiating an outbound call.
 *
 * <p>
 * This DTO intentionally contains no Exotel-specific fields.
 * Provider implementations are responsible for translating
 * this request into their respective API format.
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
public class TelephonyCallRequest {

    /**
     * Internal call public identifier.
     */
    private String callId;

    /**
     * Source/caller phone number.
     */
    private String fromNumber;

    /**
     * Destination/customer phone number.
     */
    private String toNumber;

    /**
     * URL used by the provider for call events.
     */
    private String callbackUrl;

    /**
     * Optional URL used when the provider requires
     * instructions after the call is answered.
     */
    private String answerUrl;

    /**
     * Optional metadata associated with the call.
     *
     * <p>
     * This can contain tenant, dialer, agent, flow,
     * or other internal correlation information.
     * </p>
     */
    private String metadata;
}