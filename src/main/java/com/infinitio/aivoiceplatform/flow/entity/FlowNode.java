package com.infinitio.aivoiceplatform.flow.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "flow_nodes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_flow_node_key",
                        columnNames = {
                                "flow_id",
                                "node_key"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_flow_node_flow",
                        columnList = "flow_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FlowNode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "flow_id",
            nullable = false
    )
    private Flow flow;

    @Column(
            name = "node_key",
            nullable = false,
            length = 100
    )
    private String nodeKey;

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "node_type",
            nullable = false,
            length = 40
    )
    private FlowNodeType nodeType;

    /**
     * Node-specific configuration.
     *
     * Stored as JSON string.
     */
    @Column(
            name = "configuration",
            columnDefinition = "TEXT"
    )
    private String configuration;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;
}