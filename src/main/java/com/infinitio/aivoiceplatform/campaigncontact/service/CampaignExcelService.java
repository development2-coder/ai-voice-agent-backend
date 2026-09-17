package com.infinitio.aivoiceplatform.campaigncontact.service;

import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Set;

/**
 * Service for Campaign Contact Excel processing.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignExcelService {

    /**
     * Reads headers from an Excel header row.
     *
     * @param headerRow Excel header row
     * @param formatter Excel data formatter
     * @param evaluator formula evaluator
     * @return normalized headers
     */
    List<String> readHeaders(
            Row headerRow,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    );

    /**
     * Validates Excel headers against Campaign prompt variables.
     *
     * @param headers Excel headers
     * @param promptVariables variables used by the Flow prompt
     */
    void validateHeaders(
            List<String> headers,
            Set<String> promptVariables
    );

    /**
     * Maps an Excel row to a Campaign Contact request.
     *
     * @param campaignPublicId campaign public identifier
     * @param row Excel row
     * @param headers Excel headers
     * @param promptVariables variables used by the Flow prompt
     * @param formatter Excel data formatter
     * @param evaluator formula evaluator
     * @return Campaign Contact request
     */
    CreateCampaignContactRequest mapRow(
            String campaignPublicId,
            Row row,
            List<String> headers,
            Set<String> promptVariables,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    );

    /**
     * Checks whether an Excel row is empty.
     *
     * @param row Excel row
     * @param headers Excel headers
     * @param formatter Excel data formatter
     * @param evaluator formula evaluator
     * @return true when the row is empty
     */
    boolean isEmptyRow(
            Row row,
            List<String> headers,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    );
}