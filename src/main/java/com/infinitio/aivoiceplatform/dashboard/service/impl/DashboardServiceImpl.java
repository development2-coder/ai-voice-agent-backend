package com.infinitio.aivoiceplatform.dashboard.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.campaign.constant.CampaignConstants;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.repository.CampaignRepository;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.repository.CampaignContactRepository;
import com.infinitio.aivoiceplatform.dashboard.dto.response.ActiveCampaignResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.CallActivityResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.RecentCallResponse;
import com.infinitio.aivoiceplatform.dashboard.dto.response.TotalCallsResponse;
import com.infinitio.aivoiceplatform.dashboard.service.DashboardService;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of dashboard service.
 *
 * <p>
 * Provides tenant-aware dashboard information including
 * total calls, active calls, call activity, recent calls
 * and active campaigns.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl
        implements DashboardService {

    private static final Integer NOT_DELETED = 0;

    private final CallRepository callRepository;

    private final CampaignRepository campaignRepository;

    private final CampaignContactRepository
            campaignContactRepository;

    private final CurrentUserService
            currentUserService;

    /**
     * Returns total calls and currently active calls
     * accessible to the logged-in user.
     *
     * @return total and active call counts
     */
    @Override
    public TotalCallsResponse getTotalCalls() {

        List<Call> calls =
                getAccessibleCalls();

        long activeCalls =
                calls.stream()
                        .filter(this::isActiveCall)
                        .count();

        return TotalCallsResponse.builder()
                .totalCalls(
                        calls.size()
                )
                .activeCalls(
                        activeCalls
                )
                .build();
    }

    /**
     * Returns call activity for the requested number
     * of previous days.
     *
     * @param days number of days
     * @return call activity
     */
    @Override
    public List<CallActivityResponse> getCallActivity(
            int days) {

        int requestedDays =
                Math.min(
                        Math.max(
                                days,
                                1
                        ),
                        31
                );

        LocalDate today =
                LocalDate.now();

        LocalDate fromDate =
                today.minusDays(
                        requestedDays - 1L
                );

        Map<LocalDate, Long> counts =
                getAccessibleCalls()
                        .stream()
                        .map(
                                this::resolveCallTimestamp
                        )
                        .filter(
                                timestamp ->
                                        timestamp != null
                        )
                        .map(
                                LocalDateTime::toLocalDate
                        )
                        .filter(
                                date ->
                                        !date.isBefore(
                                                fromDate
                                        )
                                                && !date.isAfter(
                                                today
                                        )
                        )
                        .collect(
                                Collectors.groupingBy(
                                        Function.identity(),
                                        Collectors.counting()
                                )
                        );

        List<CallActivityResponse> response =
                new ArrayList<>();

        for (
                int index = 0;
                index < requestedDays;
                index++
        ) {

            LocalDate date =
                    fromDate.plusDays(
                            index
                    );

            response.add(
                    CallActivityResponse.builder()
                            .date(
                                    date
                            )
                            .day(
                                    date.getDayOfWeek()
                                            .getDisplayName(
                                                    TextStyle.SHORT,
                                                    Locale.ENGLISH
                                            )
                            )
                            .calls(
                                    counts.getOrDefault(
                                            date,
                                            0L
                                    )
                            )
                            .build()
            );
        }

        return response;
    }

    /**
     * Returns the latest calls accessible to
     * the logged-in user.
     *
     * @param limit maximum number of calls
     * @return recent calls
     */
    @Override
    public List<RecentCallResponse> getRecentCalls(
            int limit) {

        int requestedLimit =
                Math.min(
                        Math.max(
                                limit,
                                1
                        ),
                        20
                );

        return getAccessibleCalls()
                .stream()
                .sorted(
                        Comparator.comparing(
                                this::resolveCallTimestamp,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .limit(
                        requestedLimit
                )
                .map(
                        this::toRecentCallResponse
                )
                .toList();
    }

    /**
     * Returns active campaigns accessible to
     * the logged-in user.
     *
     * @return active campaigns
     */
    @Override
    public List<ActiveCampaignResponse>
    getActiveCampaigns() {

        User currentUser =
                currentUserService
                        .getCurrentUser();

        boolean superAdmin =
                currentUser.getRole() != null
                        && RoleConstants.SUPER_ADMIN
                        .equalsIgnoreCase(
                                currentUser
                                        .getRole()
                                        .getRoleCode()
                        );

        Long tenantId =
                superAdmin
                        || currentUser.getTenant() == null
                        ? null
                        : currentUser
                        .getTenant()
                        .getId();

        return campaignRepository
                .findByIsDeleted(
                        NOT_DELETED
                )
                .stream()
                .filter(
                        campaign ->
                                CampaignConstants
                                        .STATUS_ACTIVE
                                        .equalsIgnoreCase(
                                                campaign.getStatus()
                                        )
                )
                .filter(
                        campaign ->
                                superAdmin
                                        || isCampaignInTenant(
                                        campaign,
                                        tenantId
                                )
                )
                .sorted(
                        Comparator.comparing(
                                Campaign::getUpdatedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .map(
                        this::toActiveCampaignResponse
                )
                .toList();
    }

    /**
     * Returns calls accessible to the current user.
     *
     * <p>
     * Super Admin can access all calls.
     * Other users can access only calls belonging
     * to their tenant.
     * </p>
     *
     * @return accessible calls
     */
    private List<Call> getAccessibleCalls() {

        User currentUser =
                currentUserService
                        .getCurrentUser();

        String roleCode =
                currentUser.getRole() == null
                        ? null
                        : currentUser
                        .getRole()
                        .getRoleCode();

        if (
                RoleConstants.SUPER_ADMIN
                        .equalsIgnoreCase(
                                roleCode
                        )
        ) {

            return callRepository
                    .findAllByIsDeletedOrderByCreatedAtDesc(
                            NOT_DELETED
                    );
        }

        if (currentUser.getTenant() == null) {

            return List.of();
        }

        return callRepository
                .findAllByTenantIdAndIsDeleted(
                        currentUser
                                .getTenant()
                                .getId(),
                        NOT_DELETED
                );
    }

    /**
     * Determines whether a call is currently active.
     *
     * @param call call entity
     * @return true when call is active
     */
    private boolean isActiveCall(
            Call call) {

        if (call.getStatus() == null) {
            return false;
        }

        String status =
                call.getStatus()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return !status.equals("COMPLETED")
                && !status.equals("ENDED")
                && !status.equals("FAILED")
                && !status.equals("BUSY")
                && !status.equals("NO_ANSWER")
                && !status.equals("REJECTED")
                && !status.equals("CANCELLED");
    }

    /**
     * Resolves the timestamp used by dashboard
     * call statistics.
     *
     * @param call call entity
     * @return call timestamp
     */
    private LocalDateTime resolveCallTimestamp(
            Call call) {

        if (call.getStartedAt() != null) {

            return call.getStartedAt();
        }

        return call.getCreatedAt();
    }

    /**
     * Converts Call entity into recent call response.
     *
     * @param call call entity
     * @return recent call response
     */
    private RecentCallResponse
    toRecentCallResponse(
            Call call) {

        String caller =
                call.getDirection() != null
                        && "OUTBOUND"
                        .equalsIgnoreCase(
                                call.getDirection()
                        )
                        ? call.getToNumber()
                        : call.getFromNumber();

        String agent = "-";

        if (
                call.getCampaignContact() != null
                        && call.getCampaignContact()
                        .getCampaign() != null
                        && call.getCampaignContact()
                        .getCampaign()
                        .getAgent() != null
        ) {

            agent =
                    call.getCampaignContact()
                            .getCampaign()
                            .getAgent()
                            .getAgentName();
        }

        return RecentCallResponse.builder()
                .publicId(
                        call.getPublicId()
                )
                .caller(
                        caller
                )
                .agent(
                        agent
                )
                .durationSeconds(
                        call.getDurationSeconds()
                )
                .status(
                        call.getStatus()
                )
                .timestamp(
                        resolveCallTimestamp(
                                call
                        )
                )
                .build();
    }

    /**
     * Checks whether a campaign belongs to
     * the requested tenant.
     *
     * @param campaign campaign
     * @param tenantId tenant ID
     * @return true when campaign belongs to tenant
     */
    private boolean isCampaignInTenant(
            Campaign campaign,
            Long tenantId) {

        return tenantId != null
                && campaign.getAgent() != null
                && campaign.getAgent().getTenant() != null
                && tenantId.equals(
                campaign
                        .getAgent()
                        .getTenant()
                        .getId()
        );
    }

    /**
     * Converts active campaign into dashboard response.
     *
     * @param campaign campaign
     * @return active campaign response
     */
    private ActiveCampaignResponse
    toActiveCampaignResponse(
            Campaign campaign) {

        long totalContacts =
                campaignContactRepository
                        .countByCampaignIdAndIsDeleted(
                                campaign.getId(),
                                NOT_DELETED
                        );

        long pendingContacts =
                campaignContactRepository
                        .countByCampaignIdAndStatusAndIsDeleted(
                                campaign.getId(),
                                CampaignContactConstants
                                        .STATUS_PENDING,
                                NOT_DELETED
                        );

        long attemptedContacts =
                Math.max(
                        0,
                        totalContacts
                                - pendingContacts
                );

        int progress =
                totalContacts == 0
                        ? 0
                        : (int) Math.round(
                        (attemptedContacts * 100.0)
                                / totalContacts
                );

        return ActiveCampaignResponse.builder()
                .publicId(
                        campaign.getPublicId()
                )
                .name(
                        campaign.getCampaignName()
                )
                .progress(
                        Math.min(
                                progress,
                                100
                        )
                )
                .totalContacts(
                        totalContacts
                )
                .attemptedContacts(
                        attemptedContacts
                )
                .build();
    }
}