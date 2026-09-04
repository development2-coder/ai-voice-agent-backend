package com.infinitio.aivoiceplatform.telephony.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an incoming or outgoing Exotel WebSocket message.
 *
 * <p>
 * The DTO represents the provider-specific WebSocket protocol and
 * is intentionally kept inside the telephony module.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
public class ExotelWebSocketMessage {

    /**
     * Exotel WebSocket event name.
     */
    private String event;

    /**
     * Provider sequence number.
     */
    @JsonProperty("sequence_number")
    private String sequenceNumber;

    /**
     * Exotel stream identifier.
     */
    @JsonProperty("stream_sid")
    private String streamSid;

    /**
     * Start event metadata.
     */
    private ExotelStartMessage start;

    /**
     * Media event metadata.
     */
    private ExotelMediaMessage media;

    /**
     * DTMF event metadata.
     */
    private ExotelDtmfMessage dtmf;

    /**
     * Stop event metadata.
     */
    private ExotelStopMessage stop;

    /**
     * Mark event metadata.
     */
    private ExotelMarkMessage mark;

    /**
     * Represents Exotel start metadata.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExotelStartMessage {

        /**
         * Stream identifier.
         */
        @JsonProperty("stream_sid")
        private String streamSid;

        /**
         * Exotel call identifier.
         */
        @JsonProperty("call_sid")
        private String callSid;

        /**
         * Exotel account identifier.
         */
        @JsonProperty("account_sid")
        private String accountSid;

        /**
         * Caller number.
         */
        private String from;

        /**
         * Destination number.
         */
        private String to;

        /**
         * Provider custom parameters.
         */
        @JsonProperty("custom_parameters")
        private Object customParameters;

        /**
         * Stream media format.
         */
        @JsonProperty("media_format")
        private ExotelMediaFormat mediaFormat;
    }

    /**
     * Represents Exotel media metadata.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExotelMediaMessage {

        /**
         * Media chunk number.
         */
        private String chunk;

        /**
         * Media timestamp.
         */
        private String timestamp;

        /**
         * Base64 encoded audio payload.
         */
        private String payload;
    }

    /**
     * Represents Exotel DTMF metadata.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExotelDtmfMessage {

        /**
         * DTMF duration in milliseconds.
         */
        private String duration;

        /**
         * Pressed DTMF digit.
         */
        private String digit;
    }

    /**
     * Represents Exotel stop metadata.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExotelStopMessage {

        /**
         * Exotel call identifier.
         */
        @JsonProperty("call_sid")
        private String callSid;

        /**
         * Exotel account identifier.
         */
        @JsonProperty("account_sid")
        private String accountSid;

        /**
         * Stream termination reason.
         */
        private String reason;
    }

    /**
     * Represents Exotel mark metadata.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExotelMarkMessage {

        /**
         * Provider mark name.
         */
        private String name;
    }

    /**
     * Represents Exotel media format metadata.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExotelMediaFormat {

        /**
         * Audio encoding.
         */
        private String encoding;

        /**
         * Audio sample rate.
         */
        @JsonProperty("sample_rate")
        private Integer sampleRate;

        /**
         * Audio bitrate.
         */
        @JsonProperty("bit_rate")
        private Integer bitRate;
    }
}