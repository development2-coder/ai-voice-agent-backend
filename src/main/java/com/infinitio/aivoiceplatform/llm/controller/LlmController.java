package com.infinitio.aivoiceplatform.llm.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.llm.constant.LlmMessages;
import com.infinitio.aivoiceplatform.llm.dto.request.CreateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.request.UpdateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.response.LlmResponse;
import com.infinitio.aivoiceplatform.llm.service.LlmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for LLM Configuration.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/llms")
@Tag(
        name = "LLM",
        description = "LLM Configuration APIs"
)
public class LlmController {

    private final LlmService llmService;

    @Operation(summary = "Create LLM")
    @PostMapping
    public ResponseEntity<ApiResponse<LlmResponse>> create(
            @Valid @RequestBody CreateLlmRequest request) {

        log.info("REST Request : Create LLM");

        LlmResponse response =
                llmService.create(request);

        return ResponseBuilder.created(
                response,
                LlmMessages.CREATED
        );
    }

    @Operation(summary = "Update LLM")
    @PutMapping
    public ResponseEntity<ApiResponse<LlmResponse>> update(
            @Valid @RequestBody UpdateLlmRequest request) {

        log.info("REST Request : Update LLM");

        LlmResponse response =
                llmService.update(request);

        return ResponseBuilder.success(
                response,
                LlmMessages.UPDATED
        );
    }

    @Operation(summary = "Get LLM By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<LlmResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get LLM : {}",
                publicId
        );

        LlmResponse response =
                llmService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "LLM configuration fetched successfully."
        );
    }

    @Operation(summary = "Get All LLMs")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LlmResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All LLMs. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<LlmResponse> response =
                llmService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "LLM configurations fetched successfully."
        );
    }

    @Operation(summary = "Delete LLM")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete LLM : {}",
                publicId
        );

        llmService.delete(publicId);

        return ResponseBuilder.success(
                null,
                LlmMessages.DELETED
        );
    }

    @Operation(summary = "Activate LLM")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate LLM : {}",
                publicId
        );

        llmService.activate(publicId);

        return ResponseBuilder.success(
                null,
                LlmMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate LLM")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate LLM : {}",
                publicId
        );

        llmService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                LlmMessages.DEACTIVATED
        );
    }
}