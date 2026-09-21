package com.infinitio.aivoiceplatform.campaigncontact.service.impl;

import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignVariablesResponse;
import com.infinitio.aivoiceplatform.campaign.service.CampaignVariableService;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactMessages;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelPreviewResponse;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelPreviewRowResponse;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactExcelPreviewService;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignExcelService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of Campaign Contact Excel preview service.
 *
 * <p>
 * Generates a preview of Campaign Contact Excel data and validates
 * required campaign contact fields before upload.
 * </p>
 *
 * <p>
 * Standard contact fields such as phone number and name are handled
 * by the standard Excel columns:
 * </p>
 *
 * <ul>
 *     <li>phone_number</li>
 *     <li>name</li>
 * </ul>
 *
 * <p>
 * Only campaign contact custom-data variables are treated as
 * dynamically required Excel columns.
 * </p>
 *
 * <p>
 * Runtime Flow variables such as:
 * </p>
 *
 * <ul>
 *     <li>customer_input</li>
 *     <li>customer_response</li>
 *     <li>ai_response</li>
 *     <li>endConversation</li>
 * </ul>
 *
 * <p>
 * are intentionally excluded from Excel validation because their
 * values are generated during call execution.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignContactExcelPreviewServiceImpl
        implements CampaignContactExcelPreviewService {

    private static final int HEADER_ROW_INDEX = 0;

    private final CampaignValidator campaignValidator;

    private final CampaignVariableService
            campaignVariableService;

    private final CampaignExcelService
            campaignExcelService;

    /**
     * {@inheritDoc}
     */
    @Override
    public CampaignContactExcelPreviewResponse preview(
            String campaignPublicId,
            MultipartFile file) {

        log.info(
                "Starting Campaign Contact Excel preview. "
                        + "Campaign : {}",
                campaignPublicId
        );

        campaignValidator.validateAndGet(
                campaignPublicId
        );

        validateFile(file);

        CampaignVariablesResponse variablesResponse =
                campaignVariableService.getVariables(
                        campaignPublicId
                );

        /*
         * Resolve only variables which actually belong
         * to Campaign Contact Excel data.
         */
        Set<String> promptVariables =
                resolvePromptVariables(
                        variablesResponse
                );

        try (
                Workbook workbook =
                        WorkbookFactory.create(
                                file.getInputStream()
                        )
        ) {

            validateWorkbook(workbook);

            Sheet sheet =
                    workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            FormulaEvaluator evaluator =
                    workbook
                            .getCreationHelper()
                            .createFormulaEvaluator();

            /*
             * Read headers from the uploaded Excel.
             */
            List<String> headers =
                    campaignExcelService.readHeaders(
                            sheet.getRow(
                                    HEADER_ROW_INDEX
                            ),
                            formatter,
                            evaluator
                    );

            /*
             * Validate mandatory and dynamic headers.
             *
             * promptVariables contains only:
             *
             * contact.customData.*
             *
             * Standard phone_number and name fields are
             * handled by CampaignExcelService.
             */
            campaignExcelService.validateHeaders(
                    headers,
                    promptVariables
            );

            List<CampaignContactExcelPreviewRowResponse>
                    rows =
                    buildPreviewRows(
                            sheet,
                            headers,
                            promptVariables,
                            formatter,
                            evaluator
                    );

            int validRows =
                    (int) rows.stream()
                            .filter(
                                    CampaignContactExcelPreviewRowResponse
                                            ::isValid
                            )
                            .count();

            int invalidRows =
                    rows.size() - validRows;

            log.info(
                    "Campaign Contact Excel preview completed. "
                            + "Campaign : {}, Total : {}, "
                            + "Valid : {}, Invalid : {}",
                    campaignPublicId,
                    rows.size(),
                    validRows,
                    invalidRows
            );

            return CampaignContactExcelPreviewResponse
                    .builder()
                    .campaignPublicId(
                            campaignPublicId
                    )
                    .headers(headers)
                    .rows(rows)
                    .totalRows(rows.size())
                    .validRows(validRows)
                    .invalidRows(invalidRows)
                    .readyForUpload(
                            !rows.isEmpty()
                                    && invalidRows == 0
                    )
                    .build();

        } catch (BadRequestException exception) {

            throw exception;

        } catch (IOException exception) {

            log.error(
                    "Campaign Contact Excel preview failed. "
                            + "Campaign : {}",
                    campaignPublicId,
                    exception
            );

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_INVALID
            );
        }
    }

    /**
     * Builds preview rows from the uploaded Excel sheet.
     *
     * @param sheet Excel sheet
     * @param headers Excel headers
     * @param promptVariables required campaign variables
     * @param formatter Excel formatter
     * @param evaluator formula evaluator
     * @return preview rows
     */
    private List<CampaignContactExcelPreviewRowResponse>
    buildPreviewRows(
            Sheet sheet,
            List<String> headers,
            Set<String> promptVariables,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        List<CampaignContactExcelPreviewRowResponse>
                rows =
                new ArrayList<>();

        Map<String, Integer> headerIndexes =
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

            if (campaignExcelService.isEmptyRow(
                    row,
                    headers,
                    formatter,
                    evaluator
            )) {
                continue;
            }

            Map<String, String> values =
                    readRowValues(
                            row,
                            headers,
                            headerIndexes,
                            formatter,
                            evaluator
                    );

            List<String> missingFields =
                    findMissingFields(
                            values,
                            promptVariables
                    );

            rows.add(
                    CampaignContactExcelPreviewRowResponse
                            .builder()
                            .rowNumber(
                                    rowIndex + 1
                            )
                            .values(values)
                            .missingFields(
                                    missingFields
                            )
                            .valid(
                                    missingFields.isEmpty()
                            )
                            .build()
            );
        }

        return rows;
    }

    /**
     * Builds a mapping between Excel header names and
     * their column indexes.
     *
     * @param headers Excel headers
     * @return header index map
     */
    private Map<String, Integer>
    buildHeaderIndexMap(
            List<String> headers) {

        Map<String, Integer> indexes =
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

                indexes.put(
                        header.trim(),
                        index
                );
            }
        }

        return indexes;
    }

    /**
     * Reads all values from one Excel row.
     *
     * @param row Excel row
     * @param headers Excel headers
     * @param headerIndexes header index map
     * @param formatter Excel formatter
     * @param evaluator formula evaluator
     * @return row values
     */
    private Map<String, String> readRowValues(
            Row row,
            List<String> headers,
            Map<String, Integer> headerIndexes,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        Map<String, String> values =
                new LinkedHashMap<>();

        for (String header : headers) {

            if (header == null
                    || header.isBlank()) {

                continue;
            }

            String normalizedHeader =
                    header.trim();

            Integer columnIndex =
                    headerIndexes.get(
                            normalizedHeader
                    );

            Cell cell =
                    columnIndex == null
                            ? null
                            : row.getCell(
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

            values.put(
                    normalizedHeader,
                    value == null
                            ? ""
                            : value
            );
        }

        return values;
    }

    /**
     * Finds missing required fields for a row.
     *
     * <p>
     * Standard fields are always required:
     * </p>
     *
     * <ul>
     *     <li>phone_number</li>
     *     <li>name</li>
     * </ul>
     *
     * <p>
     * Dynamic required fields are only
     * contact.customData.* variables.
     * </p>
     *
     * @param values row values
     * @param promptVariables required prompt variables
     * @return missing fields
     */
    private List<String> findMissingFields(
            Map<String, String> values,
            Set<String> promptVariables) {

        List<String> missingFields =
                new ArrayList<>();

        /*
         * Standard campaign contact fields.
         */
        addMissingField(
                missingFields,
                CampaignContactConstants
                        .EXCEL_PHONE_NUMBER_HEADER,
                values
        );

        addMissingField(
                missingFields,
                CampaignContactConstants
                        .EXCEL_NAME_HEADER,
                values
        );

        /*
         * Dynamic campaign custom-data fields.
         */
        for (String variable :
                promptVariables) {

            /*
             * Defensive check.
             *
             * Standard contact variables should never
             * reach this collection, but this prevents
             * duplicate validation if they do.
             */
            if (isStandardContactVariable(
                    variable
            )) {

                continue;
            }

            /*
             * Only custom-data variables belong here.
             */
            if (!isContactCustomDataVariable(
                    variable
            )) {

                continue;
            }

            addMissingField(
                    missingFields,
                    variable,
                    values
            );
        }

        return missingFields;
    }

    /**
     * Adds a field to the missing-field list when its
     * value is empty.
     *
     * @param missingFields missing-field collection
     * @param field field name
     * @param values row values
     */
    private void addMissingField(
            List<String> missingFields,
            String field,
            Map<String, String> values) {

        String value =
                values.get(field);

        if (value == null
                || value.isBlank()) {

            missingFields.add(field);
        }
    }

    /**
     * Reads a cell value as text.
     *
     * @param cell Excel cell
     * @param formatter Excel formatter
     * @param evaluator formula evaluator
     * @return formatted cell value
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
     * Resolves only variables which must be supplied
     * through Campaign Contact Excel.
     *
     * <p>
     * Example accepted variables:
     * </p>
     *
     * <pre>
     * contact.customData.loan_number
     * contact.customData.emi_amount
     * contact.customData.due_date
     * contact.customData.loan_type
     * contact.customData.days_overdue
     * </pre>
     *
     * <p>
     * The following are intentionally excluded:
     * </p>
     *
     * <pre>
     * contact.name
     * contact.mobile_number
     * phone_number
     * name
     * customer_input
     * customer_response
     * ai_response
     * endConversation
     * </pre>
     *
     * @param response campaign variables response
     * @return required Excel campaign variables
     */
    private Set<String> resolvePromptVariables(
            CampaignVariablesResponse response) {

        Set<String> variables =
                new LinkedHashSet<>();

        if (response == null
                || response.getVariables() == null) {

            return variables;
        }

        for (String variable :
                response.getVariables()) {

            if (variable == null
                    || variable.isBlank()) {

                continue;
            }

            String normalized =
                    variable.trim();

            /*
             * phone_number and name are standard
             * Campaign Contact fields.
             *
             * They must NOT be treated as dynamic
             * prompt variables.
             */
            if (isStandardContactVariable(
                    normalized
            )) {

                continue;
            }

            /*
             * Only contact.customData.* variables
             * are supplied through Campaign Excel.
             */
            if (!isContactCustomDataVariable(
                    normalized
            )) {

                continue;
            }

            variables.add(
                    normalized
            );
        }

        return variables;
    }

    private boolean isStandardContactVariable(
            String variable) {

        if (variable == null
                || variable.isBlank()) {

            return false;
        }

        String normalized =
                variable.trim();

        return "phone_number".equals(normalized)
                || "phoneNumber".equals(normalized)
                || "mobile_number".equals(normalized)
                || "mobileNumber".equals(normalized)
                || "contact.phoneNumber".equals(normalized)
                || "contact.phone_number".equals(normalized)
                || "contact.mobileNumber".equals(normalized)
                || "contact.mobile_number".equals(normalized)
                || "name".equals(normalized)
                || "contact.name".equals(normalized);
    }

    /**
     * Determines whether a variable represents one of
     * the standard Campaign Contact fields.
     *
     * <p>
     * These fields are mapped to the standard Excel
     * columns instead of creating duplicate columns.
     * </p>
     *
     * @param variable variable name
     * @return true when variable is a standard contact field
     */

    /**
     * Determines whether a variable belongs to
     * Campaign Contact custom data.
     *
     * <p>
     * Only these variables should become dynamic
     * Campaign Contact Excel columns.
     * </p>
     *
     * @param variable variable name
     * @return true when variable is contact custom data
     */
    private boolean isContactCustomDataVariable(
            String variable) {

        return variable != null
                && variable.startsWith(
                "contact.customData."
        )
                && variable.length()
                > "contact.customData.".length();
    }

    /**
     * Validates uploaded Excel file.
     *
     * @param file uploaded Excel file
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
     * Validates workbook structure.
     *
     * @param workbook Excel workbook
     */
    private void validateWorkbook(
            Workbook workbook) {

        if (workbook == null
                || workbook.getNumberOfSheets() == 0) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_EMPTY
            );
        }

        Sheet sheet =
                workbook.getSheetAt(0);

        if (sheet == null
                || sheet.getPhysicalNumberOfRows() <= 1) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_EMPTY
            );
        }
    }
}