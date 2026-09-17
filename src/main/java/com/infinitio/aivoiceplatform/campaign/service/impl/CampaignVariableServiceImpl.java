package com.infinitio.aivoiceplatform.campaign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.repository.AiDialerRepository;
import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignVariablesResponse;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.service.CampaignVariableService;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import com.infinitio.aivoiceplatform.common.util.PromptVariableExtractor;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service implementation for resolving Campaign Flow variables.
 *
 * <p>
 * Campaigns are associated with Agents, while the actual
 * prompt configuration is stored inside AI_RESPONSE and LLM
 * Flow Nodes.
 * </p>
 *
 * <p>
 * When an AI Dialer already exists for the Campaign, the
 * Flow configured on that Dialer is used because it is the
 * actual runtime Flow. If no Dialer exists, the latest Flow
 * of the Campaign Agent is used.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignVariableServiceImpl
        implements CampaignVariableService {

    /**
     * Soft-delete value representing a non-deleted record.
     */
    private static final Integer NOT_DELETED = 0;

    /**
     * Prompt property inside Flow Node configuration.
     */
    private static final String PROMPT_KEY = "prompt";

    private final ObjectMapper objectMapper;

    private final CampaignValidator campaignValidator;

    private final FlowRepository flowRepository;

    private final FlowNodeRepository flowNodeRepository;

    private final AiDialerRepository aiDialerRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public CampaignVariablesResponse getVariables(
            String campaignPublicId) {

        log.info(
                "Extracting Campaign Flow variables. " +
                        "Campaign : {}",
                campaignPublicId
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        campaignPublicId
                );

        Flow flow =
                resolveCampaignFlow(
                        campaign
                );

        List<FlowNode> nodes =
                flowNodeRepository
                        .findByFlowIdAndIsDeletedOrderByIdAsc(
                                flow.getId(),
                                NOT_DELETED
                        );

        Set<String> variables =
                new LinkedHashSet<>();

        for (FlowNode node : nodes) {

            if (!isPromptNode(node)) {
                continue;
            }

            String prompt =
                    extractPrompt(
                            node.getConfiguration()
                    );

            if (prompt == null
                    || prompt.isBlank()) {

                continue;
            }

            variables.addAll(
                    PromptVariableExtractor.extractVariables(
                            prompt
                    )
            );
        }

        log.info(
                "Campaign Flow variables extracted. " +
                        "Campaign : {}, Flow : {}, " +
                        "Variable Count : {}",
                campaignPublicId,
                flow.getPublicId(),
                variables.size()
        );

        return CampaignVariablesResponse.builder()
                .campaignPublicId(
                        campaign.getPublicId()
                )
                .agentPublicId(
                        campaign.getAgent().getPublicId()
                )
                .flowPublicId(
                        flow.getPublicId()
                )
                .variables(
                        new ArrayList<>(variables)
                )
                .build();
    }

    /**
     * Resolves the Flow used by the Campaign.
     *
     * <p>
     * An existing AI Dialer Flow has priority because that
     * Flow is the one configured for actual campaign execution.
     * If no AI Dialer exists, the latest Flow of the Campaign
     * Agent is used.
     * </p>
     *
     * @param campaign campaign entity
     * @return resolved Flow
     */
    private Flow resolveCampaignFlow(
            Campaign campaign) {

        List<AiDialer> dialers =
                aiDialerRepository
                        .findAllByCampaignIdAndIsDeleted(
                                campaign.getId(),
                                NOT_DELETED
                        );

        for (AiDialer dialer : dialers) {

            if (dialer.getFlow() != null) {

                log.debug(
                        "Using AI Dialer Flow for Campaign. " +
                                "Campaign : {}, Dialer : {}, Flow : {}",
                        campaign.getPublicId(),
                        dialer.getPublicId(),
                        dialer.getFlow().getPublicId()
                );

                return dialer.getFlow();
            }
        }

        Flow flow =
                flowRepository
                        .findFirstByAgentIdAndIsDeletedOrderByVersionDesc(
                                campaign.getAgent().getId(),
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No Flow found for Campaign Agent."
                                )
                        );

        log.debug(
                "Using latest Agent Flow for Campaign. " +
                        "Campaign : {}, Flow : {}",
                campaign.getPublicId(),
                flow.getPublicId()
        );

        return flow;
    }

    /**
     * Determines whether a Flow Node contains a prompt.
     *
     * @param node Flow Node
     * @return true when the node is AI_RESPONSE or LLM
     */
    private boolean isPromptNode(
            FlowNode node) {

        return node != null
                && (
                FlowNodeType.AI_RESPONSE
                        .equals(node.getNodeType())
                        || FlowNodeType.LLM
                        .equals(node.getNodeType())
        );
    }

    /**
     * Extracts prompt text from Flow Node configuration.
     *
     * @param configuration JSON configuration
     * @return configured prompt or null
     */
    private String extractPrompt(
            String configuration) {

        if (configuration == null
                || configuration.isBlank()) {

            return null;
        }

        try {

            Map<String, Object> values =
                    objectMapper.readValue(
                            configuration,
                            new TypeReference<
                                    Map<String, Object>>() {
                            }
                    );

            Object prompt =
                    values.get(
                            PROMPT_KEY
                    );

            if (prompt == null) {
                return null;
            }

            String promptValue =
                    String.valueOf(prompt).trim();

            return promptValue.isBlank()
                    ? null
                    : promptValue;

        } catch (Exception exception) {

            log.warn(
                    "Unable to read prompt from Flow Node " +
                            "configuration.",
                    exception
            );

            return null;
        }
    }
}