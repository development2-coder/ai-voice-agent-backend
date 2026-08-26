package com.infinitio.aivoiceplatform.organization.organizationbranding.service.impl;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import com.infinitio.aivoiceplatform.organization.organization.validator.OrganizationValidator;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.CreateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.UpdateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.response.OrganizationBrandingResponse;
import com.infinitio.aivoiceplatform.organization.organizationbranding.entity.OrganizationBranding;
import com.infinitio.aivoiceplatform.organization.organizationbranding.mapper.OrganizationBrandingMapper;
import com.infinitio.aivoiceplatform.organization.organizationbranding.repository.OrganizationBrandingRepository;
import com.infinitio.aivoiceplatform.organization.organizationbranding.service.OrganizationBrandingService;
import com.infinitio.aivoiceplatform.organization.organizationbranding.validator.OrganizationBrandingValidator;
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
public class OrganizationBrandingServiceImpl
        implements OrganizationBrandingService {

    private final OrganizationBrandingRepository organizationBrandingRepository;

    private final OrganizationBrandingMapper organizationBrandingMapper;

    private final OrganizationBrandingValidator organizationBrandingValidator;

    private final OrganizationValidator organizationValidator;

    @Override
    public OrganizationBrandingResponse create(
            CreateOrganizationBrandingRequest request) {

        log.info("Creating Organization Branding.");

        organizationBrandingValidator.validateForCreate(request);

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getOrganizationPublicId());

        OrganizationBranding entity =
                organizationBrandingMapper.toEntity(request);

        entity.setOrganization(organization);

        OrganizationBranding savedEntity =
                organizationBrandingRepository.save(entity);

        log.info("Organization Branding created successfully : {}",
                savedEntity.getPublicId());

        return organizationBrandingMapper.toResponse(savedEntity);
    }

    @Override
    public OrganizationBrandingResponse update(
            UpdateOrganizationBrandingRequest request) {

        log.info("Updating Organization Branding : {}",
                request.getPublicId());

        organizationBrandingValidator.validateForUpdate(request);

        OrganizationBranding entity =
                organizationBrandingValidator.validateAndGet(
                        request.getPublicId());

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getOrganizationPublicId());

        organizationBrandingMapper.updateEntity(request, entity);

        entity.setOrganization(organization);

        OrganizationBranding updatedEntity =
                organizationBrandingRepository.save(entity);

        log.info("Organization Branding updated successfully : {}",
                updatedEntity.getPublicId());

        return organizationBrandingMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationBrandingResponse getByPublicId(String publicId) {

        log.info("Fetching Organization Branding : {}", publicId);

        OrganizationBranding entity =
                organizationBrandingValidator.validateAndGet(publicId);

        return organizationBrandingMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrganizationBrandingResponse> getAll(
            int page,
            int size) {

        log.info("Fetching Organization Brandings. Page : {}, Size : {}",
                page,
                size);

        Page<OrganizationBranding> entityPage =
                organizationBrandingRepository.findAll(
                        PageRequest.of(page, size));

        return PageResponse.<OrganizationBrandingResponse>builder()
                .content(
                        entityPage.getContent()
                                .stream()
                                .map(organizationBrandingMapper::toResponse)
                                .toList()
                )
                .pageNumber(entityPage.getNumber())
                .pageSize(entityPage.getSize())
                .totalElements(entityPage.getTotalElements())
                .totalPages(entityPage.getTotalPages())
                .first(entityPage.isFirst())
                .last(entityPage.isLast())
                .build();
    }

    @Override
    public void delete(String publicId) {

        log.info("Deleting Organization Branding : {}", publicId);

        OrganizationBranding entity =
                organizationBrandingValidator.validateAndGet(publicId);

        entity.markAsDeleted(1L);

        organizationBrandingRepository.save(entity);

        log.info("Organization Branding deleted successfully.");
    }

    @Override
    public void activate(String publicId) {

        log.info("Activating Organization Branding : {}", publicId);

        OrganizationBranding entity =
                organizationBrandingValidator.validateAndGet(publicId);

        entity.activate(1L);

        organizationBrandingRepository.save(entity);

        log.info("Organization Branding activated successfully.");
    }

    @Override
    public void deactivate(String publicId) {

        log.info("Deactivating Organization Branding : {}", publicId);

        OrganizationBranding entity =
                organizationBrandingValidator.validateAndGet(publicId);

        entity.deactivate(1L);

        organizationBrandingRepository.save(entity);

        log.info("Organization Branding deactivated successfully.");
    }

}