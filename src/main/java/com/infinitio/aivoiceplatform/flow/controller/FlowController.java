package com.infinitio.aivoiceplatform.flow.controller;

import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.dto.request.*;
import com.infinitio.aivoiceplatform.flow.dto.response.*;
import com.infinitio.aivoiceplatform.flow.service.FlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/flows")
public class FlowController {

    private final FlowService flowService;

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody
            CreateFlowRequest request) {

        log.info(
                "Create flow request received."
        );

        return ResponseEntity.ok(
                flowService.create(request)
        );
    }

    @PutMapping
    public ResponseEntity<?> update(
            @Valid @RequestBody
            UpdateFlowRequest request) {

        log.info(
                "Update flow request received. publicId={}",
                request.getPublicId()
        );

        return ResponseEntity.ok(
                flowService.update(request)
        );
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<FlowResponse> get(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowService.getByPublicId(
                        publicId
                )
        );
    }

    @GetMapping("/{publicId}/nodes")
    public ResponseEntity<List<FlowNodeResponse>>
    getNodes(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowService.getNodes(publicId)
        );
    }

    @GetMapping("/{publicId}/edges")
    public ResponseEntity<List<FlowEdgeResponse>>
    getEdges(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowService.getEdges(publicId)
        );
    }

    @PostMapping("/nodes")
    public ResponseEntity<FlowNodeResponse>
    addNode(
            @Valid @RequestBody
            AddFlowNodeRequest request) {

        return ResponseEntity.ok(
                flowService.addNode(request)
        );
    }

    @PutMapping("/nodes")
    public ResponseEntity<FlowNodeResponse>
    updateNode(
            @Valid @RequestBody
            UpdateFlowNodeRequest request) {

        return ResponseEntity.ok(
                flowService.updateNode(request)
        );
    }

    @DeleteMapping("/nodes/{publicId}")
    public ResponseEntity<Void> deleteNode(
            @PathVariable String publicId) {

        flowService.deleteNode(publicId);

        return ResponseEntity.noContent()
                .build();
    }

    @PostMapping("/edges")
    public ResponseEntity<FlowEdgeResponse>
    addEdge(
            @Valid @RequestBody
            AddFlowEdgeRequest request) {

        return ResponseEntity.ok(
                flowService.addEdge(request)
        );
    }

    @DeleteMapping("/edges/{publicId}")
    public ResponseEntity<Void> deleteEdge(
            @PathVariable String publicId) {

        flowService.deleteEdge(publicId);

        return ResponseEntity.noContent()
                .build();
    }

    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable String publicId) {

        flowService.activate(publicId);

        return ResponseEntity.ok()
                .build();
    }

    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable String publicId) {

        flowService.deactivate(publicId);

        return ResponseEntity.ok()
                .build();
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(
            @PathVariable String publicId) {

        flowService.delete(publicId);

        return ResponseEntity.noContent()
                .build();
    }
}