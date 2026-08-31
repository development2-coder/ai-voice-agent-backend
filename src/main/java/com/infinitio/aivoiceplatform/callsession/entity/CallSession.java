package com.infinitio.aivoiceplatform.callsession.entity;

import java.util.HashMap;
import java.util.Map;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.converter.CallSessionSlotsConverter;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents the persistent runtime state of a voice call session.
 *
 * <p>
 * Call-session runtime metadata is persisted in MySQL.
 * Conversation history is stored separately as a local JSONL
 * file and archived as a GZIP-compressed JSONL file.
 * </p>
 *
 * <p>
 * The selected Flow public identifier is persisted with the
 * Call Session so that real-time provider callbacks such as
 * Voice Gateway START can recover the exact Flow selected
 * when the call was created.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Entity
@Table(
        name = "call_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_call_session_call_id",
                        columnNames = "call_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_call_session_tenant",
                        columnList = "tenant_id"
                ),
                @Index(
                        name = "idx_call_session_agent",
                        columnList = "agent_id"
                ),
                @Index(
                        name = "idx_call_session_flow",
                        columnList = "flow_public_id"
                ),
                @Index(
                        name = "idx_call_session_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_call_session_deleted",
                        columnList = "is_deleted"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CallSession extends BaseEntity {

    /**
     * Public identifier of the call.
     */
    @Column(
            name = "call_id",
            nullable = false,
            length = 100
    )
    private String callId;

    /**
     * Tenant public identifier.
     */
    @Column(
            name = "tenant_id",
            nullable = false,
            length = 100
    )
    private String tenantId;

    /**
     * Agent public identifier.
     */
    @Column(
            name = "agent_id",
            nullable = false,
            length = 100
    )
    private String agentId;

    /**
     * Agent configuration version used for this call.
     */
    @Column(
            name = "agent_version",
            nullable = false
    )
    private Integer agentVersion;

    /**
     * Public identifier of the Flow selected for this call.
     *
     * <p>
     * This is the Flow that was selected when the Call Session
     * was created. It is persisted because the later real-time
     * Voice Gateway callback contains the call identifier but
     * must not independently choose a Flow.
     * </p>
     */
    @Column(
            name = "flow_public_id",
            length = 100
    )
    private String flowPublicId;

    /**
     * Current conversation turn number.
     *
     * <p>
     * Conversation messages themselves are stored in the
     * JSONL conversation archive.
     * </p>
     */
    @Column(
            name = "turn_index",
            nullable = false
    )
    private Integer turnIndex;

    /**
     * Collected conversation slots persisted as JSON.
     *
     * <p>
     * Only the current slot state is kept in MySQL.
     * </p>
     */
    @Convert(
            converter = CallSessionSlotsConverter.class
    )
    @Column(
            name = "collected_slots",
            columnDefinition = "LONGTEXT"
    )
    @lombok.Builder.Default
    private Map<String, String> collectedSlots =
            new HashMap<>();

    /**
     * Current flow node identifier.
     */
    @Column(
            name = "flow_node_id",
            length = 100
    )
    private String flowNodeId;

    /**
     * Conversation language.
     */
    @Column(
            name = "language",
            length = 50
    )
    private String language;

    /**
     * Current call-session status.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private CallSessionStatus status;

    /**
     * Public identifier of the active Flow Execution.
     */
    @Column(
            name = "flow_execution_public_id",
            length = 100
    )
    private String flowExecutionPublicId;

    /**
     * Relative storage key of the conversation file.
     *
     * <p>
     * During an active call this points to a JSONL file.
     * After the call ends it points to the archived JSONL.GZ file.
     * </p>
     */
    @Column(
            name = "conversation_storage_key",
            length = 500
    )
    private String conversationStorageKey;

    /**
     * Adds or updates a collected slot.
     *
     * @param slotName slot name
     * @param value slot value
     */
    public void addCollectedSlot(
            String slotName,
            String value) {

        if (collectedSlots == null) {

            collectedSlots =
                    new HashMap<>();
        }

        collectedSlots.put(
                slotName,
                value
        );
    }

    /**
     * Increments the conversation turn index.
     */
    public void incrementTurnIndex() {

        if (turnIndex == null) {

            turnIndex = 0;
        }

        turnIndex++;
    }
}