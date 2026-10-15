package com.infinitio.aivoiceplatform.knowledgebasedocument.controller;

import com.infi.library.annotation.RequiresPermission;
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
    @RequiresPermission("KNOWLEDGE_BASE_DOCUMENT_CREATE")
    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeBaseDocumentResponse>> create(
            @Valid @RequestBody
            CreateKnowledgeBaseDocumentRequest request) {

        KnowledgeBaseDocumentResponse response =
                knowledgeBaseDocumentService.create(request);

        return ResponseBuilder.created(
                response,
                KnowledgeBaseDocumentMessages.CREATED
        );
    }

    @Operation(summary = "Update Knowledge Base Document")
    @RequiresPermission("KNOWLEDGE_BASE_DOCUMENT_UPDATE")
    @PutMapping
    public ResponseEntity<ApiResponse<KnowledgeBaseDocumentResponse>> update(
            @Valid @RequestBody
            UpdateKnowledgeBaseDocumentRequest request) {

        KnowledgeBaseDocumentResponse response =
                knowledgeBaseDocumentService.update(request);

        return ResponseBuilder.success(
                response,
                KnowledgeBaseDocumentMessages.UPDATED
        );
    }

    @Operation(summary = "Get Knowledge Base Document")
    @RequiresPermission("KNOWLEDGE_BASE_DOCUMENT_VIEW")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<KnowledgeBaseDocumentResponse>>
    getByPublicId(@PathVariable String publicId) {

        KnowledgeBaseDocumentResponse response =
                knowledgeBaseDocumentService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Knowledge base document fetched successfully."
        );
    }

    @Operation(summary = "Get All Knowledge Base Documents")
    @RequiresPermission("KNOWLEDGE_BASE_DOCUMENT_VIEW")
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<KnowledgeBaseDocumentResponse>>>
    getAll() {

        PageResponse<KnowledgeBaseDocumentResponse> response =
                knowledgeBaseDocumentService.getAll();

        return ResponseBuilder.success(
                response,
                "Knowledge base documents fetched successfully."
        );
    }

    @Operation(summary = "Delete Knowledge Base Document")
    @RequiresPermission("KNOWLEDGE_BASE_DOCUMENT_DELETE")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        knowledgeBaseDocumentService.delete(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseDocumentMessages.DELETED
        );
    }

    @Operation(summary = "Activate Knowledge Base Document")
    @RequiresPermission("KNOWLEDGE_BASE_DOCUMENT_UPDATE")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        knowledgeBaseDocumentService.activate(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseDocumentMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Knowledge Base Document")
    @RequiresPermission("KNOWLEDGE_BASE_DOCUMENT_UPDATE")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        knowledgeBaseDocumentService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseDocumentMessages.DEACTIVATED
        );
    }
}