package com.infinitio.aivoiceplatform.tts.dto.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a runtime text-to-speech synthesis request.
 *
 * <p>
 * The request contains the call context, target language, selected
 * Sarvam speaker, text to be synthesized, and optional speech parameters.
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
public class TtsSynthesisRequest {

    /**
     * Unique call identifier.
     */
    private String callId;

    /**
     * Target language code.
     *
     * <p>
     * Example: en-IN, hi-IN, mr-IN.
     * </p>
     */
    private String language;

    /**
     * Sarvam speaker selected by the frontend.
     *
     * <p>
     * Example: shubh, ratan, aditya, priya, ishita, ritu.
     * </p>
     */
    private String speaker;

    /**
     * Text to be converted into speech.
     */
    private String text;

    /**
     * Speech pace.
     *
     * <p>
     * When not supplied, the configured/default provider value
     * will be used.
     * </p>
     */
    private Double pace;

    /**
     * Requested speech sample rate in Hz.
     *
     * <p>
     * When not supplied, the configured/default provider value
     * will be used.
     * </p>
     */
    private Integer speechSampleRate;

    /**
     * Indicates whether this is the final response for the current
     * conversation turn.
     */
    private boolean finalResponse;
}