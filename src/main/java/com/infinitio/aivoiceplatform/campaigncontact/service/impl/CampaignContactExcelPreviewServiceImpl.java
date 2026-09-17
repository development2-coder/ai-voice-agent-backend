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

            List<String> headers =
                    campaignExcelService.readHeaders(
                            sheet.getRow(
                                    HEADER_ROW_INDEX
                            ),
                            formatter,
                            evaluator
                    );

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
                        header,
                        index
                );
            }
        }

        return indexes;
    }

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

            Integer columnIndex =
                    headerIndexes.get(header);

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
                    header,
                    value == null
                            ? ""
                            : value
            );
        }

        return values;
    }

    private List<String> findMissingFields(
            Map<String, String> values,
            Set<String> promptVariables) {

        List<String> missingFields =
                new ArrayList<>();

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

        for (String variable :
                promptVariables) {

            if ("name".equals(variable)
                    || "contact.name".equals(variable)) {

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

            if (!isPhoneVariable(normalized)) {
                variables.add(normalized);
            }
        }

        return variables;
    }

    private boolean isPhoneVariable(
            String variable) {

        return "phone_number".equals(variable)
                || "phoneNumber".equals(variable)
                || "contact.phoneNumber"
                .equals(variable);
    }

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