package com.infinitio.aivoiceplatform.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentCallResponse {

    private String publicId;

    private String caller;

    private String agent;

    private Integer durationSeconds;

    private String status;

    private LocalDateTime timestamp;
}