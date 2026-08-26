package com.infinitio.aivoiceplatform.callsession.dto.request;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request used to update the lifecycle status of a call session.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCallSessionStatusRequestDto {

    @NotNull
    private CallSessionStatus status;
}