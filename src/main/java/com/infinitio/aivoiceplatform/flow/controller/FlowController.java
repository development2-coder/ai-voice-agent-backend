package com.infinitio.aivoiceplatform.flow.controller;

import com.infi.library.annotation.RequiresPermission;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.CreateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.SaveFlowWorkspaceRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowDefinitionResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowEdgeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowTypeResponse;
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


    // =========================================================
    // FLOW CRUD
    // =========================================================

    /**
     * Creates a Flow.
     *
     * @param request create request
     * @return created Flow
     */
    @PostMapping
    @RequiresPermission("FLOW_CREATE")
    public ResponseEntity<FlowResponse> create(
            @Valid @RequestBody
            CreateFlowRequest request) {

        log.info("Create Flow request received.");

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
    @RequiresPermission("FLOW_UPDATE")
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
    @RequiresPermission("FLOW_VIEW")
    public ResponseEntity<FlowResponse> get(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowService.getByPublicId(publicId)
        );
    }


    // =========================================================
    // FLOW NODES
    // =========================================================

    /**
     * Gets all nodes of a Flow.
     *
     * @param publicId Flow public ID
     * @return Flow nodes
     */
    @GetMapping("/{publicId}/nodes")
    @RequiresPermission("FLOW_VIEW")
    public ResponseEntity<List<FlowNodeResponse>> getNodes(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowService.getNodes(publicId)
        );
    }


    /**
     * Adds a node to a Flow.
     *
     * @param request node creation request
     * @return created node
     */
    @PostMapping("/nodes")
    @RequiresPermission("FLOW_UPDATE")
    public ResponseEntity<FlowNodeResponse> addNode(
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
    @RequiresPermission("FLOW_UPDATE")
    public ResponseEntity<FlowNodeResponse> updateNode(
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
    @RequiresPermission("FLOW_DELETE")
    public ResponseEntity<Void> deleteNode(
            @PathVariable String publicId) {

        flowService.deleteNode(publicId);

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // FLOW EDGES
    // =========================================================

    /**
     * Gets all edges of a Flow.
     *
     * @param publicId Flow public ID
     * @return Flow edges
     */
    @GetMapping("/{publicId}/edges")
    @RequiresPermission("FLOW_VIEW")
    public ResponseEntity<List<FlowEdgeResponse>> getEdges(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowEdgeService.getEdges(publicId)
        );
    }


    /**
     * Creates an edge between two Flow nodes.
     *
     * @param request edge request
     * @return created edge
     */
    @PostMapping("/edges")
    @RequiresPermission("FLOW_UPDATE")
    public ResponseEntity<FlowEdgeResponse> addEdge(
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
    @RequiresPermission("FLOW_DELETE")
    public ResponseEntity<Void> deleteEdge(
            @PathVariable String publicId) {

        flowEdgeService.deleteEdge(publicId);

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // FLOW LIFECYCLE
    // =========================================================

    /**
     * Activates a Flow.
     *
     * @param publicId Flow public ID
     * @return empty response
     */
    @PatchMapping("/{publicId}/activate")
    @RequiresPermission("FLOW_UPDATE")
    public ResponseEntity<Void> activate(
            @PathVariable String publicId) {

        flowService.activate(publicId);

        return ResponseEntity.ok().build();
    }


    /**
     * Deactivates a Flow.
     *
     * @param publicId Flow public ID
     * @return empty response
     */
    @PatchMapping("/{publicId}/deactivate")
    @RequiresPermission("FLOW_UPDATE")
    public ResponseEntity<Void> deactivate(
            @PathVariable String publicId) {

        flowService.deactivate(publicId);

        return ResponseEntity.ok().build();
    }


    /**
     * Soft-deletes a Flow.
     *
     * @param publicId Flow public ID
     * @return empty response
     */
    @DeleteMapping("/{publicId}")
    @RequiresPermission("FLOW_DELETE")
    public ResponseEntity<Void> delete(
            @PathVariable String publicId) {

        flowService.delete(publicId);

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // FLOW TYPES
    // =========================================================

    /**
     * Gets supported Flow types.
     *
     * @return Flow types
     */
    @GetMapping("/types")
    @RequiresPermission("FLOW_VIEW")
    public ResponseEntity<ApiResponse<List<FlowTypeResponse>>>
    getFlowTypes() {

        log.info(
                "Request received to retrieve supported flow types"
        );

        List<FlowTypeResponse> flowTypes =
                flowService.getFlowTypes();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Flow types retrieved successfully",
                        flowTypes
                )
        );
    }


    // =========================================================
    // FLOW DEFINITION
    // =========================================================

    /**
     * Gets the complete Flow definition.
     *
     * @param publicId Flow public ID
     * @return complete Flow definition
     */
    @GetMapping("/{publicId}/definition")
    @RequiresPermission("FLOW_VIEW")
    public ResponseEntity<FlowDefinitionResponse> getDefinition(
            @PathVariable String publicId) {

        log.info(
                "Get Flow definition request received. publicId={}",
                publicId
        );

        return ResponseEntity.ok(
                flowService.getDefinition(publicId)
        );
    }


    // =========================================================
    // FLOW WORKSPACE
    // =========================================================

    /**
     * Saves the complete visual Flow Builder workspace.
     *
     * @param publicId Flow public ID
     * @param request workspace request
     * @return updated definition
     */
    @PutMapping("/{publicId}/workspace")
    @RequiresPermission("FLOW_UPDATE")
    public ResponseEntity<FlowDefinitionResponse> saveWorkspace(
            @PathVariable String publicId,
            @Valid @RequestBody
            SaveFlowWorkspaceRequest request) {

        log.info(
                "Save Flow workspace request received. " +
                        "flowPublicId={}",
                publicId
        );

        return ResponseEntity.ok(
                flowService.saveWorkspace(
                        publicId,
                        request
                )
        );
    }


    // =========================================================
    // AGENT FLOW
    // =========================================================

    /**
     * Gets the latest Flow belonging to an Agent.
     *
     * @param agentPublicId Agent public ID
     * @return latest Flow
     */
    @GetMapping("/agent/{agentPublicId}")
    @RequiresPermission("FLOW_VIEW")
    public ResponseEntity<ApiResponse<FlowResponse>>
    getLatestByAgent(
            @PathVariable String agentPublicId) {

        log.info(
                "Fetching latest Flow for agent. agentPublicId={}",
                agentPublicId
        );

        FlowResponse response =
                flowService.getLatestByAgentPublicId(
                        agentPublicId
                );

        return ResponseBuilder.success(
                response,
                "Flow fetched successfully."
        );
    }
}