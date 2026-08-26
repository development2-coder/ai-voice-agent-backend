package com.infinitio.aivoiceplatform.knowledgebase.dto.request;

import com.infinitio.aivoiceplatform.knowledgebase.constant.KnowledgeBaseConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Create Knowledge Base Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKnowledgeBaseRequest {

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "Knowledge base code is required.")
    @Size(max = KnowledgeBaseConstants.CODE_MAX_LENGTH)
    private String knowledgeBaseCode;

    @NotBlank(message = "Knowledge base name is required.")
    @Size(max = KnowledgeBaseConstants.NAME_MAX_LENGTH)
    private String knowledgeBaseName;

    @Size(max = KnowledgeBaseConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Size(max = KnowledgeBaseConstants.TYPE_MAX_LENGTH)
    private String knowledgeBaseType;
}