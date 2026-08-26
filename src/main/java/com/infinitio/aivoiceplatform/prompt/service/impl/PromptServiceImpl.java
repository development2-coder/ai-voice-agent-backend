package com.infinitio.aivoiceplatform.prompt.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.prompt.constant.PromptMessages;
import com.infinitio.aivoiceplatform.prompt.dto.request.CreatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.request.UpdatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.response.PromptResponse;
import com.infinitio.aivoiceplatform.prompt.entity.Prompt;
import com.infinitio.aivoiceplatform.prompt.mapper.PromptMapper;
import com.infinitio.aivoiceplatform.prompt.repository.PromptRepository;
import com.infinitio.aivoiceplatform.prompt.service.PromptService;
import com.infinitio.aivoiceplatform.prompt.validator.PromptValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Prompt.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;

    private final PromptMapper promptMapper;

    private final PromptValidator promptValidator;

    private final AgentValidator agentValidator;

    @Override
    public PromptResponse create(CreatePromptRequest request) {

        log.info(
                "Creating Prompt. Prompt Code : {}",
                request.getPromptCode()
        );

        promptValidator.validateForCreate(request);

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        Prompt prompt =
                promptMapper.toEntity(request);

        prompt.setAgent(agent);

        Prompt savedPrompt =
                promptRepository.save(prompt);

        log.info(
                "Prompt created successfully. Public Id : {}",
                savedPrompt.getPublicId()
        );

        return promptMapper.toResponse(savedPrompt);
    }

    @Override
    public PromptResponse update(UpdatePromptRequest request) {

        log.info(
                "Updating Prompt. Public Id : {}",
                request.getPublicId()
        );

        promptValidator.validateForUpdate(request);

        Prompt prompt =
                promptValidator.validateAndGet(
                        request.getPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        promptMapper.updateEntity(
                request,
                prompt
        );

        prompt.setAgent(agent);

        Prompt updatedPrompt =
                promptRepository.save(prompt);

        log.info(
                "Prompt updated successfully. Public Id : {}",
                updatedPrompt.getPublicId()
        );

        return promptMapper.toResponse(updatedPrompt);
    }

    @Override
    @Transactional(readOnly = true)
    public PromptResponse getByPublicId(String publicId) {

        log.info(
                "Fetching Prompt. Public Id : {}",
                publicId
        );

        Prompt prompt =
                promptValidator.validateAndGet(publicId);

        return promptMapper.toResponse(prompt);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromptResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Prompts. Page : {}, Size : {}",
                page,
                size
        );

        Page<Prompt> result =
                promptRepository.findAll(
                        PageRequest.of(page, size)
                );

        return PageResponse.<PromptResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(promptMapper::toResponse)
                                .toList()
                )
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Override
    public void delete(String publicId) {

        log.info(
                "Deleting Prompt. Public Id : {}",
                publicId
        );

        Prompt prompt =
                promptValidator.validateAndGet(publicId);

        prompt.markAsDeleted(1L);

        promptRepository.save(prompt);

        log.info(
                "Prompt deleted successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating Prompt. Public Id : {}",
                publicId
        );

        Prompt prompt =
                promptValidator.validateAndGet(publicId);

        prompt.activate(1L);

        promptRepository.save(prompt);

        log.info(
                "Prompt activated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating Prompt. Public Id : {}",
                publicId
        );

        Prompt prompt =
                promptValidator.validateAndGet(publicId);

        prompt.deactivate(1L);

        promptRepository.save(prompt);

        log.info(
                "Prompt deactivated successfully. Public Id : {}",
                publicId
        );
    }
}