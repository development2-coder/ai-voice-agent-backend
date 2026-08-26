package com.infinitio.aivoiceplatform.phonenumber.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Phone Number Entity.
 *
 * Represents a telephony number associated
 * with an AI Voice Agent.
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
        name = "phone_numbers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_phone_number",
                        columnNames = "phone_number"
                )
        }
)
public class PhoneNumber extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @Column(
            name = "phone_number",
            nullable = false,
            length = 30
    )
    private String phoneNumber;

    @Column(
            name = "display_name",
            length = 150
    )
    private String displayName;

    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "provider_number_id",
            length = 150
    )
    private String providerNumberId;

    @Column(
            name = "country_code",
            length = 10
    )
    private String countryCode;

    @Column(
            name = "country",
            length = 100
    )
    private String country;

    @Column(
            name = "direction",
            length = 20
    )
    private String direction;

    @Column(
            name = "description",
            length = 500
    )
    private String description;
}