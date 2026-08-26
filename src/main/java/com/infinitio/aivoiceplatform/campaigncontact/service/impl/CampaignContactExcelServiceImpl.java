package com.infinitio.aivoiceplatform.campaigncontact.service.impl;

import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactMessages;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelUploadResponse;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.campaigncontact.mapper.CampaignContactMapper;
import com.infinitio.aivoiceplatform.campaigncontact.repository.CampaignContactRepository;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactExcelService;
import com.infinitio.aivoiceplatform.campaigncontact.validator.CampaignContactValidator;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service implementation for Campaign Contact Excel upload.
 *
 * <p>
 * Supports standard campaign contact columns together with
 * dynamic campaign-specific columns.
 * </p>
 *
 * <p>
 * Standard columns:
 * phone_number,
 * name,
 * external_reference,
 * priority,
 * description.
 * </p>
 *
 * <p>
 * Any additional column is stored inside customData so that
 * different campaign types can use different Excel columns.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignContactExcelServiceImpl
        implements CampaignContactExcelService {

    private static final int HEADER_ROW_INDEX = 0;

    private final CampaignValidator campaignValidator;

    private final CampaignContactValidator
            campaignContactValidator;

    private final CampaignContactRepository
            campaignContactRepository;

    private final CampaignContactMapper
            campaignContactMapper;

    private final ObjectMapper objectMapper;

    /**
     * Uploads campaign contacts from an Excel file.
     *
     * @param campaignPublicId campaign public identifier
     * @param file Excel file
     * @return upload result
     */
    @Override
    public CampaignContactExcelUploadResponse upload(
            String campaignPublicId,
            MultipartFile file) {

        log.info(
                "Starting Campaign Contact Excel upload. "
                        + "Campaign : {}",
                campaignPublicId
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        campaignPublicId
                );

        validateFile(file);

        List<String> errors =
                new ArrayList<>();

        int totalRows = 0;
        int importedRows = 0;
        int failedRows = 0;

        try (
                Workbook workbook =
                        WorkbookFactory.create(
                                file.getInputStream()
                        )
        ) {

            if (workbook.getNumberOfSheets() == 0) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .EXCEL_FILE_EMPTY
                );
            }

            Sheet sheet =
                    workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() <= 1) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .EXCEL_FILE_EMPTY
                );
            }

            DataFormatter formatter =
                    new DataFormatter();

            FormulaEvaluator evaluator =
                    workbook
                            .getCreationHelper()
                            .createFormulaEvaluator();

            List<String> headers =
                    readHeaders(
                            sheet.getRow(
                                    HEADER_ROW_INDEX
                            ),
                            formatter,
                            evaluator
                    );

            validateHeaders(headers);

            Map<String, Integer> headerIndexMap =
                    buildHeaderIndexMap(
                            headers
                    );

            for (
                    int rowIndex = 1;
                    rowIndex <= sheet.getLastRowNum();
                    rowIndex++
            ) {

                Row row =
                        sheet.getRow(rowIndex);

                if (isEmptyRow(
                        row,
                        headers,
                        formatter,
                        evaluator
                )) {

                    continue;
                }

                totalRows++;

                try {

                    CreateCampaignContactRequest request =
                            buildContactRequest(
                                    campaignPublicId,
                                    row,
                                    headers,
                                    headerIndexMap,
                                    formatter,
                                    evaluator
                            );

                    saveContact(
                            request,
                            campaign
                    );

                    importedRows++;

                } catch (Exception exception) {

                    failedRows++;

                    errors.add(
                            "Row "
                                    + (rowIndex + 1)
                                    + ": "
                                    + resolveErrorMessage(
                                    exception
                            )
                    );
                }
            }

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Campaign Contact Excel upload failed. "
                            + "Campaign : {}",
                    campaignPublicId,
                    exception
            );

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_INVALID
            );
        }

        log.info(
                "Campaign Contact Excel upload completed. "
                        + "Campaign : {}, Total : {}, "
                        + "Imported : {}, Failed : {}",
                campaignPublicId,
                totalRows,
                importedRows,
                failedRows
        );

        return CampaignContactExcelUploadResponse
                .builder()
                .campaignPublicId(
                        campaignPublicId
                )
                .totalRows(
                        totalRows
                )
                .importedRows(
                        importedRows
                )
                .failedRows(
                        failedRows
                )
                .errors(
                        errors
                )
                .build();
    }

    /**
     * Saves a campaign contact without calling
     * CampaignContactService.
     *
     * <p>
     * This is intentional because CampaignContactService
     * already depends on CampaignContactExcelService.
     * Calling it here would create a circular dependency.
     * </p>
     */
    private void saveContact(
            CreateCampaignContactRequest request,
            Campaign campaign) {

        campaignContactValidator.validateForCreate(
                request,
                campaign.getId()
        );

        CampaignContact contact =
                campaignContactMapper.toEntity(
                        request
                );

        contact.setCampaign(
                campaign
        );

        CampaignContact savedContact =
                campaignContactRepository.save(
                        contact
                );

        log.debug(
                "Campaign Contact imported successfully. "
                        + "Public Id : {}",
                savedContact.getPublicId()
        );
    }

    /**
     * Validates uploaded Excel file.
     *
     * @param file uploaded file
     */
    private void validateFile(
            MultipartFile file) {

        if (file == null
                || file.isEmpty()) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_REQUIRED
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null
                || fileName.isBlank()) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_TYPE_NOT_SUPPORTED
            );
        }

        String lowerFileName =
                fileName.toLowerCase(
                        Locale.ROOT
                );

        if (!lowerFileName.endsWith(".xlsx")
                && !lowerFileName.endsWith(".xls")) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_TYPE_NOT_SUPPORTED
            );
        }
    }

    /**
     * Reads Excel headers.
     *
     * @param headerRow header row
     * @param formatter Excel formatter
     * @param evaluator formula evaluator
     * @return normalized headers
     */
    private List<String> readHeaders(
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
     * Validates Excel headers.
     *
     * @param headers Excel headers
     */
    private void validateHeaders(
            List<String> headers) {

        List<String> uniqueHeaders =
                new ArrayList<>();

        for (String header : headers) {

            if (header == null
                    || header.isBlank()) {

                continue;
            }

            if (uniqueHeaders.contains(
                    header
            )) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .EXCEL_DUPLICATE_HEADER
                );
            }

            uniqueHeaders.add(
                    header
            );
        }

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

    /**
     * Creates header-to-column mapping.
     *
     * @param headers Excel headers
     * @return header index map
     */
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

    /**
     * Builds contact request from an Excel row.
     *
     * @param campaignPublicId campaign public identifier
     * @param row Excel row
     * @param headers headers
     * @param headerIndexMap header mapping
     * @param formatter Excel formatter
     * @param evaluator formula evaluator
     * @return contact request
     */
    private CreateCampaignContactRequest
    buildContactRequest(
            String campaignPublicId,
            Row row,
            List<String> headers,
            Map<String, Integer> headerIndexMap,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

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

        String externalReference =
                getValueByHeader(
                        row,
                        headerIndexMap,
                        CampaignContactConstants
                                .EXCEL_EXTERNAL_REFERENCE_HEADER,
                        formatter,
                        evaluator
                );

        String priorityValue =
                getValueByHeader(
                        row,
                        headerIndexMap,
                        CampaignContactConstants
                                .EXCEL_PRIORITY_HEADER,
                        formatter,
                        evaluator
                );

        Integer priority =
                parsePriority(
                        priorityValue
                );

        String description =
                getValueByHeader(
                        row,
                        headerIndexMap,
                        CampaignContactConstants
                                .EXCEL_DESCRIPTION_HEADER,
                        formatter,
                        evaluator
                );

        Map<String, Object> customData =
                new LinkedHashMap<>();

        for (String header : headers) {

            if (header == null
                    || header.isBlank()
                    || isStandardHeader(header)) {

                continue;
            }

            Integer columnIndex =
                    headerIndexMap.get(
                            header
                    );

            if (columnIndex == null) {

                continue;
            }

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

                customData.put(
                        header,
                        value
                );
            }
        }

        String customDataJson = null;

        if (!customData.isEmpty()) {

            try {

                customDataJson =
                        objectMapper.writeValueAsString(
                                customData
                        );

            } catch (Exception exception) {

                log.error(
                        "Failed to convert Excel custom data "
                                + "to JSON.",
                        exception
                );

                throw new BadRequestException(
                        CampaignContactMessages
                                .CUSTOM_DATA_INVALID
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
                .priority(priority)
                .description(description)
                .customData(
                        customDataJson
                )
                .build();
    }

    /**
     * Determines whether a header is a standard field.
     *
     * @param header normalized header
     * @return true if standard field
     */
    private boolean isStandardHeader(
            String header) {

        return CampaignContactConstants
                .EXCEL_PHONE_NUMBER_HEADER
                .equals(header)

                || CampaignContactConstants
                .EXCEL_NAME_HEADER
                .equals(header)

                || CampaignContactConstants
                .EXCEL_EXTERNAL_REFERENCE_HEADER
                .equals(header)

                || CampaignContactConstants
                .EXCEL_PRIORITY_HEADER
                .equals(header)

                || CampaignContactConstants
                .EXCEL_DESCRIPTION_HEADER
                .equals(header);
    }

    /**
     * Gets a value from an Excel row by header.
     *
     * @param row Excel row
     * @param headerIndexMap header mapping
     * @param header header name
     * @param formatter formatter
     * @param evaluator evaluator
     * @return cell value
     */
    private String getValueByHeader(
            Row row,
            Map<String, Integer> headerIndexMap,
            String header,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        Integer columnIndex =
                headerIndexMap.get(
                        header
                );

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

    /**
     * Reads an Excel cell as String.
     *
     * @param cell Excel cell
     * @param formatter formatter
     * @param evaluator evaluator
     * @return cell value
     */
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

    /**
     * Checks whether Excel row is empty.
     *
     * @param row Excel row
     * @param headers headers
     * @param formatter formatter
     * @param evaluator evaluator
     * @return true if empty
     */
    private boolean isEmptyRow(
            Row row,
            List<String> headers,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        if (row == null) {

            return true;
        }

        for (
                int index = 0;
                index < headers.size();
                index++
        ) {

            Cell cell =
                    row.getCell(
                            index,
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

    /**
     * Converts priority value to Integer.
     *
     * @param priorityValue Excel priority
     * @return priority
     */
    private Integer parsePriority(
            String priorityValue) {

        if (priorityValue == null
                || priorityValue.isBlank()) {

            return null;
        }

        try {

            return Integer.valueOf(
                    priorityValue.trim()
            );

        } catch (NumberFormatException exception) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .PRIORITY_INVALID
            );
        }
    }

    /**
     * Normalizes Excel header.
     *
     * @param header original header
     * @return normalized header
     */
    private String normalizeHeader(
            String header) {

        if (header == null) {

            return null;
        }

        return header
                .trim()
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[\\s\\-]+",
                        "_"
                );
    }

    /**
     * Resolves row processing error.
     *
     * @param exception exception
     * @return error message
     */
    private String resolveErrorMessage(
            Exception exception) {

        if (exception.getMessage() != null
                && !exception
                .getMessage()
                .isBlank()) {

            return exception.getMessage();
        }

        return CampaignContactMessages
                .EXCEL_FILE_INVALID;
    }
}