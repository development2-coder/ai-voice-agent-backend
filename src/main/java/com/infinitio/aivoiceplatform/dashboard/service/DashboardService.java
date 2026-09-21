package com.infinitio.aivoiceplatform.dashboard.service;

import com.infinitio.aivoiceplatform.dashboard.dto.response.ActiveCampaignResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.CallActivityResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.RecentCallResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.TotalCallsResponse;

import java.util.List;

public interface DashboardService {

    TotalCallsResponse getTotalCalls();

    List<CallActivityResponse> getCallActivity(int days);

    List<RecentCallResponse> getRecentCalls(int limit);

    List<ActiveCampaignResponse> getActiveCampaigns();
}