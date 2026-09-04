package com.infinitio.aivoiceplatform.agent.controller;

import com.infinitio.aivoiceplatform.agent.constant.AgentMessages;
import com.infinitio.aivoiceplatform.agent.dto.request.CreateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.request.UpdateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentResponse;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentWorkspaceResponse;
import com.infinitio.aivoiceplatform.agent.service.AgentService;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Agent Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agents")
@Tag(
        name = "Agent",
        description = "AI Voice Agent Management APIs"
)
public class AgentController {

    private final AgentService agentService;


    // =========================================================
    // CREATE
    // =========================================================

    @Operation(summary = "Create Agent")
    @PostMapping
    public ResponseEntity<ApiResponse<AgentResponse>> create(
            @Valid @RequestBody CreateAgentRequest request) {

        log.info("REST Request : Create Agent");

        AgentResponse response =
                agentService.create(request);

        return ResponseBuilder.created(
                response,
                AgentMessages.AGENT_CREATED
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Operation(summary = "Update Agent")
    @PutMapping
    public ResponseEntity<ApiResponse<AgentResponse>> update(
            @Valid @RequestBody UpdateAgentRequest request) {

        log.info(
                "REST Request : Update Agent | Public Id : {}",
                request.getPublicId()
        );

        AgentResponse response =
                agentService.update(request);

        return ResponseBuilder.success(
                response,
                AgentMessages.AGENT_UPDATED
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Operation(summary = "Get Agent By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<AgentResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Agent | Public Id : {}",
                publicId
        );

        AgentResponse response =
                agentService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Agent fetched successfully."
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Operation(summary = "Get All Agents")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AgentResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info(
                "REST Request : Get All Agents | Page : {} | Size : {}",
                page,
                size
        );

        PageResponse<AgentResponse> response =
                agentService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Agents fetched successfully."
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Operation(summary = "Delete Agent")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Agent | Public Id : {}",
                publicId
        );

        agentService.delete(publicId);

        return ResponseBuilder.success(
                null,
                AgentMessages.AGENT_DELETED
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Operation(summary = "Activate Agent")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Agent | Public Id : {}",
                publicId
        );

        agentService.activate(publicId);

        return ResponseBuilder.success(
                null,
                AgentMessages.AGENT_ACTIVATED
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Operation(summary = "Deactivate Agent")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Agent | Public Id : {}",
                publicId
        );

        agentService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                AgentMessages.AGENT_DEACTIVATED
        );
    }

    /**
     * Gets the complete Agent workspace required by the
     * visual Flow Builder.
     *
     * @param publicId Agent public identifier
     * @return Agent workspace
     */
    @Operation(
            summary = "Get Agent Workspace"
    )
    @GetMapping("/{publicId}/workspace")
    public ResponseEntity<ApiResponse<AgentWorkspaceResponse>>
    getWorkspace(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Agent Workspace | Public Id : {}",
                publicId
        );

        AgentWorkspaceResponse response =
                agentService.getWorkspace(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                "Agent workspace fetched successfully."
        );
    }
}