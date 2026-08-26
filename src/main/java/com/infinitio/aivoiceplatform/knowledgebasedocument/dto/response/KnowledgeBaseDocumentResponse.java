package com.infinitio.aivoiceplatform.knowledgebasedocument.dto.response;

import lombok.*;

/**
 * Knowledge Base Document Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseDocumentResponse {

    private String publicId;

    private String knowledgeBasePublicId;

    private String documentCode;

    private String documentName;

    private String documentType;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String mimeType;

    private String description;

    private String processingStatus;

    private Integer isActive;
}