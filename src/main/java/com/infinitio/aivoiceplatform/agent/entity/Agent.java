package com.infinitio.aivoiceplatform.agent.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Agent Entity.
 *
 * Represents an AI Voice Agent.
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
@Table(name = "agents")
public class Agent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(
            name = "agent_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String agentCode;

    @Column(
            name = "agent_name",
            nullable = false,
            length = 150
    )
    private String agentName;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(
            name = "welcome_message",
            length = 1000
    )
    private String welcomeMessage;

    @Column(
            name = "language",
            length = 50
    )
    private String language;

}