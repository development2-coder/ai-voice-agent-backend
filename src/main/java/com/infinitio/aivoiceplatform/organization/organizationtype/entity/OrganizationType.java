package com.infinitio.aivoiceplatform.organization.organizationtype.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Organization Type Entity.
 *
 * Stores master data defining the type/category of an organization.
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
@Table(name = "organization_types")
public class OrganizationType extends BaseEntity {

    @Column(
            name = "organization_type_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String organizationTypeCode;

    @Column(
            name = "organization_type_name",
            nullable = false,
            unique = true,
            length = 100
    )
    private String organizationTypeName;

    @Column(
            name = "description",
            length = 255
    )
    private String description;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;
}