package com.infinitio.aivoiceplatform.transcript.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the complete conversation transcript of a call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallTranscriptResponse {

    private String callPublicId;

    private boolean available;

    private String fileName;

    private Long sizeBytes;

    private List<CallTranscriptMessageResponse> messages;
}