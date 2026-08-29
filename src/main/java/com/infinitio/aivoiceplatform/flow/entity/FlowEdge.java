package com.infinitio.aivoiceplatform.flow.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Represents a connection between two nodes in a Flow.
 *
 * <p>
 * A FlowEdge connects a specific output port of the source node
 * to a specific input port of the target node.
 * </p>
 *
 * <p>
 * The port-based model is required for branching flows such as:
 * CONDITION.true -> AI_RESPONSE
 * CONDITION.false -> MESSAGE
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
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
                ),
                @Index(
                        name = "idx_edge_source_port",
                        columnList = "source_node_id,source_port"
                ),
                @Index(
                        name = "idx_edge_flow_source_port",
                        columnList = "flow_id,source_node_id,source_port"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FlowEdge extends BaseEntity {

    /**
     * Flow to which this edge belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "flow_id",
            nullable = false
    )
    private Flow flow;

    /**
     * Source node of the connection.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "source_node_id",
            nullable = false
    )
    private FlowNode sourceNode;

    /**
     * Output port of the source node.
     *
     * <p>
     * Example:
     * CONDITION -> "true"
     * </p>
     */
    @Column(
            name = "source_port",
            nullable = false,
            length = 100
    )
    private String sourcePort;

    /**
     * Target node of the connection.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "target_node_id",
            nullable = false
    )
    private FlowNode targetNode;

    /**
     * Input port of the target node.
     *
     * <p>
     * Most standard nodes use the "main" input port.
     * </p>
     */
    @Column(
            name = "target_port",
            nullable = false,
            length = 100
    )
    private String targetPort;

    /**
     * Optional label displayed on the visual connection.
     *
     * <p>
     * Example:
     * YES, NO, SUCCESS, FAILURE
     * </p>
     */
    @Column(
            name = "label",
            length = 150
    )
    private String label;

    /**
     * Optional condition expression associated with this edge.
     *
     * <p>
     * Example:
     * accountType == 'SAVINGS'
     * </p>
     */
    @Column(
            name = "condition_expression",
            length = 1000
    )
    private String conditionExpression;

    /**
     * Execution priority.
     *
     * <p>
     * Lower values are evaluated first.
     * </p>
     */
    @Column(
            name = "priority",
            nullable = false
    )
    private Integer priority;

    /**
     * Initializes default edge priority.
     */
    @PrePersist
    protected void onCreate() {

        if (priority == null) {
            priority = 0;
        }
    }
}