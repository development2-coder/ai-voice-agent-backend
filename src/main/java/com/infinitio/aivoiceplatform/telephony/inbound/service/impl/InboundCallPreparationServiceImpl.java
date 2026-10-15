package com.infinitio.aivoiceplatform.telephony.inbound.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionCreateService;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowType;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.repository.FlowRepository;
import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import com.infinitio.aivoiceplatform.telephony.inbound.config.InboundTelephonyProperties;
import com.infinitio.aivoiceplatform.telephony.inbound.constants.InboundTelephonyConstants;
import com.infinitio.aivoiceplatform.telephony.inbound.repository.InboundPhoneNumberRepository;
import com.infinitio.aivoiceplatform.telephony.inbound.service.InboundCallPreparationService;
import com.infinitio.aivoiceplatform.user.constant.UserConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Default implementation of inbound call preparation.
 *
 * <p>
 * This service resolves the inbound phone number, Agent and
 * active inbound Flow and creates the application Call and
 * CallSession required by the existing Voice Gateway runtime.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InboundCallPreparationServiceImpl
        implements InboundCallPreparationService {

    private final CallRepository callRepository;

    private final InboundPhoneNumberRepository
            inboundPhoneNumberRepository;

    private final FlowRepository flowRepository;

    private final CallSessionCreateService
            callSessionCreateService;

    private final InboundTelephonyProperties
            inboundTelephonyProperties;

    /**
     * Prepares an inbound call.
     *
     * @param providerCode provider code received from telephony
     *                     integration
     * @param providerCallId provider-specific call identifier
     * @param callerNumber caller phone number
     * @param calledNumber called phone number
     * @return prepared application Call
     */
    @Override
    public Call prepareInboundCall(
            String providerCode,
            String providerCallId,
            String callerNumber,
            String calledNumber) {

        validateRequest(
                providerCode,
                providerCallId,
                callerNumber,
                calledNumber
        );

        String configuredProvider =
                resolveProvider(providerCode);

        log.info(
                "Preparing inbound call. provider={}, " +
                        "providerCallId={}, callerNumber={}, " +
                        "calledNumber={}",
                configuredProvider,
                providerCallId,
                maskPhoneNumber(callerNumber),
                maskPhoneNumber(calledNumber)
        );

        /*
         * ---------------------------------------------------------
         * STEP 1: IDEMPOTENCY CHECK
         * ---------------------------------------------------------
         */
        Optional<Call> existingCall =
                callRepository.findByProviderCallId(
                        providerCallId
                );

        if (existingCall.isPresent()) {

            log.info(
                    "Inbound Call already exists. " +
                            "provider={}, providerCallId={}, " +
                            "callPublicId={}",
                    configuredProvider,
                    providerCallId,
                    existingCall.get().getPublicId()
            );

            return existingCall.get();
        }

        /*
         * ---------------------------------------------------------
         * STEP 2: RESOLVE INBOUND PHONE NUMBER
         * ---------------------------------------------------------
         */
        PhoneNumber phoneNumber =
                resolveInboundPhoneNumber(
                        configuredProvider,
                        calledNumber
                );

        if (phoneNumber.getAgent() == null) {

            log.error(
                    "Inbound phone number is not associated " +
                            "with an Agent. phoneNumberId={}, " +
                            "calledNumber={}",
                    phoneNumber.getId(),
                    maskPhoneNumber(calledNumber)
            );

            throw new IllegalStateException(
                    "Inbound phone number is not associated "
                            + "with an Agent."
            );
        }

        log.info(
                "Inbound phone number resolved. " +
                        "phoneNumberId={}, agentPublicId={}",
                phoneNumber.getId(),
                phoneNumber.getAgent().getPublicId()
        );

        /*
         * ---------------------------------------------------------
         * STEP 3: RESOLVE ACTIVE INBOUND FLOW
         * ---------------------------------------------------------
         */
        Flow flow =
                resolveActiveInboundFlow(
                        phoneNumber
                                .getAgent()
                                .getId()
                );

        if (flow.getAgent() == null
                || flow.getAgent().getTenant() == null) {

            log.error(
                    "Inbound Flow has incomplete Agent/Tenant " +
                            "configuration. flowPublicId={}",
                    flow.getPublicId()
            );

            throw new IllegalStateException(
                    "Inbound Flow has incomplete Agent/Tenant "
                            + "configuration."
            );
        }

        log.info(
                "Inbound Flow resolved. flowPublicId={}, " +
                        "flowVersion={}, agentPublicId={}, " +
                        "flowType={}",
                flow.getPublicId(),
                flow.getVersion(),
                flow.getAgent().getPublicId(),
                flow.getFlowType()
        );

        /*
         * ---------------------------------------------------------
         * STEP 4: CREATE APPLICATION CALL
         * ---------------------------------------------------------
         */
        Call call =
                Call.builder()
                        .provider(
                                configuredProvider
                        )
                        .providerCallId(
                                providerCallId
                        )
                        .fromNumber(
                                callerNumber
                        )
                        .toNumber(
                                calledNumber
                        )
                        .direction(
                                InboundTelephonyConstants
                                        .DIRECTION_INBOUND
                        )
                        .status(
                                InboundTelephonyConstants
                                        .CALL_STATUS_INITIATED
                        )
                        .startedAt(
                                LocalDateTime.now()
                        )
                        .description(
                                InboundTelephonyConstants
                                        .CALL_DESCRIPTION
                        )
                        .createdBy(
                                UserConstants.SYSTEM_USER_ID
                        )
                        .build();

        Call savedCall =
                callRepository.saveAndFlush(
                        call
                );

        log.info(
                "Inbound Call created. callPublicId={}, " +
                        "providerCallId={}, agentPublicId={}, " +
                        "flowPublicId={}",
                savedCall.getPublicId(),
                providerCallId,
                flow.getAgent().getPublicId(),
                flow.getPublicId()
        );

        /*
         * ---------------------------------------------------------
         * STEP 5: CREATE CALL SESSION
         * ---------------------------------------------------------
         */
        CreateCallSessionRequestDto callSessionRequest =
                CreateCallSessionRequestDto
                        .builder()
                        .callId(
                                savedCall.getPublicId()
                        )
                        .tenantId(
                                flow.getAgent()
                                        .getTenant()
                                        .getPublicId()
                        )
                        .agentId(
                                flow.getAgent()
                                        .getPublicId()
                        )
                        .agentVersion(
                                flow.getVersion()
                        )
                        .flowPublicId(
                                flow.getPublicId()
                        )
                        .flowNodeId(
                                null
                        )
                        .language(
                                flow.getAgent()
                                        .getLanguage()
                        )
                        .build();

        callSessionCreateService.createCallSession(
                callSessionRequest,
                UserConstants.SYSTEM_USER_ID
        );

        log.info(
                "Inbound CallSession created. " +
                        "callPublicId={}, tenantPublicId={}, " +
                        "agentPublicId={}, flowPublicId={}, " +
                        "flowVersion={}",
                savedCall.getPublicId(),
                flow.getAgent()
                        .getTenant()
                        .getPublicId(),
                flow.getAgent().getPublicId(),
                flow.getPublicId(),
                flow.getVersion()
        );

        return savedCall;
    }

    /**
     * Resolves the provider that should be used for the inbound
     * call.
     *
     * <p>
     * The configured provider is the source of truth. The
     * provider received by the WebSocket integration is validated
     * against the configured provider.
     * </p>
     *
     * @param providerCode provider code received from the caller
     * @return configured provider
     */
    private String resolveProvider(
            String providerCode) {

        String configuredProvider =
                inboundTelephonyProperties
                        .getProvider();

        if (configuredProvider == null
                || configuredProvider.isBlank()) {

            throw new IllegalStateException(
                    "Inbound telephony provider is not configured."
            );
        }

        if (!configuredProvider.equalsIgnoreCase(
                providerCode
        )) {

            log.error(
                    "Inbound provider mismatch. " +
                            "configuredProvider={}, receivedProvider={}",
                    configuredProvider,
                    providerCode
            );

            throw new IllegalArgumentException(
                    "Inbound telephony provider does not match "
                            + "the configured provider."
            );
        }

        return configuredProvider.toUpperCase();
    }

    /**
     * Resolves the inbound phone number.
     *
     * @param providerCode configured provider
     * @param calledNumber called phone number
     * @return matching PhoneNumber
     */
    private PhoneNumber resolveInboundPhoneNumber(
            String providerCode,
            String calledNumber) {

        List<String> candidates =
                buildPhoneNumberCandidates(
                        calledNumber
                );

        List<PhoneNumber> phoneNumbers =
                inboundPhoneNumberRepository
                        .findInboundPhoneNumbers(
                                candidates,
                                providerCode
                        );

        if (phoneNumbers.isEmpty()) {

            log.error(
                    "No inbound phone number configuration " +
                            "found. provider={}, calledNumber={}",
                    providerCode,
                    maskPhoneNumber(calledNumber)
            );

            throw new IllegalStateException(
                    "No inbound phone number configuration found "
                            + "for the called number."
            );
        }

        if (phoneNumbers.size() > 1) {

            log.warn(
                    "Multiple inbound phone number configurations " +
                            "matched. provider={}, calledNumber={}, " +
                            "matchCount={}",
                    providerCode,
                    maskPhoneNumber(calledNumber),
                    phoneNumbers.size()
            );
        }

        return phoneNumbers.get(0);
    }

    /**
     * Resolves the latest active inbound-compatible Flow.
     *
     * @param agentId Agent database identifier
     * @return active inbound Flow
     */
    /**
     * Resolves the latest active inbound-compatible Flow.
     *
     * <p>
     * Only flows that are active, not deleted, have ACTIVE status,
     * and support inbound execution are eligible.
     * </p>
     *
     * @param agentId Agent database identifier
     * @return active inbound Flow
     */
    private Flow resolveActiveInboundFlow(
            Long agentId) {

        if (agentId == null) {

            log.error(
                    "Unable to resolve inbound Flow because " +
                            "agentId is null."
            );

            throw new IllegalStateException(
                    "Agent is required to resolve an inbound Flow."
            );
        }

        List<Flow> flows =
                flowRepository.findAllByAgentIdAndIsDeleted(
                        agentId,
                        0
                );

        if (flows == null || flows.isEmpty()) {

            log.error(
                    "No Flow configuration found for inbound Agent. " +
                            "agentId={}",
                    agentId
            );

            throw new IllegalStateException(
                    "No Flow configuration found for the Agent."
            );
        }

        Optional<Flow> activeInboundFlow =
                flows.stream()
                        .filter(Objects::nonNull)
                        .filter(
                                flow ->
                                        flow.getIsDeleted() != null
                                                && flow.getIsDeleted() == 0
                        )
                        .filter(
                                flow ->
                                        flow.getIsActive() != null
                                                && flow.getIsActive() == 1
                        )
                        .filter(
                                flow ->
                                        flow.getStatus()
                                                == FlowStatus.ACTIVE
                        )
                        .filter(
                                flow ->
                                        flow.getFlowType()
                                                == FlowType.INBOUND
                                                || flow.getFlowType()
                                                == FlowType.BOTH
                        )
                        .max(
                                Comparator.comparing(
                                        Flow::getVersion,
                                        Comparator.nullsFirst(
                                                Integer::compareTo
                                        )
                                )
                        );

        if (activeInboundFlow.isEmpty()) {

            log.error(
                    "No active inbound-compatible Flow found. " +
                            "agentId={}, totalFlows={}",
                    agentId,
                    flows.size()
            );

            throw new IllegalStateException(
                    "No active inbound Flow is configured "
                            + "for the Agent."
            );
        }

        Flow selectedFlow =
                activeInboundFlow.get();

        log.info(
                "Active inbound Flow selected. " +
                        "agentId={}, flowPublicId={}, " +
                        "flowVersion={}, flowType={}",
                agentId,
                selectedFlow.getPublicId(),
                selectedFlow.getVersion(),
                selectedFlow.getFlowType()
        );

        return selectedFlow;
    }

    /**
     * Builds candidate representations for the supplied phone
     * number.
     *
     * @param phoneNumber raw phone number
     * @return candidate phone numbers
     */
    private List<String> buildPhoneNumberCandidates(
            String phoneNumber) {

        String trimmed =
                phoneNumber.trim();

        String digitsOnly =
                trimmed.replaceAll(
                        "\\D",
                        ""
                );

        List<String> candidates =
                new ArrayList<>();

        addCandidate(
                candidates,
                trimmed
        );

        addCandidate(
                candidates,
                digitsOnly
        );

        if (digitsOnly.startsWith("91")
                && digitsOnly.length() == 12) {

            addCandidate(
                    candidates,
                    "+" + digitsOnly
            );
        }

        if (digitsOnly.length() == 10) {

            addCandidate(
                    candidates,
                    "+91" + digitsOnly
            );

            addCandidate(
                    candidates,
                    "91" + digitsOnly
            );
        }

        if (digitsOnly.startsWith("0")
                && digitsOnly.length() == 11) {

            String withoutLeadingZero =
                    digitsOnly.substring(1);

            addCandidate(
                    candidates,
                    withoutLeadingZero
            );

            addCandidate(
                    candidates,
                    "+91" + withoutLeadingZero
            );
        }

        return candidates;
    }

    /**
     * Adds a phone number candidate if it is not already present.
     *
     * @param candidates candidate list
     * @param candidate phone number candidate
     */
    private void addCandidate(
            List<String> candidates,
            String candidate) {

        if (candidate == null
                || candidate.isBlank()) {

            return;
        }

        if (!candidates.contains(candidate)) {

            candidates.add(candidate);
        }
    }

    /**
     * Validates inbound call preparation parameters.
     *
     * @param providerCode provider code
     * @param providerCallId provider call identifier
     * @param callerNumber caller number
     * @param calledNumber called number
     */
    private void validateRequest(
            String providerCode,
            String providerCallId,
            String callerNumber,
            String calledNumber) {

        if (providerCode == null
                || providerCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Provider code is required."
            );
        }

        if (providerCallId == null
                || providerCallId.isBlank()) {

            throw new IllegalArgumentException(
                    "Provider call ID is required."
            );
        }

        if (providerCallId.length()
                > InboundTelephonyConstants
                .MAX_PROVIDER_CALL_ID_LENGTH) {

            throw new IllegalArgumentException(
                    "Provider call ID exceeds the supported length."
            );
        }

        if (callerNumber == null
                || callerNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Caller number is required."
            );
        }

        if (calledNumber == null
                || calledNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Called number is required."
            );
        }

        if (callerNumber.length()
                > InboundTelephonyConstants
                .MAX_PHONE_NUMBER_LENGTH) {

            throw new IllegalArgumentException(
                    "Caller number exceeds the supported length."
            );
        }

        if (calledNumber.length()
                > InboundTelephonyConstants
                .MAX_PHONE_NUMBER_LENGTH) {

            throw new IllegalArgumentException(
                    "Called number exceeds the supported length."
            );
        }
    }

    /**
     * Masks a phone number before writing it to logs.
     *
     * @param phoneNumber phone number
     * @return masked phone number
     */
    private String maskPhoneNumber(
            String phoneNumber) {

        if (phoneNumber == null
                || phoneNumber.isBlank()) {

            return "****";
        }

        String normalized =
                phoneNumber.trim();

        if (normalized.length() <= 4) {

            return "****";
        }

        return "****"
                + normalized.substring(
                normalized.length() - 4
        );
    }
}