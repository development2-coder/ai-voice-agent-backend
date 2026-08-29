package com.infinitio.aivoiceplatform.orchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Request DTO used to process a caller transcript.
 *
 * <p>
 * This DTO supports both synchronous and streaming STT
 * integrations.
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
public class ProcessTranscriptRequestDto {

    /**
     * Unique public identifier of the call.
     */
    @NotBlank
    private String callId;

    /**
     * Caller transcript.
     */
    @NotBlank
    private String transcript;

    /**
     * Language detected or supplied by STT.
     */
    private String language;

    /**
     * Indicates whether the transcript is final.
     */
    @Builder.Default
    private boolean finalTranscript = true;

    /**
     * Additional runtime context associated with the transcript.
     */
    private Map<String, Object> context;
}