package com.infinitio.aivoiceplatform.knowledgebasedocument.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.knowledgebasedocument.constant.KnowledgeBaseDocumentMessages;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request.CreateKnowledgeBaseDocumentRequest;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request.UpdateKnowledgeBaseDocumentRequest;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.response.KnowledgeBaseDocumentResponse;
import com.infinitio.aivoiceplatform.knowledgebasedocument.service.KnowledgeBaseDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Knowledge Base Document.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/knowledge-base-documents")
@Tag(
        name = "Knowledge Base Document",
        description = "Knowledge Base Document Management APIs"
)
public class KnowledgeBaseDocumentController {

    private final KnowledgeBaseDocumentService
            knowledgeBaseDocumentService;

    @Operation(summary = "Create Knowledge Base Document")
    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeBaseDocumentResponse>> create(
            @Valid @RequestBody
            CreateKnowledgeBaseDocumentRequest request) {

        log.info(
                "REST Request : Create Knowledge Base Document"
        );

        KnowledgeBaseDocumentResponse response =
                knowledgeBaseDocumentService.create(request);

        return ResponseBuilder.created(
                response,
                KnowledgeBaseDocumentMessages.CREATED
        );
    }

    @Operation(summary = "Update Knowledge Base Document")
    @PutMapping
    public ResponseEntity<ApiResponse<KnowledgeBaseDocumentResponse>> update(
            @Valid @RequestBody
            UpdateKnowledgeBaseDocumentRequest request) {

        log.info(
                "REST Request : Update Knowledge Base Document"
        );

        KnowledgeBaseDocumentResponse response =
                knowledgeBaseDocumentService.update(request);

        return ResponseBuilder.success(
                response,
                KnowledgeBaseDocumentMessages.UPDATED
        );
    }

    @Operation(summary = "Get Knowledge Base Document")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<KnowledgeBaseDocumentResponse>>
    getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Knowledge Base Document : {}",
                publicId
        );

        KnowledgeBaseDocumentResponse response =
                knowledgeBaseDocumentService
                        .getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Knowledge base document fetched successfully."
        );
    }

    @Operation(summary = "Get All Knowledge Base Documents")
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<KnowledgeBaseDocumentResponse>>>
    getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All Knowledge Base Documents. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<KnowledgeBaseDocumentResponse> response =
                knowledgeBaseDocumentService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Knowledge base documents fetched successfully."
        );
    }

    @Operation(summary = "Delete Knowledge Base Document")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Knowledge Base Document : {}",
                publicId
        );

        knowledgeBaseDocumentService.delete(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseDocumentMessages.DELETED
        );
    }

    @Operation(summary = "Activate Knowledge Base Document")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Knowledge Base Document : {}",
                publicId
        );

        knowledgeBaseDocumentService.activate(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseDocumentMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Knowledge Base Document")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Knowledge Base Document : {}",
                publicId
        );

        knowledgeBaseDocumentService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseDocumentMessages.DEACTIVATED
        );
    }
}