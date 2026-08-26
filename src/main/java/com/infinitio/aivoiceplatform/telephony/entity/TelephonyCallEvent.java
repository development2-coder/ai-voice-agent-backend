package com.infinitio.aivoiceplatform.telephony.entity;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Stores provider events received during a call lifecycle.
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
        name = "telephony_call_events",
        indexes = {
                @Index(
                        name = "idx_telephony_call_event_call_id",
                        columnList = "call_id"
                ),
                @Index(
                        name = "idx_telephony_call_event_provider_event_id",
                        columnList = "provider_event_id"
                ),
                @Index(
                        name = "idx_telephony_call_event_provider_call_id",
                        columnList = "provider_call_id"
                )
        }
)
public class TelephonyCallEvent extends BaseEntity {

    /**
     * Call associated with the provider event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "call_id",
            nullable = false
    )
    private Call call;

    /**
     * Telephony provider.
     */
    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    /**
     * Provider call identifier.
     */
    @Column(
            name = "provider_call_id",
            length = 150
    )
    private String providerCallId;

    /**
     * Provider event identifier.
     */
    @Column(
            name = "provider_event_id",
            length = 150
    )
    private String providerEventId;

    /**
     * Normalized event name.
     */
    @Column(
            name = "event",
            nullable = false,
            length = 50
    )
    private String event;

    /**
     * From phone number.
     */
    @Column(
            name = "from_number",
            length = 30
    )
    private String fromNumber;

    /**
     * To phone number.
     */
    @Column(
            name = "to_number",
            length = 30
    )
    private String toNumber;

    /**
     * Provider event timestamp.
     */
    @Column(
            name = "event_at"
    )
    private LocalDateTime eventAt;

    /**
     * Original provider payload.
     */
    @Lob
    @Column(
            name = "payload",
            columnDefinition = "LONGTEXT"
    )
    private String payload;
}