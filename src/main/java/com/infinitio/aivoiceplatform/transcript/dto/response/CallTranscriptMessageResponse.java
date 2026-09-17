package com.infinitio.aivoiceplatform.transcript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents one conversation message from a call transcript.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallTranscriptMessageResponse {

    private Integer sequenceNumber;

    private String speakerType;

    private String text;

    private String language;

    private String source;

    private String timestamp;
}