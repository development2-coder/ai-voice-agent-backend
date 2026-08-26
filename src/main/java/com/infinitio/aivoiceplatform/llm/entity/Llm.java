package com.infinitio.aivoiceplatform.llm.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * LLM Configuration Entity.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "llms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_llm_code",
                        columnNames = "llm_code"
                )
        }
)
public class Llm extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @Column(
            name = "llm_code",
            nullable = false,
            length = 50
    )
    private String llmCode;

    @Column(
            name = "llm_name",
            nullable = false,
            length = 150
    )
    private String llmName;

    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "model",
            nullable = false,
            length = 150
    )
    private String model;

    @Column(
            name = "api_key_reference",
            length = 500
    )
    private String apiKeyReference;

    @Column(
            name = "temperature"
    )
    private Double temperature;

    @Column(
            name = "max_tokens"
    )
    private Integer maxTokens;

    @Column(
            name = "description",
            length = 500
    )
    private String description;
}