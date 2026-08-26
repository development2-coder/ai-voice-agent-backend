package com.infinitio.aivoiceplatform.phonenumber.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.CreatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.UpdatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.response.PhoneNumberResponse;
import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import com.infinitio.aivoiceplatform.phonenumber.mapper.PhoneNumberMapper;
import com.infinitio.aivoiceplatform.phonenumber.repository.PhoneNumberRepository;
import com.infinitio.aivoiceplatform.phonenumber.service.PhoneNumberService;
import com.infinitio.aivoiceplatform.phonenumber.validator.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Phone Number.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PhoneNumberServiceImpl
        implements PhoneNumberService {

    private final PhoneNumberRepository phoneNumberRepository;

    private final PhoneNumberMapper phoneNumberMapper;

    private final PhoneNumberValidator phoneNumberValidator;

    private final AgentValidator agentValidator;

    @Override
    public PhoneNumberResponse create(
            CreatePhoneNumberRequest request) {

        log.info(
                "Creating Phone Number. Number : {}, Provider : {}",
                request.getPhoneNumber(),
                request.getProvider()
        );

        phoneNumberValidator.validateForCreate(request);

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        PhoneNumber phoneNumber =
                phoneNumberMapper.toEntity(request);

        phoneNumber.setAgent(agent);

        PhoneNumber savedPhoneNumber =
                phoneNumberRepository.save(phoneNumber);

        log.info(
                "Phone Number created successfully. Public Id : {}",
                savedPhoneNumber.getPublicId()
        );

        return phoneNumberMapper.toResponse(
                savedPhoneNumber
        );
    }

    @Override
    public PhoneNumberResponse update(
            UpdatePhoneNumberRequest request) {

        log.info(
                "Updating Phone Number. Public Id : {}",
                request.getPublicId()
        );

        phoneNumberValidator.validateForUpdate(request);

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        request.getPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        phoneNumberMapper.updateEntity(
                request,
                phoneNumber
        );

        phoneNumber.setAgent(agent);

        PhoneNumber updatedPhoneNumber =
                phoneNumberRepository.save(phoneNumber);

        log.info(
                "Phone Number updated successfully. Public Id : {}",
                updatedPhoneNumber.getPublicId()
        );

        return phoneNumberMapper.toResponse(
                updatedPhoneNumber
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PhoneNumberResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Phone Number. Public Id : {}",
                publicId
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        publicId
                );

        return phoneNumberMapper.toResponse(
                phoneNumber
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PhoneNumberResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Phone Numbers. Page : {}, Size : {}",
                page,
                size
        );

        Page<PhoneNumber> result =
                phoneNumberRepository.findAll(
                        PageRequest.of(page, size)
                );

        return PageResponse
                .<PhoneNumberResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        phoneNumberMapper::toResponse
                                )
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
                "Deleting Phone Number. Public Id : {}",
                publicId
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        publicId
                );

        phoneNumber.markAsDeleted(1L);

        phoneNumberRepository.save(phoneNumber);

        log.info(
                "Phone Number deleted successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating Phone Number. Public Id : {}",
                publicId
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        publicId
                );

        phoneNumber.activate(1L);

        phoneNumberRepository.save(phoneNumber);

        log.info(
                "Phone Number activated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating Phone Number. Public Id : {}",
                publicId
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        publicId
                );

        phoneNumber.deactivate(1L);

        phoneNumberRepository.save(phoneNumber);

        log.info(
                "Phone Number deactivated successfully. Public Id : {}",
                publicId
        );
    }
}