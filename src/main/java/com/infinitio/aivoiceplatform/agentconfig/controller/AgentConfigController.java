package com.infinitio.aivoiceplatform.agentconfig.controller;

import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigMessages;
import com.infinitio.aivoiceplatform.agentconfig.dto.request.CreateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.request.UpdateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.response.AgentConfigResponse;
import com.infinitio.aivoiceplatform.agentconfig.service.AgentConfigService;
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
 * REST Controller for Agent Configuration Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agent-configs")
@Tag(
        name = "Agent Configuration",
        description = "AI Agent Configuration APIs"
)
public class AgentConfigController {


    private final AgentConfigService agentConfigService;


    // =========================================================
    // CREATE
    // =========================================================

    @Operation(
            summary = "Create Agent Configuration"
    )
    @PostMapping
    public ResponseEntity<
            ApiResponse<AgentConfigResponse>>
    create(
            @Valid
            @RequestBody
            CreateAgentConfigRequest request) {

        log.info(
                "REST Request : Create Agent Configuration"
        );

        AgentConfigResponse response =
                agentConfigService.create(
                        request
                );

        return ResponseBuilder.created(
                response,
                AgentConfigMessages.CREATED
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Operation(
            summary = "Update Agent Configuration"
    )
    @PutMapping
    public ResponseEntity<
            ApiResponse<AgentConfigResponse>>
    update(
            @Valid
            @RequestBody
            UpdateAgentConfigRequest request) {

        log.info(
                "REST Request : Update Agent Configuration"
        );

        AgentConfigResponse response =
                agentConfigService.update(
                        request
                );

        return ResponseBuilder.success(
                response,
                AgentConfigMessages.UPDATED
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Operation(
            summary = "Get Agent Configuration"
    )
    @GetMapping("/{publicId}")
    public ResponseEntity<
            ApiResponse<AgentConfigResponse>>
    getByPublicId(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Get Agent Configuration : {}",
                publicId
        );

        AgentConfigResponse response =
                agentConfigService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                AgentConfigMessages.FETCHED
        );
    }


    // =========================================================
    // GET BY AGENT
    // =========================================================

    @Operation(
            summary = "Get Configuration By Agent"
    )
    @GetMapping("/agent/{agentPublicId}")
    public ResponseEntity<
            ApiResponse<AgentConfigResponse>>
    getByAgent(
            @PathVariable
            String agentPublicId) {

        log.info(
                "REST Request : Get Agent Configuration By Agent : {}",
                agentPublicId
        );

        AgentConfigResponse response =
                agentConfigService.getByAgent(
                        agentPublicId
                );

        return ResponseBuilder.success(
                response,
                AgentConfigMessages.FETCHED
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Operation(
            summary = "Get All Agent Configurations"
    )
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<AgentConfigResponse>>>
    getAll(
            @RequestParam(
                    defaultValue = "0"
            )
            Integer page,

            @RequestParam(
                    defaultValue = "10"
            )
            Integer size) {

        log.info(
                "REST Request : Get All Agent Configurations. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<AgentConfigResponse> response =
                agentConfigService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                AgentConfigMessages.FETCHED_ALL
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Operation(
            summary = "Delete Agent Configuration"
    )
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>>
    delete(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Delete Agent Configuration : {}",
                publicId
        );

        agentConfigService.delete(
                publicId
        );

        return ResponseBuilder.success(
                null,
                AgentConfigMessages.DELETED
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Operation(
            summary = "Activate Agent Configuration"
    )
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>>
    activate(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Activate Agent Configuration : {}",
                publicId
        );

        agentConfigService.activate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                AgentConfigMessages.ACTIVATED
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Operation(
            summary = "Deactivate Agent Configuration"
    )
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>>
    deactivate(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Deactivate Agent Configuration : {}",
                publicId
        );

        agentConfigService.deactivate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                AgentConfigMessages.DEACTIVATED
        );
    }
}