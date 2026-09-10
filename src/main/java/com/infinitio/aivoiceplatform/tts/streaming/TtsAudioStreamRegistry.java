package com.infinitio.aivoiceplatform.tts.streaming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Maintains active TTS audio stream listeners for live calls.
 *
 * <p>
 * The registry allows the TTS runtime to stream generated audio
 * directly to the active Voice Gateway session.
 * </p>
 *
 * <p>
 * This implementation is intentionally in-memory because Redis
 * is not part of the current Phase 1 architecture.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
public class TtsAudioStreamRegistry {

    /**
     * Active TTS listeners mapped by call identifier.
     */
    private final ConcurrentHashMap<String, TtsAudioStreamListener> listeners =
            new ConcurrentHashMap<>();

    /**
     * TTS interruption state mapped by call identifier.
     */
    private final ConcurrentHashMap<String, AtomicBoolean> interruptions =
            new ConcurrentHashMap<>();

    /**
     * Active TTS playback state mapped by call identifier.
     */
    private final ConcurrentHashMap<String, AtomicBoolean> activePlayback =
            new ConcurrentHashMap<>();


    /**
     * Registers a TTS audio listener for a call.
     *
     * @param callId call identifier
     * @param listener audio stream listener
     */
    public void register(
            String callId,
            TtsAudioStreamListener listener) {

        if (callId == null || callId.isBlank() || listener == null) {
            log.warn("Unable to register TTS listener. callId or listener is invalid.");
            return;
        }

        listeners.put(callId, listener);

        interruptions.computeIfAbsent(
                callId,
                key -> new AtomicBoolean(false)
        );

        activePlayback.computeIfAbsent(
                callId,
                key -> new AtomicBoolean(false)
        );

        log.debug("Registered TTS audio listener. callId={}", callId);
    }

    /**
     * Returns the registered listener for a call.
     *
     * @param callId call identifier
     * @return registered listener or {@code null}
     */
    public TtsAudioStreamListener getListener(String callId) {
        if (callId == null || callId.isBlank()) {
            return null;
        }

        return listeners.get(callId);
    }

    /**
     * Marks the active TTS stream as interrupted.
     *
     * @param callId call identifier
     */
    public void interrupt(String callId) {
        if (callId == null || callId.isBlank()) {
            return;
        }

        interruptions
                .computeIfAbsent(
                        callId,
                        key -> new AtomicBoolean(false)
                )
                .set(true);

        log.debug("TTS stream interrupted. callId={}", callId);
    }

    /**
     * Checks whether the active TTS stream has been interrupted.
     *
     * @param callId call identifier
     * @return {@code true} if interrupted
     */
    public boolean isInterrupted(String callId) {
        if (callId == null || callId.isBlank()) {
            return false;
        }

        AtomicBoolean interrupted = interruptions.get(callId);

        return interrupted != null && interrupted.get();
    }

    /**
     * Resets the interruption state before starting a new TTS stream.
     *
     * @param callId call identifier
     */
    public void resetInterruption(String callId) {
        if (callId == null || callId.isBlank()) {
            return;
        }

        interruptions
                .computeIfAbsent(
                        callId,
                        key -> new AtomicBoolean(false)
                )
                .set(false);

        log.debug("TTS interruption state reset. callId={}", callId);
    }

    /**
     * Marks TTS playback as active for a call.
     *
     * @param callId call identifier
     */
    public void startPlayback(String callId) {

        if (callId == null || callId.isBlank()) {
            return;
        }

        activePlayback
                .computeIfAbsent(
                        callId,
                        key -> new AtomicBoolean(false)
                )
                .set(true);

        log.debug(
                "TTS playback started. callId={}",
                callId
        );
    }

    /**
     * Marks TTS playback as inactive for a call.
     *
     * @param callId call identifier
     */
    public void stopPlayback(String callId) {

        if (callId == null || callId.isBlank()) {
            return;
        }

        AtomicBoolean playback =
                activePlayback.get(callId);

        if (playback != null) {
            playback.set(false);
        }

        log.debug(
                "TTS playback stopped. callId={}",
                callId
        );
    }

    /**
     * Checks whether TTS playback is currently active.
     *
     * @param callId call identifier
     * @return {@code true} if TTS playback is active
     */
    public boolean isPlaybackActive(String callId) {

        if (callId == null || callId.isBlank()) {
            return false;
        }

        AtomicBoolean playback =
                activePlayback.get(callId);

        return playback != null && playback.get();
    }

    /**
     * Removes the interruption state for a call.
     *
     * @param callId call identifier
     */
    public void removeInterruption(String callId) {
        if (callId == null || callId.isBlank()) {
            return;
        }

        interruptions.remove(callId);

        log.debug("Removed TTS interruption state. callId={}", callId);
    }

    /**
     * Removes the complete TTS streaming state for a call.
     *
     * @param callId call identifier
     */
    /**
     * Removes the complete TTS streaming state for a call.
     *
     * @param callId call identifier
     */
    public void remove(String callId) {

        if (callId == null || callId.isBlank()) {
            return;
        }

        listeners.remove(callId);
        interruptions.remove(callId);
        activePlayback.remove(callId);

        log.debug(
                "Removed TTS audio stream state. callId={}",
                callId
        );
    }
}