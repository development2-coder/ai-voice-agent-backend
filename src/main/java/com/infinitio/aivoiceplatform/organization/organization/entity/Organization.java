package com.infinitio.aivoiceplatform.organization.organization.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.organization.organizationstatus.entity.OrganizationStatus;
import com.infinitio.aivoiceplatform.organization.organizationtype.entity.OrganizationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Organization Entity.
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
@Table(name = "organizations")
public class Organization extends BaseEntity {

    @Column(
            name = "organization_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String organizationCode;

    @Column(
            name = "organization_name",
            nullable = false,
            length = 150
    )
    private String organizationName;

    @Column(
            name = "legal_name",
            length = 200
    )
    private String legalName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organization_type_id",
            nullable = false
    )
    private OrganizationType organizationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organization_status_id",
            nullable = false
    )
    private OrganizationStatus organizationStatus;

    @Column(
            name = "email",
            length = 150
    )
    private String email;

    @Column(
            name = "mobile_number",
            length = 20
    )
    private String mobileNumber;

    @Column(
            name = "website",
            length = 255
    )
    private String website;

    @Column(
            name = "registration_number",
            length = 100
    )
    private String registrationNumber;

    @Column(
            name = "tax_identification_number",
            length = 100
    )
    private String taxIdentificationNumber;

    @Column(
            name = "timezone",
            length = 50
    )
    private String timezone;

    @Column(
            name = "currency",
            length = 20
    )
    private String currency;

    @Column(
            name = "date_format",
            length = 20
    )
    private String dateFormat;

    @Column(
            name = "time_format",
            length = 20
    )
    private String timeFormat;

    @Column(
            name = "language",
            length = 20
    )
    private String language;
}