package com.infinitio.aivoiceplatform.flow.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "flow_executions",
        indexes = {
                @Index(
                        name = "idx_execution_flow",
                        columnList = "flow_id"
                ),
                @Index(
                        name = "idx_execution_call",
                        columnList = "call_public_id"
                ),
                @Index(
                        name = "idx_execution_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FlowExecution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "flow_id",
            nullable = false
    )
    private Flow flow;

    @Column(
            name = "call_public_id",
            length = 100
    )
    private String callPublicId;

    @Column(
            name = "conversation_public_id",
            length = 100
    )
    private String conversationPublicId;

    @Column(
            name = "current_node_id"
    )
    private Long currentNodeId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private FlowExecutionStatus status;

    /**
     * Runtime variables.
     *
     * Example:
     * {
     *   "customerName": "Kiran",
     *   "language": "mr-IN",
     *   "appointmentDate": "2026-08-20"
     * }
     */
    @Column(
            name = "context_data",
            columnDefinition = "LONGTEXT"
    )
    private String contextData;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(
            name = "error_message",
            columnDefinition = "TEXT"
    )
    private String errorMessage;
}