package com.infinitio.aivoiceplatform.knowledgebasedocument.service.impl;

import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.knowledgebase.entity.KnowledgeBase;
import com.infinitio.aivoiceplatform.knowledgebase.validator.KnowledgeBaseValidator;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request.CreateKnowledgeBaseDocumentRequest;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request.UpdateKnowledgeBaseDocumentRequest;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.response.KnowledgeBaseDocumentResponse;
import com.infinitio.aivoiceplatform.knowledgebasedocument.entity.KnowledgeBaseDocument;
import com.infinitio.aivoiceplatform.knowledgebasedocument.mapper.KnowledgeBaseDocumentMapper;
import com.infinitio.aivoiceplatform.knowledgebasedocument.repository.KnowledgeBaseDocumentRepository;
import com.infinitio.aivoiceplatform.knowledgebasedocument.service.KnowledgeBaseDocumentService;
import com.infinitio.aivoiceplatform.knowledgebasedocument.validator.KnowledgeBaseDocumentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Knowledge Base Documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeBaseDocumentServiceImpl
        implements KnowledgeBaseDocumentService {

    private static final Integer NOT_DELETED = 0;

    private final KnowledgeBaseDocumentRepository
            knowledgeBaseDocumentRepository;

    private final KnowledgeBaseDocumentMapper
            knowledgeBaseDocumentMapper;

    private final KnowledgeBaseDocumentValidator
            knowledgeBaseDocumentValidator;

    private final KnowledgeBaseValidator knowledgeBaseValidator;

    private final TenantAccessService tenantAccessService;

    @Override
    public KnowledgeBaseDocumentResponse create(
            CreateKnowledgeBaseDocumentRequest request) {

        log.info(
                "Creating Knowledge Base Document. Code: {}",
                request.getDocumentCode()
        );

        knowledgeBaseDocumentValidator.validateForCreate(request);

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        request.getKnowledgeBasePublicId()
                );

        // Validate ownership through KnowledgeBase -> Agent -> Tenant.
        tenantAccessService.validateKnowledgeBaseAccess(knowledgeBase);

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentMapper.toEntity(request);

        document.setKnowledgeBase(knowledgeBase);
        document.setCreatedBy(tenantAccessService.getCurrentUserId());

        KnowledgeBaseDocument savedDocument =
                knowledgeBaseDocumentRepository.save(document);

        log.info(
                "Knowledge Base Document created. Public ID: {}",
                savedDocument.getPublicId()
        );

        return knowledgeBaseDocumentMapper.toResponse(savedDocument);
    }

    @Override
    public KnowledgeBaseDocumentResponse update(
            UpdateKnowledgeBaseDocumentRequest request) {

        knowledgeBaseDocumentValidator.validateForUpdate(request);

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator.validateAndGet(
                        request.getPublicId()
                );

        // Verify access to the current parent before modifying the document.
        tenantAccessService.validateKnowledgeBaseAccess(
                document.getKnowledgeBase()
        );

        KnowledgeBase newKnowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        request.getKnowledgeBasePublicId()
                );

        // Prevent moving a document into another tenant's Knowledge Base.
        tenantAccessService.validateKnowledgeBaseAccess(newKnowledgeBase);

        knowledgeBaseDocumentMapper.updateEntity(request, document);
        document.setKnowledgeBase(newKnowledgeBase);
        document.setUpdatedBy(tenantAccessService.getCurrentUserId());

        KnowledgeBaseDocument updatedDocument =
                knowledgeBaseDocumentRepository.save(document);

        log.info(
                "Knowledge Base Document updated. Public ID: {}",
                updatedDocument.getPublicId()
        );

        return knowledgeBaseDocumentMapper.toResponse(updatedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeBaseDocumentResponse getByPublicId(String publicId) {

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator.validateAndGet(publicId);

        tenantAccessService.validateKnowledgeBaseAccess(
                document.getKnowledgeBase()
        );

        return knowledgeBaseDocumentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KnowledgeBaseDocumentResponse> getAll() {

        List<KnowledgeBaseDocument> documents;

        if (tenantAccessService.isSuperAdmin()) {
            documents =
                    knowledgeBaseDocumentRepository.findByIsDeleted(
                            NOT_DELETED
                    );
        } else {
            Long tenantId = tenantAccessService.getCurrentTenantId();

            documents = knowledgeBaseDocumentRepository
                    .findByKnowledgeBase_Agent_Tenant_IdAndIsDeleted(
                            tenantId,
                            NOT_DELETED
                    );
        }

        List<KnowledgeBaseDocumentResponse> content =
                documents.stream()
                        .map(knowledgeBaseDocumentMapper::toResponse)
                        .toList();

        int totalElements = content.size();

        return PageResponse
                .<KnowledgeBaseDocumentResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(totalElements)
                .totalPages(totalElements == 0 ? 0 : 1)
                .totalElements(totalElements)
                .first(true)
                .last(true)
                .build();
    }

    @Override
    public void delete(String publicId) {

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator.validateAndGet(publicId);

        tenantAccessService.validateKnowledgeBaseAccess(
                document.getKnowledgeBase()
        );

        document.markAsDeleted(tenantAccessService.getCurrentUserId());
        knowledgeBaseDocumentRepository.save(document);

        log.info("Knowledge Base Document deleted: {}", publicId);
    }

    @Override
    public void activate(String publicId) {

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator.validateAndGet(publicId);

        tenantAccessService.validateKnowledgeBaseAccess(
                document.getKnowledgeBase()
        );

        document.activate(tenantAccessService.getCurrentUserId());
        knowledgeBaseDocumentRepository.save(document);

        log.info("Knowledge Base Document activated: {}", publicId);
    }

    @Override
    public void deactivate(String publicId) {

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator.validateAndGet(publicId);

        tenantAccessService.validateKnowledgeBaseAccess(
                document.getKnowledgeBase()
        );

        document.deactivate(tenantAccessService.getCurrentUserId());
        knowledgeBaseDocumentRepository.save(document);

        log.info("Knowledge Base Document deactivated: {}", publicId);
    }
}