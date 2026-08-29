package com.infinitio.aivoiceplatform.flow.controller;

import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.CreateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowEdgeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowResponse;
import com.infinitio.aivoiceplatform.flow.service.FlowEdgeService;
import com.infinitio.aivoiceplatform.flow.service.FlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Flow management and Flow Builder operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/flows")
public class FlowController {

    private final FlowService flowService;

    private final FlowEdgeService flowEdgeService;

    /**
     * Creates a Flow.
     *
     * @param request create request
     * @return created Flow
     */
    @PostMapping
    public ResponseEntity<FlowResponse> create(
            @Valid @RequestBody
            CreateFlowRequest request) {

        log.info(
                "Create Flow request received."
        );

        return ResponseEntity.ok(
                flowService.create(request)
        );
    }

    /**
     * Updates a Flow.
     *
     * @param request update request
     * @return updated Flow
     */
    @PutMapping
    public ResponseEntity<FlowResponse> update(
            @Valid @RequestBody
            UpdateFlowRequest request) {

        log.info(
                "Update Flow request received. publicId={}",
                request.getPublicId()
        );

        return ResponseEntity.ok(
                flowService.update(request)
        );
    }

    /**
     * Gets a Flow.
     *
     * @param publicId Flow public ID
     * @return Flow
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<FlowResponse> get(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowService.getByPublicId(
                        publicId
                )
        );
    }

    /**
     * Gets all nodes of a Flow.
     *
     * @param publicId Flow public ID
     * @return Flow nodes
     */
    @GetMapping("/{publicId}/nodes")
    public ResponseEntity<List<FlowNodeResponse>>
    getNodes(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowService.getNodes(publicId)
        );
    }

    /**
     * Gets all edges of a Flow.
     *
     * @param publicId Flow public ID
     * @return Flow edges
     */
    @GetMapping("/{publicId}/edges")
    public ResponseEntity<List<FlowEdgeResponse>>
    getEdges(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowEdgeService.getEdges(publicId)
        );
    }

    /**
     * Adds a node to a Flow.
     *
     * @param request node creation request
     * @return created node
     */
    @PostMapping("/nodes")
    public ResponseEntity<FlowNodeResponse>
    addNode(
            @Valid @RequestBody
            AddFlowNodeRequest request) {

        return ResponseEntity.ok(
                flowService.addNode(request)
        );
    }

    /**
     * Updates a Flow node.
     *
     * @param request node update request
     * @return updated node
     */
    @PutMapping("/nodes")
    public ResponseEntity<FlowNodeResponse>
    updateNode(
            @Valid @RequestBody
            UpdateFlowNodeRequest request) {

        return ResponseEntity.ok(
                flowService.updateNode(request)
        );
    }

    /**
     * Deletes a Flow node.
     *
     * @param publicId node public ID
     * @return empty response
     */
    @DeleteMapping("/nodes/{publicId}")
    public ResponseEntity<Void> deleteNode(
            @PathVariable String publicId) {

        flowService.deleteNode(
                publicId
        );

        return ResponseEntity.noContent()
                .build();
    }

    /**
     * Creates an edge between two Flow nodes.
     *
     * @param request edge request
     * @return created edge
     */
    @PostMapping("/edges")
    public ResponseEntity<FlowEdgeResponse>
    addEdge(
            @Valid @RequestBody
            AddFlowEdgeRequest request) {

        return ResponseEntity.ok(
                flowEdgeService.addEdge(request)
        );
    }

    /**
     * Deletes a Flow edge.
     *
     * @param publicId edge public ID
     * @return empty response
     */
    @DeleteMapping("/edges/{publicId}")
    public ResponseEntity<Void> deleteEdge(
            @PathVariable String publicId) {

        flowEdgeService.deleteEdge(
                publicId
        );

        return ResponseEntity.noContent()
                .build();
    }

    /**
     * Activates a Flow.
     *
     * @param publicId Flow public ID
     * @return empty response
     */
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable String publicId) {

        flowService.activate(publicId);

        return ResponseEntity.ok()
                .build();
    }

    /**
     * Deactivates a Flow.
     *
     * @param publicId Flow public ID
     * @return empty response
     */
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable String publicId) {

        flowService.deactivate(publicId);

        return ResponseEntity.ok()
                .build();
    }

    /**
     * Soft-deletes a Flow.
     *
     * @param publicId Flow public ID
     * @return empty response
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(
            @PathVariable String publicId) {

        flowService.delete(publicId);

        return ResponseEntity.noContent()
                .build();
    }
}