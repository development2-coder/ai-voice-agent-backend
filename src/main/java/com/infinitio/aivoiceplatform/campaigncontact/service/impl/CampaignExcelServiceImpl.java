package com.infinitio.aivoiceplatform.campaigncontact.service.impl;

import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactMessages;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignExcelService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of Campaign Excel Service.
 *
 * <p>
 * Handles Excel header validation, row validation and mapping
 * of Campaign Contact Excel data to Campaign Contact requests.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class CampaignExcelServiceImpl
        implements CampaignExcelService {

    private static final String CUSTOM_DATA_PREFIX =
            "contact.customData.";

    private static final String CONTACT_NAME =
            "contact.name";

    private static final String NAME =
            "name";

    private static final String CONTACT_EXTERNAL_REFERENCE =
            "contact.externalReference";

    private static final String EXTERNAL_REFERENCE =
            "externalReference";

    /**
     * Object mapper used to serialize custom contact data.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates Campaign Excel Service implementation.
     *
     * @param objectMapper object mapper
     */
    public CampaignExcelServiceImpl(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> readHeaders(
            Row headerRow,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        if (headerRow == null) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_PHONE_COLUMN_REQUIRED
            );
        }

        List<String> headers =
                new ArrayList<>();

        for (
                int columnIndex = 0;
                columnIndex < headerRow.getLastCellNum();
                columnIndex++
        ) {

            Cell cell =
                    headerRow.getCell(
                            columnIndex,
                            Row.MissingCellPolicy
                                    .RETURN_BLANK_AS_NULL
                    );

            String header =
                    getCellValue(
                            cell,
                            formatter,
                            evaluator
                    );

            headers.add(
                    normalizeHeader(header)
            );
        }

        return headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateHeaders(
            List<String> headers,
            Set<String> promptVariables) {

        validateDuplicateHeaders(
                headers
        );

        validatePhoneNumberHeader(
                headers
        );

        validateNameHeader(
                headers
        );

        validateRequiredPromptVariables(
                headers,
                promptVariables
        );

        validateUnexpectedHeaders(
                headers,
                promptVariables
        );
    }

    private void validateNameHeader(
            List<String> headers) {

        if (!headers.contains(
                CampaignContactConstants.EXCEL_NAME_HEADER
        )) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_NAME_COLUMN_REQUIRED
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateCampaignContactRequest mapRow(
            String campaignPublicId,
            Row row,
            List<String> headers,
            Set<String> promptVariables,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        Map<String, Integer> headerIndexMap =
                buildHeaderIndexMap(
                        headers
                );

        String phoneNumber =
                getValueByHeader(
                        row,
                        headerIndexMap,
                        CampaignContactConstants
                                .EXCEL_PHONE_NUMBER_HEADER,
                        formatter,
                        evaluator
                );

        if (phoneNumber == null
                || phoneNumber.isBlank()) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .PHONE_NUMBER_REQUIRED
            );
        }

        String name =
                getValueByHeader(
                        row,
                        headerIndexMap,
                        CampaignContactConstants
                                .EXCEL_NAME_HEADER,
                        formatter,
                        evaluator
                );

        if (name == null
                || name.isBlank()) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .NAME_REQUIRED
            );
        }

        String externalReference = null;

        Map<String, Object> customData =
                new LinkedHashMap<>();

        for (String variable : promptVariables) {

            String value =
                    getValueByHeader(
                            row,
                            headerIndexMap,
                            variable,
                            formatter,
                            evaluator
                    );

            if (value == null
                    || value.isBlank()) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .EXCEL_REQUIRED_VARIABLE_COLUMN
                                + " Value is missing for: "
                                + variable
                );
            }

            if (isNameVariable(variable)) {

                name = value;

            } else if (
                    isExternalReferenceVariable(
                            variable
                    )
            ) {

                externalReference = value;

            } else if (
                    variable.startsWith(
                            CUSTOM_DATA_PREFIX
                    )
            ) {

                String customDataKey =
                        variable.substring(
                                CUSTOM_DATA_PREFIX.length()
                        );

                if (!customDataKey.isBlank()) {

                    customData.put(
                            customDataKey,
                            value
                    );
                }

            } else {

                /*
                 * Root-level prompt variables are stored in
                 * Campaign Contact customData.
                 *
                 * Example:
                 * {{language}}
                 *
                 * Excel:
                 * language
                 *
                 * Stored as:
                 * {"language":"Marathi"}
                 */
                customData.put(
                        variable,
                        value
                );
            }
        }

        return CreateCampaignContactRequest
                .builder()
                .campaignPublicId(
                        campaignPublicId
                )
                .name(name)
                .phoneNumber(phoneNumber)
                .externalReference(
                        externalReference
                )
                .priority(null)
                .description(null)
                .customData(
                        serializeCustomData(
                                customData
                        )
                )
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmptyRow(
            Row row,
            List<String> headers,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        if (row == null) {
            return true;
        }

        for (
                int columnIndex = 0;
                columnIndex < headers.size();
                columnIndex++
        ) {

            Cell cell =
                    row.getCell(
                            columnIndex,
                            Row.MissingCellPolicy
                                    .RETURN_BLANK_AS_NULL
                    );

            String value =
                    getCellValue(
                            cell,
                            formatter,
                            evaluator
                    );

            if (value != null
                    && !value.isBlank()) {

                return false;
            }
        }

        return true;
    }

    private void validateDuplicateHeaders(
            List<String> headers) {

        Set<String> uniqueHeaders =
                new LinkedHashSet<>();

        for (String header : headers) {

            if (header == null
                    || header.isBlank()) {
                continue;
            }

            if (!uniqueHeaders.add(header)) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .EXCEL_DUPLICATE_HEADER
                );
            }
        }
    }

    private void validatePhoneNumberHeader(
            List<String> headers) {

        if (!headers.contains(
                CampaignContactConstants
                        .EXCEL_PHONE_NUMBER_HEADER
        )) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_PHONE_COLUMN_REQUIRED
            );
        }
    }

    private void validateRequiredPromptVariables(
            List<String> headers,
            Set<String> promptVariables) {

        for (String variable : promptVariables) {

            if (!headers.contains(variable)) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .EXCEL_REQUIRED_VARIABLE_COLUMN
                                + " Missing column: "
                                + variable
                );
            }
        }
    }

    private void validateUnexpectedHeaders(
            List<String> headers,
            Set<String> promptVariables) {

        Set<String> allowedHeaders =
                new LinkedHashSet<>();

        allowedHeaders.add(
                CampaignContactConstants
                        .EXCEL_PHONE_NUMBER_HEADER
        );

        allowedHeaders.add(
                CampaignContactConstants
                        .EXCEL_NAME_HEADER
        );

        allowedHeaders.addAll(
                promptVariables
        );

        for (String header : headers) {

            if (header == null
                    || header.isBlank()) {

                continue;
            }

            if (!allowedHeaders.contains(header)) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .EXCEL_UNEXPECTED_HEADER
                                + " Unexpected column: "
                                + header
                );
            }
        }
    }

    private Map<String, Integer>
    buildHeaderIndexMap(
            List<String> headers) {

        Map<String, Integer> headerIndexMap =
                new LinkedHashMap<>();

        for (
                int index = 0;
                index < headers.size();
                index++
        ) {

            String header =
                    headers.get(index);

            if (header != null
                    && !header.isBlank()) {

                headerIndexMap.put(
                        header,
                        index
                );
            }
        }

        return headerIndexMap;
    }

    private String getValueByHeader(
            Row row,
            Map<String, Integer> headerIndexMap,
            String header,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        Integer columnIndex =
                headerIndexMap.get(header);

        if (columnIndex == null) {
            return null;
        }

        Cell cell =
                row.getCell(
                        columnIndex,
                        Row.MissingCellPolicy
                                .RETURN_BLANK_AS_NULL
                );

        return getCellValue(
                cell,
                formatter,
                evaluator
        );
    }

    private String getCellValue(
            Cell cell,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        if (cell == null) {
            return null;
        }

        String value =
                formatter.formatCellValue(
                        cell,
                        evaluator
                );

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    private String normalizeHeader(
            String value) {

        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private boolean isNameVariable(
            String variable) {

        return NAME.equals(variable)
                || CONTACT_NAME.equals(variable);
    }

    private boolean isExternalReferenceVariable(
            String variable) {

        return EXTERNAL_REFERENCE.equals(variable)
                || CONTACT_EXTERNAL_REFERENCE
                .equals(variable);
    }

    private String serializeCustomData(
            Map<String, Object> customData) {

        if (customData.isEmpty()) {
            return null;
        }

        try {

            return objectMapper.writeValueAsString(
                    customData
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to serialize Campaign Contact "
                            + "custom data.",
                    exception
            );

            throw new BadRequestException(
                    CampaignContactMessages
                            .CUSTOM_DATA_INVALID
            );
        }
    }
}