package com.infinitio.aivoiceplatform.llm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;
import com.infinitio.aivoiceplatform.llm.service.LlmRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Exposes runtime LLM generation APIs.
 *
 * <p>
 * The controller is responsible only for HTTP request handling.
 * Runtime business logic is delegated to the LLM runtime service.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/llm/runtime")
public class LlmRuntimeController {

    private final LlmRuntimeService llmRuntimeService;

    /**
     * Generates an LLM response for the supplied conversation.
     *
     * @param request LLM generation request
     * @return generated LLM response
     */
    @PostMapping("/generate")
    public ResponseEntity<LlmGenerationResponseDto> generate(
            @RequestBody LlmGenerationRequestDto request) {

        log.info(
                "Received LLM generation request. callId={}, language={}, messageCount={}, finalResponse={}",
                request == null
                        ? null
                        : request.getCallId(),
                request == null
                        ? null
                        : request.getLanguage(),
                request == null
                        || request.getMessages() == null
                        ? 0
                        : request.getMessages().size(),
                request != null
                        && request.isFinalResponse()
        );

        LlmGenerationResponseDto response =
                llmRuntimeService.generate(request);

        log.info(
                "LLM generation request completed. callId={}, provider={}, model={}, latencyMs={}",
                response.getCallId(),
                response.getProvider(),
                response.getModel(),
                response.getLatencyMs()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}