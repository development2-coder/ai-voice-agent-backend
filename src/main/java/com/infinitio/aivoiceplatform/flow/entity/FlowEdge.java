package com.infinitio.aivoiceplatform.flow.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "flow_edges",
        indexes = {
                @Index(
                        name = "idx_edge_source",
                        columnList = "source_node_id"
                ),
                @Index(
                        name = "idx_edge_target",
                        columnList = "target_node_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FlowEdge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "flow_id",
            nullable = false
    )
    private Flow flow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "source_node_id",
            nullable = false
    )
    private FlowNode sourceNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "target_node_id",
            nullable = false
    )
    private FlowNode targetNode;

    /**
     * Example:
     *
     * paymentIntent == YES
     * customerType == CUSTOMER
     * language == mr-IN
     */
    @Column(
            name = "condition_expression",
            length = 1000
    )
    private String conditionExpression;

    @Column(
            name = "priority",
            nullable = false
    )
    private Integer priority;

    @PrePersist
    protected void onCreate() {

        if (priority == null) {
            priority = 0;
        }
    }
}