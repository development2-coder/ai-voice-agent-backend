package com.infinitio.aivoiceplatform.callrecording.dto.response;

import lombok.*;

/**
 * Call Recording Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallRecordingResponse {

    private String publicId;

    private String callPublicId;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private String storageProvider;

    private Integer durationSeconds;

    private String description;

    private Integer isActive;
}