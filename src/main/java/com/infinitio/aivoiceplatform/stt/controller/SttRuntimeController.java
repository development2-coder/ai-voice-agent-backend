package com.infinitio.aivoiceplatform.stt.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.infinitio.aivoiceplatform.exception.InternalServerException;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Exposes runtime speech-to-text APIs.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stt/runtime")
public class SttRuntimeController {

    private final SttRuntimeService sttRuntimeService;

    /**
     * Transcribes the supplied audio using the configured STT provider.
     *
     * @param callId call identifier
     * @param language requested language
     * @param finalTranscript indicates whether the transcript is final
     * @param audio audio file
     * @return STT transcription response
     */
    @PostMapping(
            value = "/transcribe",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<SttTranscriptionResponse> transcribe(
            @RequestParam("callId") String callId,
            @RequestParam("language") String language,
            @RequestParam(
                    value = "finalTranscript",
                    required = false,
                    defaultValue = "false"
            ) Boolean finalTranscript,
            @RequestParam("audio") MultipartFile audio) {

        log.info(
                "Received STT transcription request. callId={}, language={}, finalTranscript={}, fileName={}",
                callId,
                language,
                finalTranscript,
                audio.getOriginalFilename()
        );

        byte[] audioBytes;

        try {

            audioBytes = audio.getBytes();

        } catch (IOException exception) {

            log.error(
                    "Failed to read STT audio file. callId={}, fileName={}",
                    callId,
                    audio.getOriginalFilename(),
                    exception
            );

            throw new InternalServerException(
                    SttMessages.TRANSCRIPTION_FAILED
            );
        }

        SttTranscriptionRequest request =
                SttTranscriptionRequest.builder()
                        .callId(callId)
                        .audio(audioBytes)
                        .contentType(audio.getContentType())
                        .fileName(audio.getOriginalFilename())
                        .language(language)
                        .finalTranscript(
                                Boolean.TRUE.equals(finalTranscript)
                        )
                        .build();

        SttTranscriptionResponse response =
                sttRuntimeService.transcribe(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}