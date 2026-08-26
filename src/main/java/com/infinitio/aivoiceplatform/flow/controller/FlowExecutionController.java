package com.infinitio.aivoiceplatform.flow.controller;

import com.infinitio.aivoiceplatform.flow.dto.request.ContinueFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.StartFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionService;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueAiResponseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/flow-executions")
public class FlowExecutionController {

    private final FlowExecutionService
            flowExecutionService;

    @PostMapping("/start")
    public ResponseEntity<FlowExecutionResult>
    start(
            @Valid @RequestBody
            StartFlowExecutionRequest request) {

        log.info(
                "Start flow execution request. flow={}",
                request.getFlowPublicId()
        );

        return ResponseEntity.ok(
                flowExecutionService.start(request)
        );
    }

    @PostMapping("/continue")
    public ResponseEntity<FlowExecutionResult>
    continueExecution(
            @Valid @RequestBody
            ContinueFlowExecutionRequest request) {

        log.info(
                "Continue flow execution request. execution={}",
                request.getExecutionPublicId()
        );

        return ResponseEntity.ok(
                flowExecutionService
                        .continueExecution(request)
        );
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<FlowExecutionResult>
    getExecution(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                flowExecutionService
                        .getExecution(publicId)
        );
    }

    @PatchMapping("/{publicId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable String publicId) {

        flowExecutionService.cancel(
                publicId
        );

        return ResponseEntity.ok()
                .build();
    }

    @PostMapping("/ai-response")
    public ResponseEntity<FlowExecutionResult> continueWithAiResponse(
            @Valid @RequestBody ContinueAiResponseRequest request) {

        FlowExecutionResult result =
                flowExecutionService.continueWithAiResponse(
                        request
                );

        return ResponseEntity.ok(result);
    }
}