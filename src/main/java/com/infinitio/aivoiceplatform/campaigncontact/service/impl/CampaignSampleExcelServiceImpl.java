package com.infinitio.aivoiceplatform.campaigncontact.service.impl;

import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignVariablesResponse;
import com.infinitio.aivoiceplatform.campaign.service.CampaignVariableService;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignSampleExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Service implementation for generating Campaign Contact
 * sample Excel files.
 *
 * <p>
 * The generated Excel contains only:
 * </p>
 *
 * <ul>
 *     <li>phone_number as the mandatory contact identifier</li>
 *     <li>Variables actually used by the Campaign Flow prompts</li>
 * </ul>
 *
 * <p>
 * Standard optional Campaign Contact fields such as name,
 * external_reference, priority and description are not added
 * automatically. They are included only when the selected
 * Flow prompt explicitly uses the corresponding variable.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignSampleExcelServiceImpl
        implements CampaignSampleExcelService {

    /**
     * Excel sheet name.
     */
    private static final String SHEET_NAME =
            "Campaign Contacts";

    /**
     * Mandatory phone number column.
     */
    private static final String PHONE_NUMBER_HEADER =
            CampaignContactConstants
                    .EXCEL_PHONE_NUMBER_HEADER;

    private final CampaignVariableService
            campaignVariableService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource generateSampleExcel(
            String campaignPublicId) {

        log.info(
                "Generating Campaign Contact sample Excel. "
                        + "Campaign : {}",
                campaignPublicId
        );

        CampaignVariablesResponse variablesResponse =
                campaignVariableService.getVariables(
                        campaignPublicId
                );

        List<String> headers =
                buildHeaders(
                        variablesResponse.getVariables()
                );

        try (
                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet(
                            SHEET_NAME
                    );

            createHeaderRow(
                    sheet,
                    headers
            );

            createEmptyDataRow(
                    sheet,
                    headers.size()
            );

            autoSizeColumns(
                    sheet,
                    headers.size()
            );

            workbook.write(
                    outputStream
            );

            log.info(
                    "Campaign Contact sample Excel generated. "
                            + "Campaign : {}, Flow : {}, "
                            + "Columns : {}",
                    campaignPublicId,
                    variablesResponse.getFlowPublicId(),
                    headers.size()
            );

            return new ByteArrayResource(
                    outputStream.toByteArray()
            );

        } catch (IOException exception) {

            log.error(
                    "Failed to generate Campaign Contact "
                            + "sample Excel. Campaign : {}",
                    campaignPublicId,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to generate Campaign Contact "
                            + "sample Excel.",
                    exception
            );
        }
    }

    /**
     * Builds the Excel headers.
     *
     * <p>
     * The phone number is always the first and only
     * automatically added column. All remaining columns
     * come from variables extracted from the Flow prompts.
     * </p>
     *
     * @param variables variables extracted from Flow prompts
     * @return ordered unique headers
     */
    private List<String> buildHeaders(
            List<String> variables) {

        Set<String> headers =
                new LinkedHashSet<>();

        /*
         * phone_number and name are mandatory standard Campaign Contact columns.
         * Additional columns are generated from variables used by the Campaign Flow prompt.
         */
        headers.add(
                PHONE_NUMBER_HEADER
        );

        headers.add(
                CampaignContactConstants
                        .EXCEL_NAME_HEADER
        );

        if (variables == null) {

            return new ArrayList<>(
                    headers
            );
        }

        for (String variable : variables) {

            if (variable == null
                    || variable.isBlank()) {

                continue;
            }

            String normalizedVariable =
                    variable.trim();

            /*
             * phone_number must not be duplicated if someone
             * accidentally uses it as a prompt variable.
             */
            if (PHONE_NUMBER_HEADER.equals(
                    normalizedVariable
            )) {

                continue;
            }

            headers.add(
                    normalizedVariable
            );
        }

        return new ArrayList<>(
                headers
        );
    }

    /**
     * Creates the Excel header row.
     *
     * @param sheet Excel sheet
     * @param headers Excel headers
     */
    private void createHeaderRow(
            Sheet sheet,
            List<String> headers) {

        Row headerRow =
                sheet.createRow(0);

        CellStyle headerStyle =
                sheet.getWorkbook()
                        .createCellStyle();

        for (
                int index = 0;
                index < headers.size();
                index++
        ) {

            Cell cell =
                    headerRow.createCell(
                            index
                    );

            cell.setCellValue(
                    headers.get(index)
            );

            cell.setCellStyle(
                    headerStyle
            );
        }
    }

    /**
     * Creates an empty data row.
     *
     * @param sheet Excel sheet
     * @param columnCount number of columns
     */
    private void createEmptyDataRow(
            Sheet sheet,
            int columnCount) {

        Row dataRow =
                sheet.createRow(1);

        for (
                int index = 0;
                index < columnCount;
                index++
        ) {

            dataRow.createCell(index)
                    .setCellValue("");
        }
    }

    /**
     * Adjusts Excel column widths.
     *
     * @param sheet Excel sheet
     * @param columnCount number of columns
     */
    private void autoSizeColumns(
            Sheet sheet,
            int columnCount) {

        for (
                int index = 0;
                index < columnCount;
                index++
        ) {

            sheet.autoSizeColumn(index);
        }
    }
}