package com.infinitio.aivoiceplatform.organization.organizationaddress.service.impl;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import com.infinitio.aivoiceplatform.organization.organization.service.OrganizationService;
import com.infinitio.aivoiceplatform.organization.organization.validator.OrganizationValidator;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.CreateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.UpdateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.response.OrganizationAddressResponse;
import com.infinitio.aivoiceplatform.organization.organizationaddress.entity.OrganizationAddress;
import com.infinitio.aivoiceplatform.organization.organizationaddress.mapper.OrganizationAddressMapper;
import com.infinitio.aivoiceplatform.organization.organizationaddress.repository.OrganizationAddressRepository;
import com.infinitio.aivoiceplatform.organization.organizationaddress.service.OrganizationAddressService;
import com.infinitio.aivoiceplatform.organization.organizationaddress.validator.OrganizationAddressValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationAddressServiceImpl
        implements OrganizationAddressService {

    private final OrganizationAddressRepository repository;

    private final OrganizationAddressMapper mapper;

    private final OrganizationAddressValidator validator;

    private final OrganizationValidator organizationValidator;

    @Override
    public OrganizationAddressResponse create(
            CreateOrganizationAddressRequest request) {

        validator.validateForCreate(request);

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getOrganizationPublicId());

        OrganizationAddress entity = mapper.toEntity(request);

        entity.setOrganization(organization);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public OrganizationAddressResponse update(
            UpdateOrganizationAddressRequest request) {

        validator.validateForUpdate(request);

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getOrganizationPublicId());

        OrganizationAddress entity =
                validator.validateAndGet(request.getPublicId());

        mapper.updateEntity(request, entity);

        entity.setOrganization(organization);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationAddressResponse getByPublicId(String publicId) {

        return mapper.toResponse(
                validator.validateAndGet(publicId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrganizationAddressResponse> getAll(
            int page,
            int size) {

        Page<OrganizationAddress> result =
                repository.findAll(PageRequest.of(page, size));

        return PageResponse.<OrganizationAddressResponse>builder()
                .content(result.getContent()
                        .stream()
                        .map(mapper::toResponse)
                        .toList())
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

        OrganizationAddress entity =
                validator.validateAndGet(publicId);

        entity.markAsDeleted(1L);

        repository.save(entity);
    }

    @Override
    public void activate(String publicId) {

        OrganizationAddress entity =
                validator.validateAndGet(publicId);

        entity.activate(1L);

        repository.save(entity);
    }

    @Override
    public void deactivate(String publicId) {

        OrganizationAddress entity =
                validator.validateAndGet(publicId);

        entity.deactivate(1L);

        repository.save(entity);
    }

}