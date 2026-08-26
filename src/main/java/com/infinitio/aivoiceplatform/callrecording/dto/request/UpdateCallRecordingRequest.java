package com.infinitio.aivoiceplatform.callrecording.dto.request;

import com.infinitio.aivoiceplatform.callrecording.constant.CallRecordingConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Call Recording Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCallRecordingRequest {

    @NotBlank(message = "Public Id is required.")
    private String publicId;

    @NotBlank(message = "Call is required.")
    private String callPublicId;

    @NotBlank(message = "File name is required.")
    @Size(max = CallRecordingConstants.FILE_NAME_MAX_LENGTH)
    private String fileName;

    @NotBlank(message = "File URL is required.")
    @Size(max = CallRecordingConstants.FILE_URL_MAX_LENGTH)
    private String fileUrl;

    @NotBlank(message = "File type is required.")
    @Size(max = CallRecordingConstants.FILE_TYPE_MAX_LENGTH)
    private String fileType;

    @Size(max = CallRecordingConstants.STORAGE_PROVIDER_MAX_LENGTH)
    private String storageProvider;

    private Integer durationSeconds;

    @Size(max = CallRecordingConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}