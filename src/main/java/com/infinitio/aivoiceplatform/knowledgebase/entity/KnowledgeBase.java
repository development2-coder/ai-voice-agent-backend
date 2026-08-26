package com.infinitio.aivoiceplatform.knowledgebase.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Knowledge Base Entity.
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
        name = "knowledge_bases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_knowledge_base_code",
                        columnNames = "knowledge_base_code"
                )
        }
)
public class KnowledgeBase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @Column(
            name = "knowledge_base_code",
            nullable = false,
            length = 50
    )
    private String knowledgeBaseCode;

    @Column(
            name = "knowledge_base_name",
            nullable = false,
            length = 150
    )
    private String knowledgeBaseName;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(
            name = "knowledge_base_type",
            length = 50
    )
    private String knowledgeBaseType;
}