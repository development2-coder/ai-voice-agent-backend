package com.infinitio.aivoiceplatform.stt.provider;

/**
 * Represents one provider-side streaming STT session.
 *
 * <p>
 * One instance belongs to one active telephone call and remains
 * connected while MEDIA packets are received.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface SttStreamingSession {

    /**
     * Returns the application call identifier.
     *
     * @return call identifier
     */
    String getCallId();

    /**
     * Sends an audio chunk to the provider.
     *
     * @param audio audio bytes
     */
    void sendAudio(
            byte[] audio
    );

    /**
     * Sends a provider-side turn/end signal.
     */
    void finishTurn();

    /**
     * Closes the provider streaming connection.
     */
    void close();

    /**
     * Checks whether the provider connection is open.
     *
     * @return true when connected
     */
    boolean isOpen();
}