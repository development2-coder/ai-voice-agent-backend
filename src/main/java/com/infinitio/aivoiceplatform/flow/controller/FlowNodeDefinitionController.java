package com.infinitio.aivoiceplatform.flow.controller;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeDefinitionResponse;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes the Flow Builder node library.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/flows/node-types")
public class FlowNodeDefinitionController {

    private final FlowNodeDefinitionService
            flowNodeDefinitionService;

    /**
     * Gets all available node definitions.
     *
     * @return node definitions
     */
    @GetMapping
    public ResponseEntity<List<FlowNodeDefinitionResponse>>
    getAll() {

        log.info(
                "Fetching Flow node library."
        );

        return ResponseEntity.ok(
                flowNodeDefinitionService.getAll()
        );
    }

    /**
     * Gets one node definition.
     *
     * @param nodeType node type
     * @return node definition
     */
    @GetMapping("/{nodeType}")
    public ResponseEntity<FlowNodeDefinitionResponse>
    getByType(
            @PathVariable FlowNodeType nodeType) {

        log.info(
                "Fetching Flow node definition. nodeType={}",
                nodeType
        );

        return ResponseEntity.ok(
                flowNodeDefinitionService.getByType(
                        nodeType
                )
        );
    }
}