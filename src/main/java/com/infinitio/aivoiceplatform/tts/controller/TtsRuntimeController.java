package com.infinitio.aivoiceplatform.tts.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.service.TtsRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides runtime APIs for text-to-speech processing.
 *
 * <p>
 * This controller accepts TTS synthesis requests and delegates
 * business processing to the TTS runtime service.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/tts/runtime")
@RequiredArgsConstructor
public class TtsRuntimeController {

    private final TtsRuntimeService ttsRuntimeService;

    /**
     * Synthesizes speech from the supplied text.
     *
     * @param request TTS synthesis request
     * @return TTS synthesis response
     */
    @PostMapping("/synthesize")
    public ResponseEntity<TtsSynthesisResponse> synthesize(
            @RequestBody TtsSynthesisRequest request) {

        log.info(
                "Received TTS synthesis request. callId={}, language={}, speaker={}, textLength={}, finalResponse={}",
                request != null
                        ? request.getCallId()
                        : null,
                request != null
                        ? request.getLanguage()
                        : null,
                request != null
                        ? request.getSpeaker()
                        : null,
                request != null
                        && request.getText() != null
                        ? request.getText().length()
                        : 0,
                request != null
                        && request.isFinalResponse()
        );

        TtsSynthesisResponse response =
                ttsRuntimeService.synthesize(
                        request
                );

        log.info(
                "TTS synthesis request completed. callId={}, provider={}, speaker={}, latencyMs={}",
                response.getCallId(),
                response.getProvider(),
                response.getSpeaker(),
                response.getLatencyMs()
        );

        return ResponseEntity.ok(
                response
        );
    }
}