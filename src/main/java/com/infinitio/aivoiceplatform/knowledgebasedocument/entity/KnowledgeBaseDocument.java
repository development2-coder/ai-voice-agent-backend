package com.infinitio.aivoiceplatform.knowledgebasedocument.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.knowledgebase.entity.KnowledgeBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Knowledge Base Document Entity.
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
        name = "knowledge_base_documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_kb_document_code",
                        columnNames = "document_code"
                )
        }
)
public class KnowledgeBaseDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "knowledge_base_id",
            nullable = false
    )
    private KnowledgeBase knowledgeBase;

    @Column(
            name = "document_code",
            nullable = false,
            length = 50
    )
    private String documentCode;

    @Column(
            name = "document_name",
            nullable = false,
            length = 255
    )
    private String documentName;

    @Column(
            name = "document_type",
            length = 50
    )
    private String documentType;

    @Column(
            name = "file_name",
            length = 255
    )
    private String fileName;

    @Column(
            name = "file_url",
            length = 1000
    )
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(
            name = "mime_type",
            length = 100
    )
    private String mimeType;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(
            name = "processing_status",
            length = 50
    )
    private String processingStatus;

    @PrePersist
    public void initializeDefaults() {

        if (processingStatus == null ||
                processingStatus.isBlank()) {

            processingStatus = "PENDING";
        }
    }
}