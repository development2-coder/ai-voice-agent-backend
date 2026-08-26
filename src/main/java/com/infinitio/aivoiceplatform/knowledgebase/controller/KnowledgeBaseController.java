package com.infinitio.aivoiceplatform.knowledgebase.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.knowledgebase.constant.KnowledgeBaseMessages;
import com.infinitio.aivoiceplatform.knowledgebase.dto.request.CreateKnowledgeBaseRequest;
import com.infinitio.aivoiceplatform.knowledgebase.dto.request.UpdateKnowledgeBaseRequest;
import com.infinitio.aivoiceplatform.knowledgebase.dto.response.KnowledgeBaseResponse;
import com.infinitio.aivoiceplatform.knowledgebase.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Knowledge Base Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/knowledge-bases")
@Tag(
        name = "Knowledge Base",
        description = "Knowledge Base Management APIs"
)
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "Create Knowledge Base")
    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeBaseResponse>> create(
            @Valid @RequestBody CreateKnowledgeBaseRequest request) {

        log.info(
                "REST Request : Create Knowledge Base"
        );

        KnowledgeBaseResponse response =
                knowledgeBaseService.create(request);

        return ResponseBuilder.created(
                response,
                KnowledgeBaseMessages.CREATED
        );
    }

    @Operation(summary = "Update Knowledge Base")
    @PutMapping
    public ResponseEntity<ApiResponse<KnowledgeBaseResponse>> update(
            @Valid @RequestBody UpdateKnowledgeBaseRequest request) {

        log.info(
                "REST Request : Update Knowledge Base"
        );

        KnowledgeBaseResponse response =
                knowledgeBaseService.update(request);

        return ResponseBuilder.success(
                response,
                KnowledgeBaseMessages.UPDATED
        );
    }

    @Operation(summary = "Get Knowledge Base")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<KnowledgeBaseResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Knowledge Base : {}",
                publicId
        );

        KnowledgeBaseResponse response =
                knowledgeBaseService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                "Knowledge base fetched successfully."
        );
    }

    @Operation(summary = "Get All Knowledge Bases")
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<KnowledgeBaseResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All Knowledge Bases. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<KnowledgeBaseResponse> response =
                knowledgeBaseService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Knowledge bases fetched successfully."
        );
    }

    @Operation(summary = "Delete Knowledge Base")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Knowledge Base : {}",
                publicId
        );

        knowledgeBaseService.delete(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseMessages.DELETED
        );
    }

    @Operation(summary = "Activate Knowledge Base")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Knowledge Base : {}",
                publicId
        );

        knowledgeBaseService.activate(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Knowledge Base")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Knowledge Base : {}",
                publicId
        );

        knowledgeBaseService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                KnowledgeBaseMessages.DEACTIVATED
        );
    }
}