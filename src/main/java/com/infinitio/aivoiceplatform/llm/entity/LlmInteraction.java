package com.infinitio.aivoiceplatform.llm.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "llm_interactions",
        indexes = {
                @Index(
                        name = "idx_llm_interaction_call",
                        columnList = "call_public_id"
                ),
                @Index(
                        name = "idx_llm_interaction_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LlmInteraction
        extends BaseEntity {

    @Column(
            name = "call_public_id",
            nullable = false,
            length = 100
    )
    private String callPublicId;

    @Column(
            name = "request_messages",
            columnDefinition = "LONGTEXT"
    )
    private String requestMessages;

    @Column(
            name = "response_content",
            columnDefinition = "LONGTEXT"
    )
    private String responseContent;

    @Column(
            name = "language",
            length = 20
    )
    private String language;

    @Column(
            name = "provider",
            length = 50
    )
    private String provider;

    @Column(
            name = "model",
            length = 100
    )
    private String model;

    @Column(
            name = "final_response",
            nullable = false
    )
    private Boolean finalResponse;

    @Column(
            name = "latency_ms"
    )
    private Long latencyMs;

    @Column(
            name = "input_tokens"
    )
    private Long inputTokens;

    @Column(
            name = "output_tokens"
    )
    private Long outputTokens;

    @Column(
            name = "total_tokens"
    )
    private Long totalTokens;

    @Column(
            name = "provider_request_id",
            length = 200
    )
    private String providerRequestId;

    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;

    @Column(
            name = "error_message",
            columnDefinition = "TEXT"
    )
    private String errorMessage;
}