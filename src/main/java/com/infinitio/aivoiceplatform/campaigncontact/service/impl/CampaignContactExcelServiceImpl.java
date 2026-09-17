package com.infinitio.aivoiceplatform.campaigncontact.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignVariablesResponse;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.service.CampaignVariableService;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactMessages;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CampaignContactExcelConfirmRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelPreviewResponse;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelPreviewRowResponse;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelUploadResponse;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.campaigncontact.mapper.CampaignContactMapper;
import com.infinitio.aivoiceplatform.campaigncontact.repository.CampaignContactRepository;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactExcelPreviewService;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactExcelService;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignExcelService;
import com.infinitio.aivoiceplatform.campaigncontact.validator.CampaignContactValidator;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of Campaign Contact Excel Service.
 *
 * <p>
 * Coordinates Campaign Contact Excel upload, preview and
 * confirmation. Excel-specific parsing is delegated to the
 * dedicated Excel services.
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

    private static final String CUSTOM_DATA_PREFIX =
            "contact.customData.";

    private final CampaignValidator campaignValidator;

    private final CampaignContactValidator
            campaignContactValidator;

    private final CampaignContactRepository
            campaignContactRepository;

    private final CampaignContactMapper
            campaignContactMapper;

    private final CurrentUserService
            currentUserService;

    private final CampaignVariableService
            campaignVariableService;

    private final CampaignExcelService
            campaignExcelService;

    private final CampaignContactExcelPreviewService
            campaignContactExcelPreviewService;

    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public CampaignContactExcelUploadResponse upload(
            String campaignPublicId,
            MultipartFile file) {

        Campaign campaign =
                campaignValidator.validateAndGet(
                        campaignPublicId
                );

        validateFile(file);

        Set<String> promptVariables =
                resolvePromptVariables(
                        campaignVariableService.getVariables(
                                campaignPublicId
                        )
                );

        List<String> errors =
                new ArrayList<>();

        int totalRows = 0;
        int importedRows = 0;

        try (
                Workbook workbook =
                        WorkbookFactory.create(
                                file.getInputStream()
                        )
        ) {

            validateWorkbook(workbook);

            var sheet =
                    workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            FormulaEvaluator evaluator =
                    workbook
                            .getCreationHelper()
                            .createFormulaEvaluator();

            List<String> headers =
                    campaignExcelService.readHeaders(
                            sheet.getRow(0),
                            formatter,
                            evaluator
                    );

            campaignExcelService.validateHeaders(
                    headers,
                    promptVariables
            );

            for (
                    int rowIndex = 1;
                    rowIndex <= sheet.getLastRowNum();
                    rowIndex++
            ) {

                var row =
                        sheet.getRow(rowIndex);

                if (campaignExcelService.isEmptyRow(
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
                            campaignExcelService.mapRow(
                                    campaignPublicId,
                                    row,
                                    headers,
                                    promptVariables,
                                    formatter,
                                    evaluator
                            );

                    saveContact(
                            request,
                            campaign
                    );

                    importedRows++;

                } catch (Exception exception) {

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

        return buildUploadResponse(
                campaignPublicId,
                totalRows,
                importedRows,
                errors.size(),
                errors
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CampaignContactExcelPreviewResponse preview(
            String campaignPublicId,
            MultipartFile file) {

        return campaignContactExcelPreviewService.preview(
                campaignPublicId,
                file
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CampaignContactExcelUploadResponse confirm(
            CampaignContactExcelConfirmRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_INVALID
            );
        }

        String campaignPublicId =
                request.getCampaignPublicId();

        Campaign campaign =
                campaignValidator.validateAndGet(
                        campaignPublicId
                );

        List<CampaignContactExcelPreviewRowResponse>
                rows =
                request.getRows();

        if (rows == null
                || rows.isEmpty()) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_EMPTY
            );
        }

        Set<String> promptVariables =
                resolvePromptVariables(
                        campaignVariableService.getVariables(
                                campaignPublicId
                        )
                );

        List<String> errors =
                new ArrayList<>();

        List<CreateCampaignContactRequest>
                requests =
                new ArrayList<>();

        Set<String> phoneNumbers =
                new LinkedHashSet<>();

        /*
         * Validate every row before saving any row.
         */
        for (
                CampaignContactExcelPreviewRowResponse row :
                rows
        ) {

            if (row == null) {

                errors.add(
                        "Invalid empty preview row."
                );

                continue;
            }

            List<String> missingFields =
                    findMissingFields(
                            row.getValues(),
                            promptVariables
                    );

            if (!missingFields.isEmpty()) {

                errors.add(
                        "Row "
                                + row.getRowNumber()
                                + ": Missing values: "
                                + String.join(
                                ", ",
                                missingFields
                        )
                );

                continue;
            }

            try {

                CreateCampaignContactRequest contactRequest =
                        buildContactRequest(
                                campaignPublicId,
                                row.getValues(),
                                promptVariables
                        );

                String phoneNumber =
                        contactRequest.getPhoneNumber();

                if (!phoneNumbers.add(
                        phoneNumber
                )) {

                    throw new BadRequestException(
                            CampaignContactMessages
                                    .PHONE_ALREADY_EXISTS
                    );
                }

                campaignContactValidator
                        .validateForCreate(
                                contactRequest,
                                campaign.getId()
                        );

                requests.add(
                        contactRequest
                );

            } catch (Exception exception) {

                errors.add(
                        "Row "
                                + row.getRowNumber()
                                + ": "
                                + resolveErrorMessage(
                                exception
                        )
                );
            }
        }

        /*
         * Do not partially import the Excel.
         */
        if (!errors.isEmpty()) {

            return buildUploadResponse(
                    campaignPublicId,
                    rows.size(),
                    0,
                    errors.size(),
                    errors
            );
        }

        /*
         * Every row is valid.
         * Save all contacts.
         */
        for (
                CreateCampaignContactRequest contactRequest :
                requests
        ) {

            saveContactWithoutValidation(
                    contactRequest,
                    campaign
            );
        }

        log.info(
                "Campaign Contact Excel confirmation completed. "
                        + "Campaign : {}, Imported : {}",
                campaignPublicId,
                requests.size()
        );

        return buildUploadResponse(
                campaignPublicId,
                rows.size(),
                requests.size(),
                0,
                new ArrayList<>()
        );
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

            if (!isPhoneVariable(
                    normalized
            )) {

                variables.add(
                        normalized
                );
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

    private List<String> findMissingFields(
            Map<String, String> values,
            Set<String> promptVariables) {

        List<String> missingFields =
                new ArrayList<>();

        String phoneNumber =
                values == null
                        ? null
                        : values.get(
                        CampaignContactConstants
                                .EXCEL_PHONE_NUMBER_HEADER
                );

        if (phoneNumber == null
                || phoneNumber.isBlank()) {

            missingFields.add(
                    CampaignContactConstants
                            .EXCEL_PHONE_NUMBER_HEADER
            );
        }

        String name =
                values == null
                        ? null
                        : values.get(
                        CampaignContactConstants
                                .EXCEL_NAME_HEADER
                );

        if (name == null
                || name.isBlank()) {

            missingFields.add(
                    CampaignContactConstants
                            .EXCEL_NAME_HEADER
            );
        }

        for (String variable :
                promptVariables) {

            String value =
                    values == null
                            ? null
                            : values.get(variable);

            if (value == null
                    || value.isBlank()) {

                missingFields.add(variable);
            }
        }

        return missingFields;
    }

    private CreateCampaignContactRequest
    buildContactRequest(
            String campaignPublicId,
            Map<String, String> values,
            Set<String> promptVariables) {

        String phoneNumber =
                values.get(
                        CampaignContactConstants
                                .EXCEL_PHONE_NUMBER_HEADER
                );

        String name =
                values.get(
                        CampaignContactConstants
                                .EXCEL_NAME_HEADER
                );

        if (name == null
                || name.isBlank()) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .NAME_REQUIRED
            );
        }

        String externalReference =
                values.get(
                        CampaignContactConstants
                                .EXCEL_EXTERNAL_REFERENCE_HEADER
                );

        Map<String, Object> customData =
                new LinkedHashMap<>();

        for (String variable :
                promptVariables) {

            String value =
                    values.get(variable);

            if (value == null
                    || value.isBlank()) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .EXCEL_REQUIRED_VARIABLE_COLUMN
                                + " Value is missing for: "
                                + variable
                );
            }

            if ("name".equals(variable)
                    || "contact.name"
                    .equals(variable)) {

                name = value;

            } else if (
                    "externalReference"
                            .equals(variable)
                            || "contact.externalReference"
                            .equals(variable)
            ) {

                externalReference =
                        value;

            } else if (
                    variable.startsWith(
                            CUSTOM_DATA_PREFIX
                    )
            ) {

                String key =
                        variable.substring(
                                CUSTOM_DATA_PREFIX
                                        .length()
                        );

                if (!key.isBlank()) {

                    customData.put(
                            key,
                            value
                    );
                }

            } else {

                /*
                 * Root-level variables such as
                 * {{language}} are stored as customData.
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
                .phoneNumber(
                        phoneNumber.trim()
                )
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

    private String serializeCustomData(
            Map<String, Object> customData) {

        if (customData.isEmpty()) {
            return null;
        }

        try {

            return objectMapper
                    .writeValueAsString(
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

    private void saveContact(
            CreateCampaignContactRequest request,
            Campaign campaign) {

        campaignContactValidator
                .validateForCreate(
                        request,
                        campaign.getId()
                );

        saveContactWithoutValidation(
                request,
                campaign
        );
    }

    private void saveContactWithoutValidation(
            CreateCampaignContactRequest request,
            Campaign campaign) {

        CampaignContact contact =
                campaignContactMapper
                        .toEntity(request);

        contact.setCreatedBy(
                currentUserService
                        .getCurrentUserId()
        );

        contact.setCampaign(
                campaign
        );

        CampaignContact savedContact =
                campaignContactRepository
                        .save(contact);

        log.debug(
                "Campaign Contact imported successfully. "
                        + "Public Id : {}",
                savedContact.getPublicId()
        );
    }

    private CampaignContactExcelUploadResponse
    buildUploadResponse(
            String campaignPublicId,
            int totalRows,
            int importedRows,
            int failedRows,
            List<String> errors) {

        return CampaignContactExcelUploadResponse
                .builder()
                .campaignPublicId(
                        campaignPublicId
                )
                .totalRows(totalRows)
                .importedRows(
                        importedRows
                )
                .failedRows(
                        failedRows
                )
                .errors(errors)
                .build();
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
                        java.util.Locale.ROOT
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

        var sheet =
                workbook.getSheetAt(0);

        if (sheet == null
                || sheet.getPhysicalNumberOfRows()
                <= 1) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .EXCEL_FILE_EMPTY
            );
        }
    }

    private String resolveErrorMessage(
            Exception exception) {

        if (exception.getMessage() != null
                && !exception.getMessage().isBlank()) {

            return exception.getMessage();
        }

        return CampaignContactMessages
                .EXCEL_FILE_INVALID;
    }
}