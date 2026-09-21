package com.infinitio.aivoiceplatform.tts.streaming;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Maintains active TTS audio stream listeners and playback state
 * for live calls.
 *
 * <p>
 * The registry allows the TTS runtime to stream generated audio
 * directly to the active Voice Gateway session.
 * </p>
 *
 * <p>
 * Playback state represents outbound telephony audio that is still
 * being queued/sent, rather than only the period during which the
 * TTS provider is generating audio.
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
    private final ConcurrentHashMap<
            String,
            TtsAudioStreamListener>
            listeners =
            new ConcurrentHashMap<>();

    /**
     * TTS interruption state mapped by call identifier.
     */
    private final ConcurrentHashMap<
            String,
            AtomicBoolean>
            interruptions =
            new ConcurrentHashMap<>();

    /**
     * Active TTS playback state mapped by call identifier.
     *
     * <p>
     * This state remains active until the Voice Gateway has finished
     * sending the queued outbound audio.
     * </p>
     */
    private final ConcurrentHashMap<
            String,
            AtomicBoolean>
            activePlayback =
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

        if (callId == null
                || callId.isBlank()
                || listener == null) {

            log.warn(
                    "Unable to register TTS listener. " +
                            "callId or listener is invalid."
            );

            return;
        }

        listeners.put(
                callId,
                listener
        );

        interruptions.computeIfAbsent(
                callId,
                key -> new AtomicBoolean(false)
        );

        activePlayback.computeIfAbsent(
                callId,
                key -> new AtomicBoolean(false)
        );

        log.debug(
                "Registered TTS audio listener. callId={}",
                callId
        );
    }

    /**
     * Returns the registered listener for a call.
     *
     * @param callId call identifier
     * @return registered listener or null
     */
    public TtsAudioStreamListener getListener(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return null;
        }

        return listeners.get(
                callId
        );
    }

    /**
     * Marks the active TTS stream as interrupted.
     *
     * @param callId call identifier
     */
    public void interrupt(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        interruptions
                .computeIfAbsent(
                        callId,
                        key -> new AtomicBoolean(false)
                )
                .set(true);

        /*
         * Immediately stop treating the old audio stream as active.
         * Voice Gateway clearAudio() will discard already queued
         * provider audio.
         */
        stopPlayback(
                callId
        );

        log.debug(
                "TTS stream interrupted. callId={}",
                callId
        );
    }

    /**
     * Checks whether the active TTS stream has been interrupted.
     *
     * @param callId call identifier
     * @return true if interrupted
     */
    public boolean isInterrupted(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return false;
        }

        AtomicBoolean interrupted =
                interruptions.get(
                        callId
                );

        return interrupted != null
                && interrupted.get();
    }

    /**
     * Resets the interruption state before starting a new TTS stream.
     *
     * @param callId call identifier
     */
    public void resetInterruption(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        interruptions
                .computeIfAbsent(
                        callId,
                        key -> new AtomicBoolean(false)
                )
                .set(false);

        log.debug(
                "TTS interruption state reset. callId={}",
                callId
        );
    }

    /**
     * Marks TTS playback as active.
     *
     * @param callId call identifier
     */
    public void startPlayback(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

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
     * Marks TTS playback as inactive.
     *
     * @param callId call identifier
     */
    public void stopPlayback(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        AtomicBoolean playback =
                activePlayback.get(
                        callId
                );

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
     * @return true if playback is active
     */
    public boolean isPlaybackActive(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return false;
        }

        AtomicBoolean playback =
                activePlayback.get(
                        callId
                );

        return playback != null
                && playback.get();
    }

    /**
     * Removes interruption state for a call.
     *
     * @param callId call identifier
     */
    public void removeInterruption(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        interruptions.remove(
                callId
        );

        log.debug(
                "Removed TTS interruption state. callId={}",
                callId
        );
    }

    /**
     * Removes the complete TTS streaming state for a call.
     *
     * @param callId call identifier
     */
    public void remove(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        listeners.remove(
                callId
        );

        interruptions.remove(
                callId
        );

        activePlayback.remove(
                callId
        );

        log.debug(
                "Removed TTS audio stream state. callId={}",
                callId
        );
    }
}