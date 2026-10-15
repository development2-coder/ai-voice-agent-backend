package com.infinitio.aivoiceplatform.prompt.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.prompt.constant.PromptMessages;
import com.infinitio.aivoiceplatform.prompt.dto.request.CreatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.request.UpdatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.response.PromptResponse;
import com.infinitio.aivoiceplatform.prompt.service.PromptService;
import com.infi.library.annotation.RequiresPermission;
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

    /**
     * Creates a Prompt.
     *
     * @param request create prompt request
     * @return created prompt
     */
    @Operation(summary = "Create Prompt")
    @RequiresPermission("PROMPT_CREATE")
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

    /**
     * Updates a Prompt.
     *
     * @param request update prompt request
     * @return updated prompt
     */
    @Operation(summary = "Update Prompt")
    @RequiresPermission("PROMPT_UPDATE")
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

    /**
     * Gets a Prompt by public ID.
     *
     * @param publicId prompt public ID
     * @return prompt
     */
    @Operation(summary = "Get Prompt By Public Id")
    @RequiresPermission("PROMPT_VIEW")
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

    /**
     * Gets all Prompts.
     *
     * @param page page number
     * @param size page size
     * @return paginated prompts
     */
    @Operation(summary = "Get All Prompts")
    @RequiresPermission("PROMPT_VIEW")
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

    /**
     * Soft deletes a Prompt.
     *
     * @param publicId prompt public ID
     * @return success response
     */
    @Operation(summary = "Delete Prompt")
    @RequiresPermission("PROMPT_DELETE")
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

    /**
     * Activates a Prompt.
     *
     * @param publicId prompt public ID
     * @return success response
     */
    @Operation(summary = "Activate Prompt")
    @RequiresPermission("PROMPT_UPDATE")
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

    /**
     * Deactivates a Prompt.
     *
     * @param publicId prompt public ID
     * @return success response
     */
    @Operation(summary = "Deactivate Prompt")
    @RequiresPermission("PROMPT_UPDATE")
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