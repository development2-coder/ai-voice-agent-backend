package com.infinitio.aivoiceplatform.prompt.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.prompt.constant.PromptMessages;
import com.infinitio.aivoiceplatform.prompt.dto.request.CreatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.request.UpdatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.response.PromptResponse;
import com.infinitio.aivoiceplatform.prompt.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Prompt Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/prompts")
@Tag(
        name = "Prompt",
        description = "AI Voice Prompt Management APIs"
)
public class PromptController {

    private final PromptService promptService;

    @Operation(summary = "Create Prompt")
    @PostMapping
    public ResponseEntity<ApiResponse<PromptResponse>> create(
            @Valid @RequestBody CreatePromptRequest request) {

        log.info("REST Request : Create Prompt");

        PromptResponse response =
                promptService.create(request);

        return ResponseBuilder.created(
                response,
                PromptMessages.PROMPT_CREATED
        );
    }

    @Operation(summary = "Update Prompt")
    @PutMapping
    public ResponseEntity<ApiResponse<PromptResponse>> update(
            @Valid @RequestBody UpdatePromptRequest request) {

        log.info("REST Request : Update Prompt");

        PromptResponse response =
                promptService.update(request);

        return ResponseBuilder.success(
                response,
                PromptMessages.PROMPT_UPDATED
        );
    }

    @Operation(summary = "Get Prompt By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<PromptResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Prompt : {}",
                publicId
        );

        PromptResponse response =
                promptService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Prompt fetched successfully."
        );
    }

    @Operation(summary = "Get All Prompts")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PromptResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All Prompts. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<PromptResponse> response =
                promptService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Prompts fetched successfully."
        );
    }

    @Operation(summary = "Delete Prompt")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Prompt : {}",
                publicId
        );

        promptService.delete(publicId);

        return ResponseBuilder.success(
                null,
                PromptMessages.PROMPT_DELETED
        );
    }

    @Operation(summary = "Activate Prompt")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Prompt : {}",
                publicId
        );

        promptService.activate(publicId);

        return ResponseBuilder.success(
                null,
                PromptMessages.PROMPT_ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Prompt")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Prompt : {}",
                publicId
        );

        promptService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                PromptMessages.PROMPT_DEACTIVATED
        );
    }
}