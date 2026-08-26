package com.infinitio.aivoiceplatform.llm.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.llm.dto.request.CreateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.request.UpdateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.response.LlmResponse;
import com.infinitio.aivoiceplatform.llm.entity.Llm;
import com.infinitio.aivoiceplatform.llm.mapper.LlmMapper;
import com.infinitio.aivoiceplatform.llm.repository.LlmRepository;
import com.infinitio.aivoiceplatform.llm.service.LlmService;
import com.infinitio.aivoiceplatform.llm.validator.LlmValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for LLM.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LlmServiceImpl implements LlmService {

    private final LlmRepository llmRepository;

    private final LlmMapper llmMapper;

    private final LlmValidator llmValidator;

    private final AgentValidator agentValidator;

    @Override
    public LlmResponse create(CreateLlmRequest request) {

        log.info(
                "Creating LLM. Code : {}, Provider : {}, Model : {}",
                request.getLlmCode(),
                request.getProvider(),
                request.getModel()
        );

        llmValidator.validateForCreate(request);

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        Llm llm =
                llmMapper.toEntity(request);

        llm.setAgent(agent);

        Llm savedLlm =
                llmRepository.save(llm);

        log.info(
                "LLM created successfully. Public Id : {}",
                savedLlm.getPublicId()
        );

        return llmMapper.toResponse(savedLlm);
    }

    @Override
    public LlmResponse update(UpdateLlmRequest request) {

        log.info(
                "Updating LLM. Public Id : {}",
                request.getPublicId()
        );

        llmValidator.validateForUpdate(request);

        Llm llm =
                llmValidator.validateAndGet(
                        request.getPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        llmMapper.updateEntity(
                request,
                llm
        );

        llm.setAgent(agent);

        Llm updatedLlm =
                llmRepository.save(llm);

        log.info(
                "LLM updated successfully. Public Id : {}",
                updatedLlm.getPublicId()
        );

        return llmMapper.toResponse(updatedLlm);
    }

    @Override
    @Transactional(readOnly = true)
    public LlmResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching LLM. Public Id : {}",
                publicId
        );

        Llm llm =
                llmValidator.validateAndGet(publicId);

        return llmMapper.toResponse(llm);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LlmResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching LLM configurations. Page : {}, Size : {}",
                page,
                size
        );

        Page<Llm> result =
                llmRepository.findAll(
                        PageRequest.of(page, size)
                );

        return PageResponse.<LlmResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(llmMapper::toResponse)
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
                "Deleting LLM. Public Id : {}",
                publicId
        );

        Llm llm =
                llmValidator.validateAndGet(publicId);

        llm.markAsDeleted(1L);

        llmRepository.save(llm);

        log.info(
                "LLM deleted successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating LLM. Public Id : {}",
                publicId
        );

        Llm llm =
                llmValidator.validateAndGet(publicId);

        llm.activate(1L);

        llmRepository.save(llm);

        log.info(
                "LLM activated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating LLM. Public Id : {}",
                publicId
        );

        Llm llm =
                llmValidator.validateAndGet(publicId);

        llm.deactivate(1L);

        llmRepository.save(llm);

        log.info(
                "LLM deactivated successfully. Public Id : {}",
                publicId
        );
    }
}