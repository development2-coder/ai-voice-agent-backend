package com.infinitio.aivoiceplatform.knowledgebasedocument.service.impl;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Knowledge Base Document.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeBaseDocumentServiceImpl
        implements KnowledgeBaseDocumentService {

    private final KnowledgeBaseDocumentRepository
            knowledgeBaseDocumentRepository;

    private final KnowledgeBaseDocumentMapper
            knowledgeBaseDocumentMapper;

    private final KnowledgeBaseDocumentValidator
            knowledgeBaseDocumentValidator;

    private final KnowledgeBaseValidator knowledgeBaseValidator;

    @Override
    public KnowledgeBaseDocumentResponse create(
            CreateKnowledgeBaseDocumentRequest request) {

        log.info(
                "Creating Knowledge Base Document. Code : {}",
                request.getDocumentCode()
        );

        knowledgeBaseDocumentValidator
                .validateForCreate(request);

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        request.getKnowledgeBasePublicId()
                );

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentMapper.toEntity(request);

        document.setKnowledgeBase(knowledgeBase);

        KnowledgeBaseDocument savedDocument =
                knowledgeBaseDocumentRepository.save(document);

        log.info(
                "Knowledge Base Document created successfully. Public Id : {}",
                savedDocument.getPublicId()
        );

        return knowledgeBaseDocumentMapper
                .toResponse(savedDocument);
    }

    @Override
    public KnowledgeBaseDocumentResponse update(
            UpdateKnowledgeBaseDocumentRequest request) {

        log.info(
                "Updating Knowledge Base Document. Public Id : {}",
                request.getPublicId()
        );

        knowledgeBaseDocumentValidator
                .validateForUpdate(request);

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator
                        .validateAndGet(
                                request.getPublicId()
                        );

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        request.getKnowledgeBasePublicId()
                );

        knowledgeBaseDocumentMapper.updateEntity(
                request,
                document
        );

        document.setKnowledgeBase(knowledgeBase);

        KnowledgeBaseDocument updatedDocument =
                knowledgeBaseDocumentRepository.save(
                        document
                );

        log.info(
                "Knowledge Base Document updated successfully. Public Id : {}",
                updatedDocument.getPublicId()
        );

        return knowledgeBaseDocumentMapper
                .toResponse(updatedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeBaseDocumentResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Knowledge Base Document. Public Id : {}",
                publicId
        );

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator
                        .validateAndGet(publicId);

        return knowledgeBaseDocumentMapper
                .toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KnowledgeBaseDocumentResponse> getAll() {

        log.info(
                "Fetching all Knowledge Base Documents"
        );

        List<KnowledgeBaseDocument> documents =
                knowledgeBaseDocumentRepository.findAll();

        List<KnowledgeBaseDocumentResponse> content =
                documents.stream()
                        .map(
                                knowledgeBaseDocumentMapper::toResponse
                        )
                        .toList();

        int totalElements =
                content.size();

        return PageResponse
                .<KnowledgeBaseDocumentResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(totalElements)
                .totalPages(
                        totalElements == 0
                                ? 0
                                : 1
                )
                .totalElements(totalElements)
                .first(true)
                .last(true)
                .build();
    }

    @Override
    public void delete(String publicId) {

        log.info(
                "Deleting Knowledge Base Document. Public Id : {}",
                publicId
        );

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator
                        .validateAndGet(publicId);

        document.markAsDeleted(1L);

        knowledgeBaseDocumentRepository.save(document);

        log.info(
                "Knowledge Base Document deleted successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating Knowledge Base Document. Public Id : {}",
                publicId
        );

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator
                        .validateAndGet(publicId);

        document.activate(1L);

        knowledgeBaseDocumentRepository.save(document);

        log.info(
                "Knowledge Base Document activated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating Knowledge Base Document. Public Id : {}",
                publicId
        );

        KnowledgeBaseDocument document =
                knowledgeBaseDocumentValidator
                        .validateAndGet(publicId);

        document.deactivate(1L);

        knowledgeBaseDocumentRepository.save(document);

        log.info(
                "Knowledge Base Document deactivated successfully. Public Id : {}",
                publicId
        );
    }
}