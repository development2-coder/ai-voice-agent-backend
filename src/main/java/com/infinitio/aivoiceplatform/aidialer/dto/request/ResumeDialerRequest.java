package com.infinitio.aivoiceplatform.aidialer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDialerRequest {

    @NotBlank(message = "Dialer public ID is required")
    private String dialerPublicId;
}