package com.infinitio.aivoiceplatform.dashboard.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.dashboard.dto.response.ActiveCampaignResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.CallActivityResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.RecentCallResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.TotalCallsResponse;
import com.infinitio.aivoiceplatform.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(
        name = "Dashboard",
        description = "Dashboard analytics APIs"
)
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get Total Calls")
    @GetMapping("/total-calls")
    public ResponseEntity<ApiResponse<TotalCallsResponse>>
    getTotalCalls() {

        return ResponseBuilder.success(
                dashboardService.getTotalCalls(),
                "Total calls fetched successfully."
        );
    }

    @Operation(summary = "Get Call Activity")
    @GetMapping("/call-activity")
    public ResponseEntity<ApiResponse<List<CallActivityResponse>>>
    getCallActivity(
            @RequestParam(defaultValue = "7") int days) {

        return ResponseBuilder.success(
                dashboardService.getCallActivity(days),
                "Call activity fetched successfully."
        );
    }

    @Operation(summary = "Get Recent Calls")
    @GetMapping("/recent-calls")
    public ResponseEntity<ApiResponse<List<RecentCallResponse>>>
    getRecentCalls(
            @RequestParam(defaultValue = "4") int limit) {

        return ResponseBuilder.success(
                dashboardService.getRecentCalls(limit),
                "Recent calls fetched successfully."
        );
    }

    @Operation(summary = "Get Active Campaigns")
    @GetMapping("/active-campaigns")
    public ResponseEntity<ApiResponse<List<ActiveCampaignResponse>>>
    getActiveCampaigns() {

        return ResponseBuilder.success(
                dashboardService.getActiveCampaigns(),
                "Active campaigns fetched successfully."
        );
    }
}