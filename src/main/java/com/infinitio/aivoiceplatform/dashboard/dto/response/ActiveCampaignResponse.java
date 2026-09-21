package com.infinitio.aivoiceplatform.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveCampaignResponse {

    private String publicId;

    private String name;

    private int progress;

    private long totalContacts;

    private long attemptedContacts;
}