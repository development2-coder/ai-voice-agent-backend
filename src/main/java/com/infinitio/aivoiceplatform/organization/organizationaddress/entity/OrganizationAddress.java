package com.infinitio.aivoiceplatform.organization.organizationaddress.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Organization Address Entity.
 *
 * Stores Organization Address Information.
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
@Table(name = "organization_addresses")
public class OrganizationAddress extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organization_id",
            nullable = false,
            unique = true
    )
    private Organization organization;

    @Column(name = "address_line_1",
            nullable = false,
            length = 255)
    private String addressLine1;

    @Column(name = "address_line_2",
            length = 255)
    private String addressLine2;

    @Column(name = "city",
            nullable = false,
            length = 100)
    private String city;

    @Column(name = "state",
            nullable = false,
            length = 100)
    private String state;

    @Column(name = "country",
            nullable = false,
            length = 100)
    private String country;

    @Column(name = "postal_code",
            length = 20)
    private String postalCode;

}