package com.infinitio.aivoiceplatform.stt.provider;

import java.net.http.WebSocket;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.stt.constant.SarvamStreamingConstants;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Maintains one realtime Sarvam STT WebSocket session.
 *
 * <p>
 * One instance is created for each active telephone call.
 * Audio chunks are forwarded to Sarvam and transcription
 * events are delivered through the streaming listener.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
public class SarvamStreamingSession
        implements SttStreamingSession,
        WebSocket.Listener {

    /**
     * Application call identifier.
     */
    private final String callId;

    /**
     * Conversation language.
     */
    private final String language;

    /**
     * Audio sample rate.
     */
    private final Integer sampleRate;

    /**
     * Streaming listener.
     */
    private final SttStreamingListener listener;

    /**
     * JSON mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Incoming fragmented provider message buffer.
     */
    private final StringBuilder messageBuffer =
            new StringBuilder();

    /**
     * Provider WebSocket.
     */
    private volatile WebSocket webSocket;

    /**
     * Indicates that the WebSocket connection is open.
     */
    private volatile boolean open;

    /**
     * Indicates that the application is intentionally closing
     * the session.
     */
    private volatile boolean closing;

    /**
     * Indicates that the provider connection failed.
     */
    private volatile boolean connectionFailed;

    /**
     * Indicates that Sarvam has sent session.begin.
     *
     * <p>
     * This is deliberately different from the WebSocket
     * handshake state. Audio must not be sent before the
     * Sarvam realtime session is initialized.
     * </p>
     */
    private volatile boolean providerSessionReady;

    /**
     * Prevents duplicate error callbacks when both WebSocket
     * error and close callbacks are received.
     */
    private volatile boolean failureNotified;

    /**
     * Runtime callback invoked when the provider session is ready.
     */
    private volatile Runnable readyListener;

    /**
     * Creates a Sarvam streaming session.
     *
     * @param callId application call identifier
     * @param language conversation language
     * @param sampleRate audio sample rate
     * @param listener streaming listener
     * @param objectMapper JSON mapper
     */
    public SarvamStreamingSession(
            String callId,
            String language,
            Integer sampleRate,
            SttStreamingListener listener,
            ObjectMapper objectMapper) {

        this.callId =
                Objects.requireNonNull(
                        callId,
                        SttMessages.CALL_ID_REQUIRED
                );

        this.language =
                Objects.requireNonNull(
                        language,
                        SttMessages.LANGUAGE_REQUIRED
                );

        this.sampleRate =
                Objects.requireNonNull(
                        sampleRate,
                        SttMessages.STREAMING_SAMPLE_RATE_REQUIRED
                );

        this.listener =
                Objects.requireNonNull(
                        listener,
                        SttMessages.STREAMING_LISTENER_REQUIRED
                );

        this.objectMapper =
                Objects.requireNonNull(
                        objectMapper,
                        "ObjectMapper is required."
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCallId() {

        return callId;
    }

    /**
     * Handles successful WebSocket handshake.
     *
     * <p>
     * IMPORTANT:
     * The WebSocket handshake does not mean that the Sarvam
     * realtime STT session is ready. The runtime waits for the
     * provider's session.begin event before forwarding audio.
     * </p>
     *
     * @param webSocket provider WebSocket
     */
    @Override
    public void onOpen(
            WebSocket webSocket) {

        this.webSocket =
                webSocket;

        this.open =
                true;

        this.closing =
                false;

        this.connectionFailed =
                false;

        this.providerSessionReady =
                false;

        this.failureNotified =
                false;

        log.info(
                "Sarvam realtime STT WebSocket connected. " +
                        "callId={}, language={}, sampleRate={}",
                callId,
                language,
                sampleRate
        );

        /*
         * Request the next provider frame.
         */
        webSocket.request(1);

        /*
         * DO NOT call onReady() here.
         *
         * Sarvam still has to send:
         *
         *     session.begin
         *
         * Only after that event do we flush buffered audio.
         */
    }

    /**
     * Handles incoming provider text messages.
     *
     * @param webSocket provider WebSocket
     * @param data received message fragment
     * @param last indicates final fragment
     * @return completion stage
     */
    @Override
    public CompletionStage<?> onText(
            WebSocket webSocket,
            CharSequence data,
            boolean last) {

        if (data == null) {

            webSocket.request(1);

            return null;
        }

        synchronized (messageBuffer) {

            messageBuffer.append(
                    data
            );

            if (last) {

                String message =
                        messageBuffer.toString();

                messageBuffer.setLength(0);

                handleProviderMessage(
                        message
                );
            }
        }

        webSocket.request(1);

        return null;
    }

    /**
     * Marks the provider connection as failed.
     *
     * @param error provider connection error
     */
    public void markConnectionFailure(
            Throwable error) {

        this.connectionFailed =
                true;

        this.open =
                false;

        this.providerSessionReady =
                false;

        log.error(
                "Sarvam realtime STT connection failed. " +
                        "callId={}",
                callId,
                error
        );

        notifyFailure(
                error
        );
    }

    /**
     * Handles provider WebSocket closure.
     *
     * @param webSocket provider WebSocket
     * @param statusCode close status code
     * @param reason close reason
     * @return completion stage
     */
    @Override
    public CompletionStage<?> onClose(
            WebSocket webSocket,
            int statusCode,
            String reason) {

        open =
                false;

        providerSessionReady =
                false;

        if (closing) {
            log.info(
                    "Sarvam realtime STT WebSocket closed normally. " +
                            "callId={}, statusCode={}, reason={}",
                    callId,
                    statusCode,
                    reason
            );
        } else {
            log.error(
                    "Sarvam realtime STT WebSocket CLOSED unexpectedly. " +
                            "callId={}, statusCode={}, reason={}",
                    callId,
                    statusCode,
                    reason
            );
        }

        /*
         * Do not treat an application-requested close as a failure.
         */
        if (!closing
                && !connectionFailed) {

            connectionFailed =
                    true;

            notifyFailure(
                    new IllegalStateException(
                            "Sarvam STT WebSocket closed. " +
                                    "statusCode=" +
                                    statusCode +
                                    ", reason=" +
                                    reason
                    )
            );
        }

        return null;
    }

    /**
     * Handles provider WebSocket errors.
     *
     * @param webSocket provider WebSocket
     * @param error WebSocket error
     */
    @Override
    public void onError(
            WebSocket webSocket,
            Throwable error) {

        open =
                false;

        providerSessionReady =
                false;

        connectionFailed =
                true;

        log.error(
                "Sarvam realtime STT WebSocket ERROR. " +
                        "callId={}",
                callId,
                error
        );

        notifyFailure(
                error
        );
    }

    /**
     * Sends an audio chunk to Sarvam.
     *
     * @param audio audio bytes
     */
    @Override
    public synchronized void sendAudio(
            byte[] audio) {

        if (audio == null
                || audio.length == 0) {

            return;
        }

        if (connectionFailed) {

            throw new IllegalStateException(
                    SttMessages.SARVAM_TRANSCRIPTION_FAILED
            );
        }

        WebSocket currentSocket =
                webSocket;

        /*
         * isOpen() now means both:
         *
         * 1. WebSocket is connected
         * 2. Sarvam session.begin was received
         */
        if (!isOpen()
                || currentSocket == null) {

            throw new IllegalStateException(
                    SttMessages.STREAMING_SESSION_NOT_OPEN
            );
        }

        String encodedAudio =
                Base64.getEncoder()
                        .encodeToString(audio);

        String payload =
                buildAudioPayload(
                        encodedAudio
                );

        try {

            currentSocket
                    .sendText(
                            payload,
                            true
                    )
                    .join();

            log.debug(
                    "Audio sent to Sarvam realtime STT. " +
                            "callId={}, audioSizeBytes={}",
                    callId,
                    audio.length
            );

        } catch (Exception exception) {

            open =
                    false;

            providerSessionReady =
                    false;

            connectionFailed =
                    true;

            log.error(
                    "Unable to send audio to Sarvam realtime STT. " +
                            "callId={}, audioSizeBytes={}",
                    callId,
                    audio.length,
                    exception
            );

            notifyFailure(
                    exception
            );

            throw new IllegalStateException(
                    SttMessages.SARVAM_TRANSCRIPTION_FAILED,
                    exception
            );
        }
    }

    /**
     * Signals a turn boundary.
     *
     * <p>
     * Sarvam VAD handles turn boundaries when endpointing is
     * configured as VAD.
     * </p>
     */
    @Override
    public void finishTurn() {

        if (!isOpen()) {

            log.debug(
                    "Ignoring STT turn boundary because session " +
                            "is not ready. callId={}",
                    callId
            );

            return;
        }

        log.debug(
                "STT streaming turn boundary requested. " +
                        "Sarvam VAD handles endpoint detection. " +
                        "callId={}",
                callId
        );
    }

    /**
     * Closes the provider WebSocket.
     */
    @Override
    public synchronized void close() {

        if (closing) {

            return;
        }

        closing =
                true;

        open =
                false;

        providerSessionReady =
                false;

        WebSocket currentSocket =
                webSocket;

        if (currentSocket == null) {

            log.debug(
                    "Sarvam STT WebSocket already unavailable. " +
                            "callId={}",
                    callId
            );

            return;
        }

        try {

            currentSocket.sendText(
                    buildEndPayload(),
                    true
            ).join();

            log.info(
                    "Sarvam realtime STT end event sent. " +
                            "Waiting for final transcript events. " +
                            "callId={}",
                    callId
            );

            CompletableFuture
                    .delayedExecutor(
                            1500,
                            TimeUnit.MILLISECONDS
                    )
                    .execute(
                            () -> closeProviderSocket(
                                    currentSocket
                            )
                    );

        } catch (Exception exception) {

            log.warn(
                    "Error while sending Sarvam realtime STT " +
                            "end event. callId={}",
                    callId,
                    exception
            );

            closeProviderSocket(
                    currentSocket
            );
        }
    }

    /**
     * Closes the provider WebSocket after the final-transcript
     * grace period.
     *
     * @param currentSocket provider WebSocket
     */
    private void closeProviderSocket(
            WebSocket currentSocket) {

        if (currentSocket == null) {

            return;
        }

        try {

            currentSocket.sendClose(
                    WebSocket.NORMAL_CLOSURE,
                    "Voice call ended"
            ).join();

            log.info(
                    "Sarvam realtime STT WebSocket close requested. " +
                            "callId={}",
                    callId
            );

        } catch (Exception exception) {

            log.warn(
                    "Error while closing Sarvam realtime STT " +
                            "WebSocket. callId={}",
                    callId,
                    exception
            );
        } finally {

            open =
                    false;

            providerSessionReady =
                    false;
        }
    }

    /**
     * Checks whether the provider session is ready for audio.
     *
     * @return true when Sarvam session is fully ready
     */
    @Override
    public boolean isOpen() {

        return open
                && providerSessionReady
                && !closing
                && !connectionFailed
                && webSocket != null;
    }

    /**
     * Builds Sarvam audio input payload.
     *
     * @param encodedAudio Base64 audio
     * @return JSON payload
     */
    private String buildAudioPayload(
            String encodedAudio) {

        try {

            return objectMapper.writeValueAsString(
                    new SarvamAudioInputRequest(
                            SarvamStreamingConstants
                                    .EVENT_AUDIO_INPUT,
                            encodedAudio
                    )
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to build Sarvam STT audio payload. " +
                            "callId={}",
                    callId,
                    exception
            );

            throw new IllegalStateException(
                    SttMessages.SARVAM_TRANSCRIPTION_FAILED,
                    exception
            );
        }
    }

    /**
     * Builds Sarvam end payload.
     *
     * @return JSON end payload
     */
    private String buildEndPayload() {

        try {

            return objectMapper.writeValueAsString(
                    new SarvamAudioInputRequest(
                            SarvamStreamingConstants
                                    .EVENT_END,
                            null
                    )
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to build Sarvam STT end payload. " +
                            "callId={}",
                    callId,
                    exception
            );

            return "{\"event\":\""
                    + SarvamStreamingConstants.EVENT_END
                    + "\"}";
        }
    }

    /**
     * Processes one complete Sarvam provider message.
     *
     * @param message provider JSON message
     */
    private void handleProviderMessage(
            String message) {

        if (message == null
                || message.isBlank()) {

            return;
        }

        try {

            JsonNode root =
                    objectMapper.readTree(
                            message
                    );

            String event =
                    root.path(
                            SarvamStreamingConstants
                                    .FIELD_EVENT
                    ).asText();

            log.debug(
                    "Sarvam realtime STT event received. " +
                            "callId={}, event={}, payload={}",
                    callId,
                    event,
                    message
            );

            switch (event) {

                case SarvamStreamingConstants
                             .EVENT_SESSION_BEGIN -> {

                    providerSessionReady =
                            true;

                    open =
                            true;

                    connectionFailed =
                            false;

                    log.info(
                            "Sarvam realtime STT session initialized " +
                                    "and ready for audio. callId={}",
                            callId
                    );

                    /*
                     * IMPORTANT:
                     * Buffered Exotel audio is flushed only now.
                     */
                    onReady();
                }

                case SarvamStreamingConstants
                             .EVENT_TRANSCRIPT_PARTIAL -> {

                    processPartialTranscript(
                            root
                    );
                }

                case SarvamStreamingConstants
                             .EVENT_TRANSCRIPT_FINAL -> {

                    processFinalTranscript(
                            root
                    );
                }

                case SarvamStreamingConstants
                             .EVENT_VAD_SPEECH_START -> {

                    processSpeechStart();
                }

                case SarvamStreamingConstants
                             .EVENT_VAD_SPEECH_END -> {

                    processSpeechEnd();
                }

                case SarvamStreamingConstants
                             .EVENT_SESSION_END -> {

                    open =
                            false;

                    providerSessionReady =
                            false;

                    log.warn(
                            "Sarvam realtime STT session ended. " +
                                    "callId={}, intentionalClose={}",
                            callId,
                            closing
                    );

                    if (!closing
                            && !connectionFailed) {

                        connectionFailed =
                                true;

                        notifyFailure(
                                new IllegalStateException(
                                        "Sarvam realtime STT session ended."
                                )
                        );
                    }
                }

                case SarvamStreamingConstants
                             .EVENT_ERROR -> {

                    handleProviderError(
                            message
                    );
                }

                default -> {

                    log.debug(
                            "Unhandled Sarvam realtime STT event. " +
                                    "callId={}, event={}",
                            callId,
                            event
                    );
                }
            }

        } catch (Exception exception) {

            log.error(
                    "Unable to process Sarvam realtime STT message. " +
                            "callId={}",
                    callId,
                    exception
            );

            notifyFailure(
                    exception
            );
        }
    }

    /**
     * Processes a partial transcript.
     *
     * @param root provider event
     */
    private void processPartialTranscript(
            JsonNode root) {

        String transcript =
                extractTranscript(
                        root
                );

        if (transcript == null
                || transcript.isBlank()) {

            return;
        }

        log.debug(
                "Sarvam partial transcript. " +
                        "callId={}, transcript={}",
                callId,
                transcript
        );

        listener.onPartialTranscript(
                callId,
                transcript
        );
    }

    /**
     * Processes a final transcript.
     *
     * @param root provider event
     */
    private void processFinalTranscript(
            JsonNode root) {

        String transcript =
                extractTranscript(
                        root
                );

        if (transcript == null
                || transcript.isBlank()) {

            log.debug(
                    "Ignoring empty final transcript. callId={}",
                    callId
            );

            return;
        }

        String transcriptLanguage =
                extractLanguage(
                        root
                );

        log.info(
                "Sarvam final transcript received. " +
                        "callId={}, language={}, transcript={}",
                callId,
                transcriptLanguage,
                transcript
        );

        SttTranscriptionResponse response =
                SttTranscriptionResponse.builder()
                        .callId(
                                callId
                        )
                        .transcript(
                                transcript
                        )
                        .language(
                                transcriptLanguage
                        )
                        .finalTranscript(
                                true
                        )
                        .provider(
                                "SARVAM"
                        )
                        .build();

        listener.onFinalTranscript(
                response
        );
    }

    /**
     * Processes speech-start event.
     */
    private void processSpeechStart() {

        log.debug(
                "Sarvam VAD speech started. callId={}",
                callId
        );

        listener.onSpeechStart(
                callId
        );
    }

    /**
     * Processes speech-end event.
     */
    private void processSpeechEnd() {

        log.debug(
                "Sarvam VAD speech ended. callId={}",
                callId
        );

        listener.onSpeechEnd(
                callId
        );
    }

    /**
     * Handles a provider error event.
     *
     * @param message provider error message
     */
    private void handleProviderError(
            String message) {

        open =
                false;

        providerSessionReady =
                false;

        connectionFailed =
                true;

        log.error(
                "Sarvam realtime STT PROVIDER ERROR. " +
                        "callId={}, message={}",
                callId,
                message
        );

        notifyFailure(
                new IllegalStateException(
                        "Sarvam realtime STT provider error: "
                                + message
                )
        );
    }

    /**
     * Notifies the runtime about a provider failure only once.
     *
     * @param error failure
     */
    private synchronized void notifyFailure(
            Throwable error) {

        if (failureNotified) {

            return;
        }

        failureNotified =
                true;

        try {

            listener.onError(
                    callId,
                    error
            );

        } catch (Exception listenerException) {

            log.error(
                    "STT failure listener threw an exception. " +
                            "callId={}",
                    callId,
                    listenerException
            );
        }
    }

    /**
     * Extracts transcript text.
     *
     * @param root provider JSON
     * @return transcript or null
     */
    private String extractTranscript(
            JsonNode root) {

        JsonNode textNode =
                root.get(
                        SarvamStreamingConstants.FIELD_TEXT
                );

        if (textNode != null
                && !textNode.isNull()) {

            return textNode.asText();
        }

        JsonNode transcriptNode =
                root.get(
                        SarvamStreamingConstants.FIELD_TRANSCRIPT
                );

        if (transcriptNode != null
                && !transcriptNode.isNull()) {

            return transcriptNode.asText();
        }

        return null;
    }

    /**
     * Extracts detected language.
     *
     * @param root provider JSON
     * @return detected language
     */
    private String extractLanguage(
            JsonNode root) {

        JsonNode languageNode =
                root.get(
                        SarvamStreamingConstants.FIELD_LANGUAGE
                );

        if (languageNode != null
                && !languageNode.isNull()
                && !languageNode.asText().isBlank()) {

            return languageNode.asText();
        }

        JsonNode languageCodeNode =
                root.get(
                        SarvamStreamingConstants.FIELD_LANGUAGE_CODE
                );

        if (languageCodeNode != null
                && !languageCodeNode.isNull()
                && !languageCodeNode.asText().isBlank()
                && !"auto".equalsIgnoreCase(
                languageCodeNode.asText().trim()
        )) {

            return languageCodeNode.asText();
        }

        return language;
    }

    /**
     * Notifies runtime that the Sarvam provider session is ready.
     */
    @Override
    public void onReady() {

        if (!providerSessionReady) {

            log.debug(
                    "Ignoring STT ready callback because provider " +
                            "session is not initialized. callId={}",
                    callId
            );

            return;
        }

        log.info(
                "Sarvam realtime STT session is READY for audio. " +
                        "callId={}",
                callId
        );

        Runnable callback =
                readyListener;

        if (callback == null) {

            log.debug(
                    "No STT ready listener registered. callId={}",
                    callId
            );

            return;
        }

        try {

            callback.run();

        } catch (Exception exception) {

            log.error(
                    "Unable to execute STT ready listener. " +
                            "callId={}",
                    callId,
                    exception
            );
        }
    }

    /**
     * Registers the ready callback.
     *
     * @param readyListener callback
     */
    @Override
    public void setReadyListener(
            Runnable readyListener) {

        this.readyListener =
                readyListener;

        log.debug(
                "Sarvam STT ready listener registered. " +
                        "callId={}, providerSocketOpen={}, " +
                        "providerSessionReady={}",
                callId,
                open,
                providerSessionReady
        );

        /*
         * Handle the race where session.begin arrived before the
         * runtime registered the listener.
         */
        if (providerSessionReady
                && readyListener != null) {

            try {

                readyListener.run();

            } catch (Exception exception) {

                log.error(
                        "Unable to execute immediate STT ready listener. " +
                                "callId={}",
                        callId,
                        exception
                );
            }
        }
    }
}