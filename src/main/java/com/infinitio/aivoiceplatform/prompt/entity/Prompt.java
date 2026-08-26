package com.infinitio.aivoiceplatform.prompt.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Prompt Entity.
 *
 * Represents the prompt configuration of an AI Voice Agent.
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
@Table(name = "prompts")
public class Prompt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(
            name = "prompt_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String promptCode;

    @Column(
            name = "prompt_name",
            nullable = false,
            length = 150
    )
    private String promptName;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Lob
    @Column(
            name = "system_prompt",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String systemPrompt;

    @Column(
            name = "prompt_type",
            length = 50
    )
    private String promptType;

    @Column(
            name = "version",
            length = 20
    )
    private String version;

    @Column(
            name = "is_default",
            nullable = false
    )
    private Boolean defaultPrompt;

    @PrePersist
    public void initializeDefaults() {

        if (defaultPrompt == null) {
            defaultPrompt = false;
        }
    }
}