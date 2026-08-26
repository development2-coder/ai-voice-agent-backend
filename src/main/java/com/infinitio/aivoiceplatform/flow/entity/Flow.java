package com.infinitio.aivoiceplatform.flow.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "flows",
        indexes = {
                @Index(name = "idx_flow_agent", columnList = "agent_id"),
                @Index(name = "idx_flow_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Flow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "description",
            length = 1000
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "flow_type",
            nullable = false,
            length = 20
    )
    private FlowType flowType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private FlowStatus status;

    @Column(
            name = "version",
            nullable = false
    )
    private Integer version;

    @PrePersist
    protected void onCreate() {

        if (status == null) {
            status = FlowStatus.DRAFT;
        }

        if (version == null) {
            version = 1;
        }
    }
}