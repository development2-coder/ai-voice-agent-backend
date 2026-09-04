package com.infinitio.aivoiceplatform.stt.provider;

import java.net.http.WebSocket;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

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
    @Override
    public void onOpen(
            WebSocket webSocket) {

        this.webSocket =
                webSocket;

        this.open =
                true;

        this.closing =
                false;

        log.info(
                "Sarvam realtime STT WebSocket connected. " +
                        "callId={}, sampleRate={}, language={}",
                callId,
                sampleRate,
                language
        );

        webSocket.request(1);
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
    @Override
    public void sendAudio(
            byte[] audio) {

        if (audio == null
                || audio.length == 0) {

            log.debug(
                    "Ignoring empty STT audio chunk. callId={}",
                    callId
            );

            return;
        }

        WebSocket currentSocket =
                webSocket;

        if (!isOpen()
                || currentSocket == null) {

            log.warn(
                    "Cannot send STT audio because WebSocket " +
                            "is not open. callId={}, audioBytes={}",
                    callId,
                    audio.length
            );

            throw new IllegalStateException(
                    SttMessages.STREAMING_SESSION_NOT_OPEN
            );
        }

        String encodedAudio =
                Base64.getEncoder()
                        .encodeToString(
                                audio
                        );

        String payload =
                buildAudioPayload(
                        encodedAudio
                );

        try {

            currentSocket.sendText(
                    payload,
                    true
            );

            log.debug(
                    "STT audio chunk sent to Sarvam. " +
                            "callId={}, audioBytes={}",
                    callId,
                    audio.length
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to send STT audio to Sarvam. " +
                            "callId={}, audioBytes={}",
                    callId,
                    audio.length,
                    exception
            );

            listener.onError(
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

        WebSocket currentSocket =
                webSocket;

        return open
                && !closing
                && currentSocket != null
                && !currentSocket.isInputClosed()
                && !currentSocket.isOutputClosed();
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
                            "callId={}, event={}",
                    callId,
                    event
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

        listener.onFinalTranscript(
                callId,
                transcript
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
    private String extractLanguage(
            JsonNode root) {

        JsonNode languageCodeNode =
                root.get(
                        SarvamStreamingConstants
                                .FIELD_LANGUAGE_CODE
                );

        if (languageCodeNode != null
                && !languageCodeNode.isNull()
                && !languageCodeNode.asText().isBlank()) {

            return languageCodeNode.asText();
        }

        return language;
    }
}