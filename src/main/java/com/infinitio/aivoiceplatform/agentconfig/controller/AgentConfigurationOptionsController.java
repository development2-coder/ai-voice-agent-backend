package com.infinitio.aivoiceplatform.agentconfig.controller;

import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigMessages;
import com.infinitio.aivoiceplatform.agentconfig.dto.response.AgentConfigurationOptionsResponse;
import com.infinitio.aivoiceplatform.agentconfig.service.AgentConfigurationOptionsService;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Agent Configuration selection options.
 *
 * <p>
 * Provides controlled language, speaker and greeting options required
 * by the Agent creation and configuration UI.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agent-configs/options")
@Tag(
        name = "Agent Configuration Options",
        description = "Agent Configuration selection option APIs"
)
public class AgentConfigurationOptionsController {

    private final AgentConfigurationOptionsService
            agentConfigurationOptionsService;

    /**
     * Fetches language, speaker and greeting options.
     *
     * @return selectable Agent Configuration options
     */
    @Operation(
            summary = "Get Agent Configuration Options"
    )
    @GetMapping
    public ResponseEntity<
            ApiResponse<AgentConfigurationOptionsResponse>>
    getOptions() {

        log.info(
                "REST Request : Get Agent Configuration options."
        );

        AgentConfigurationOptionsResponse response =
                agentConfigurationOptionsService.getOptions();

        return ResponseBuilder.success(
                response,
                AgentConfigMessages.OPTIONS_FETCHED
        );
    }
}