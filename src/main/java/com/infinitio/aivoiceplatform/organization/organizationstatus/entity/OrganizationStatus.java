package com.infinitio.aivoiceplatform.organization.organizationstatus.entity;

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
 * Organization Status Entity.
 *
 * <p>
 * Represents a configurable status that can be assigned to
 * an organization.
 * </p>
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
@Table(name = "organization_statuses")
public class OrganizationStatus extends BaseEntity {

    /**
     * Organization status code.
     *
     * <p>
     * This property is mapped to the existing database column
     * {@code status_code}.
     * </p>
     */
    @Column(
            name = "status_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String organizationStatusCode;

    /**
     * Organization status name.
     *
     * <p>
     * This property is mapped to the existing database column
     * {@code status_name}.
     * </p>
     */
    @Column(
            name = "status_name",
            nullable = false,
            unique = true,
            length = 100
    )
    private String organizationStatusName;

    /**
     * Organization status description.
     */
    @Column(
            name = "description",
            length = 255
    )
    private String description;

    /**
     * Display order of the organization status.
     */
    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;
}