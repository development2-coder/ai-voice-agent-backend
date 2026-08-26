package com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request;

import com.infinitio.aivoiceplatform.knowledgebasedocument.constant.KnowledgeBaseDocumentConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Knowledge Base Document Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKnowledgeBaseDocumentRequest {

    @NotBlank(message = "Public Id is required.")
    private String publicId;

    @NotBlank(message = "Knowledge base is required.")
    private String knowledgeBasePublicId;

    @NotBlank(message = "Document code is required.")
    @Size(max = KnowledgeBaseDocumentConstants.DOCUMENT_CODE_MAX_LENGTH)
    private String documentCode;

    @NotBlank(message = "Document name is required.")
    @Size(max = KnowledgeBaseDocumentConstants.DOCUMENT_NAME_MAX_LENGTH)
    private String documentName;

    @Size(max = KnowledgeBaseDocumentConstants.DOCUMENT_TYPE_MAX_LENGTH)
    private String documentType;

    @Size(max = KnowledgeBaseDocumentConstants.FILE_NAME_MAX_LENGTH)
    private String fileName;

    @Size(max = KnowledgeBaseDocumentConstants.FILE_URL_MAX_LENGTH)
    private String fileUrl;

    private Long fileSize;

    @Size(max = KnowledgeBaseDocumentConstants.MIME_TYPE_MAX_LENGTH)
    private String mimeType;

    @Size(max = KnowledgeBaseDocumentConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Size(max = KnowledgeBaseDocumentConstants.PROCESSING_STATUS_MAX_LENGTH)
    private String processingStatus;
}