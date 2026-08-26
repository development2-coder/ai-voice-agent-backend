package com.infinitio.aivoiceplatform.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Entity.
 *
 * Contains common fields shared by all entities.
 *
 * Every entity in the application must extend this class.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 36)
    private String publicId;

    @Column(name = "is_active", nullable = false)
    private Integer isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void prePersist() {

        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }

        if (isActive == null) {
            isActive = 1;
        }

        if (isDeleted == null) {
            isDeleted = 0;
        }

    }

    @PreUpdate
    protected void preUpdate() {

        updatedAt = LocalDateTime.now();

    }

    /**
     * Marks the entity as deleted (soft delete).
     *
     * @param updatedBy User Id performing delete.
     */
    public void markAsDeleted(Long updatedBy) {

        this.isDeleted = 1;
        this.isActive = 0;
        this.deletedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;

    }

    /**
     * Activates the entity.
     *
     * @param updatedBy User Id performing activation.
     */
    public void activate(Long updatedBy) {

        this.isActive = 1;
        this.updatedBy = updatedBy;

    }

    /**
     * Deactivates the entity.
     *
     * @param updatedBy User Id performing deactivation.
     */
    public void deactivate(Long updatedBy) {

        this.isActive = 0;
        this.updatedBy = updatedBy;

    }

}