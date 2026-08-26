package com.infinitio.aivoiceplatform.knowledgebase.dto.response;

import lombok.*;

/**
 * Knowledge Base Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseResponse {

    private String publicId;

    private String agentPublicId;

    private String knowledgeBaseCode;

    private String knowledgeBaseName;

    private String description;

    private String knowledgeBaseType;

    private Integer isActive;
}