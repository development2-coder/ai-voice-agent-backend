package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerCallResponse;

import java.util.List;

public interface DialerCallService {

    /*
     * Create a new call attempt for a campaign contact.
     */
    DialerCallResponse createCall(
            String dialerPublicId,
            String campaignContactPublicId
    );

    /*
     * Get a call by public ID.
     */
    DialerCallResponse getByPublicId(
            String publicId
    );

    /*
     * Get all calls belonging to a dialer.
     */
    List<DialerCallResponse> getByDialer(
            String dialerPublicId
    );

    /*
     * Get all attempts made for a campaign contact.
     */
    List<DialerCallResponse> getByCampaignContact(
            String campaignContactPublicId
    );

    /*
     * Update the call status.
     */
    DialerCallResponse updateStatus(
            String publicId,
            CallAttemptStatus status
    );

    /*
     * Mark Exotel call ID against the attempt.
     */
    DialerCallResponse updateExotelCallId(
            String publicId,
            String exotelCallId
    );

    /*
     * Mark the Flow execution associated with
     * this call.
     */
    DialerCallResponse updateFlowExecution(
            String publicId,
            String flowExecutionPublicId
    );

    /*
     * Mark the call as answered.
     */
    DialerCallResponse markAnswered(
            String publicId
    );

    /*
     * Mark the call as completed.
     */
    DialerCallResponse completeCall(
            String publicId,
            Integer durationSeconds,
            String hangupReason
    );

    /*
     * Mark the call as failed.
     */
    DialerCallResponse failCall(
            String publicId,
            String failureReason
    );

    /*
     * Determine whether another attempt can be made.
     */
    boolean canRetry(
            String dialerPublicId,
            String campaignContactPublicId
    );
}