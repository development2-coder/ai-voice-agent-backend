package com.infinitio.aivoiceplatform.stt.provider;

import java.net.http.WebSocket;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.stt.constant.SarvamStreamingConstants;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;

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
 * <p>
 * Runtime provider configuration is handled by
 * {@link SarvamSttProvider}. This class is responsible only
 * for the lifecycle and communication of an established
 * provider-side WebSocket session.
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
     * Incoming fragmented text message buffer.
     */
    private final StringBuilder messageBuffer =
            new StringBuilder();

    /**
     * Provider WebSocket.
     */
    private volatile WebSocket webSocket;

    /**
     * Indicates whether the session is open.
     */
    private volatile boolean open;

    /**
     * Indicates whether close processing has already started.
     */
    private volatile boolean closing;

    /**
     * Indicates that the provider WebSocket connection failed
     * during initialization.
     */
    private volatile boolean connectionFailed;

    /**
     * Callback invoked when the Sarvam WebSocket becomes ready.
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
     * Handles successful WebSocket connection establishment.
     *
     * @param webSocket provider WebSocket
     */
    /**
     * Handles successful provider WebSocket connection establishment.
     *
     * @param webSocket provider WebSocket
     */
    /**
     * Handles successful provider WebSocket connection establishment.
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

        log.info(
                "Sarvam realtime STT WebSocket connected. " +
                        "callId={}, language={}, sampleRate={}",
                callId,
                language,
                sampleRate
        );

        webSocket.request(1);

        /*
         * Notify the runtime that audio can now be forwarded.
         *
         * The STT runtime uses this callback to flush audio
         * packets that arrived while the Sarvam connection
         * was being established.
         */
        onReady();
    }

    /**
     * Handles incoming provider text messages.
     *
     * @param webSocket provider WebSocket
     * @param data received message fragment
     * @param last indicates whether this is the final fragment
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
     * <p>
     * This method is called by the provider connection future when
     * the WebSocket handshake cannot be established.
     * </p>
     *
     * @param error provider connection error
     */
    public void markConnectionFailure(
            Throwable error) {

        this.connectionFailed =
                true;

        this.open =
                false;

        log.error(
                "Sarvam realtime STT connection failed. callId={}",
                callId,
                error
        );

        listener.onError(
                callId,
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

        closing =
                true;

        log.info(
                "Sarvam realtime STT WebSocket closed. " +
                        "callId={}, statusCode={}, reason={}",
                callId,
                statusCode,
                reason
        );

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

        log.error(
                "Sarvam realtime STT WebSocket error. callId={}",
                callId,
                error
        );

        listener.onError(
                callId,
                error
        );
    }

    /**
     * Sends an audio chunk to Sarvam.
     *
     * <p>
     * The raw audio bytes are Base64 encoded and wrapped
     * inside the Sarvam realtime {@code audio_input} event.
     * </p>
     *
     * @param audio audio bytes
     */
    /**
     * Sends an audio chunk to Sarvam.
     *
     * <p>
     * The provider WebSocket must be open before audio can be
     * transmitted. Runtime-level buffering prevents normal Exotel
     * startup races from reaching this method, but this validation
     * remains as a final provider-level safety check.
     * </p>
     *
     * @param audio audio bytes
     */
    /**
     * Sends an audio chunk to Sarvam realtime STT.
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

            log.error(
                    "Unable to send audio to Sarvam realtime STT. " +
                            "callId={}, audioSizeBytes={}",
                    callId,
                    audio.length,
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
     * When Sarvam VAD endpointing is configured, the provider
     * detects speech boundaries automatically. Therefore no
     * explicit turn-end message is sent here.
     * </p>
     */
    @Override
    public void finishTurn() {

        if (!isOpen()) {

            log.debug(
                    "Ignoring STT turn boundary because session " +
                            "is not open. callId={}",
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
        }
    }

    /**
     * Checks whether the provider WebSocket is open.
     *
     * @return true when the WebSocket is open
     */
    @Override
    public boolean isOpen() {

        return open
                && !closing
                && !connectionFailed
                && webSocket != null;
    }

    /**
     * Builds the Sarvam audio input message.
     *
     * @param encodedAudio Base64 encoded audio
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
     * Builds the provider session end message.
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
     * Processes one complete provider message.
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

                    log.info(
                            "Sarvam realtime STT session initialized. " +
                                    "callId={}",
                            callId
                    );
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

                    log.info(
                            "Sarvam realtime STT session ended. " +
                                    "callId={}",
                            callId
                    );
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

            listener.onError(
                    callId,
                    exception
            );
        }
    }

    /**
     * Processes a partial transcript.
     *
     * <p>
     * Partial transcripts are forwarded immediately to the
     * streaming listener so downstream runtime components can
     * react to realtime transcription updates.
     * </p>
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
    /**
     * Processes a final Sarvam transcript event.
     *
     * <p>
     * The detected language is preserved together with the
     * transcript so that downstream conversation and Flow
     * execution can dynamically switch the response language.
     * </p>
     *
     * @param root provider JSON response
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
     * Processes a Sarvam VAD speech-start event.
     *
     * <p>
     * This event is important for barge-in handling. When the
     * caller starts speaking while TTS audio is being played,
     * the downstream Voice Gateway can interrupt the current
     * TTS playback.
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
     * Processes a Sarvam VAD speech-end event.
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
     * @param message provider message
     */
    private void handleProviderError(
            String message) {

        log.error(
                "Sarvam realtime STT provider error. " +
                        "callId={}, message={}",
                callId,
                message
        );

        listener.onError(
                callId,
                new IllegalStateException(
                        message
                )
        );
    }

    /**
     * Extracts transcript text from a provider event.
     *
     * @param root provider JSON
     * @return transcript or null
     */
    private String extractTranscript(
            JsonNode root) {

        JsonNode textNode =
                root.get(
                        SarvamStreamingConstants
                                .FIELD_TEXT
                );

        if (textNode != null
                && !textNode.isNull()) {

            return textNode.asText();
        }

        JsonNode transcriptNode =
                root.get(
                        SarvamStreamingConstants
                                .FIELD_TRANSCRIPT
                );

        if (transcriptNode != null
                && !transcriptNode.isNull()) {

            return transcriptNode.asText();
        }

        return null;
    }

    /**
     * Extracts transcript language.
     *
     * @param root provider JSON
     * @return language code
     */
    /**
     * Extracts the detected transcript language.
     *
     * <p>
     * Realtime Sarvam STT returns the detected language in the
     * {@code language} field when automatic language detection is enabled.
     * The connection language code is retained as a fallback.
     * </p>
     *
     * @param root provider JSON
     * @return detected language code
     */
    private String extractLanguage(
            JsonNode root) {

        JsonNode languageNode =
                root.get(
                        SarvamStreamingConstants
                                .FIELD_LANGUAGE
                );

        if (languageNode != null
                && !languageNode.isNull()
                && !languageNode.asText().isBlank()) {

            return languageNode.asText();
        }

        JsonNode languageCodeNode =
                root.get(
                        SarvamStreamingConstants
                                .FIELD_LANGUAGE_CODE
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
     * Notifies the runtime that the provider WebSocket is ready.
     */
    /**
     * Notifies the runtime that the provider WebSocket is ready.
     */
    @Override
    public void onReady() {

        log.info(
                "Sarvam realtime STT session is ready. callId={}",
                callId
        );

        Runnable listener =
                readyListener;

        if (listener == null) {

            log.debug(
                    "No STT ready listener is registered. " +
                            "callId={}",
                    callId
            );

            return;
        }

        try {

            listener.run();

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
     * Registers a callback that is invoked when the Sarvam
     * WebSocket becomes ready.
     *
     * @param readyListener callback to execute after connection
     */
    /**
     * Registers a callback that is invoked when the Sarvam
     * WebSocket becomes ready.
     *
     * <p>
     * If the provider connection is already open when the listener
     * is registered, the listener is invoked immediately. This
     * prevents a startup race where the WebSocket becomes ready
     * before the runtime registers its callback.
     * </p>
     *
     * @param readyListener callback to execute after connection
     */
    @Override
    public void setReadyListener(
            Runnable readyListener) {

        this.readyListener =
                readyListener;

        log.debug(
                "Sarvam STT ready listener registered. " +
                        "callId={}, providerSocketOpen={}",
                callId,
                open
        );

        /*
         * Handle the race where the provider WebSocket became ready
         * before the runtime registered the listener.
         */
        if (open
                && readyListener != null) {

            log.debug(
                    "Sarvam STT WebSocket was already ready. " +
                            "Executing ready listener immediately. " +
                            "callId={}",
                    callId
            );

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