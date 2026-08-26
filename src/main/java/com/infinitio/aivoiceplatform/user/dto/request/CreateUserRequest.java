package com.infinitio.aivoiceplatform.user.dto.request;

import com.infinitio.aivoiceplatform.user.constant.UserConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CreateUserRequest {

    @NotBlank(message = "Tenant is required.")
    private String tenantPublicId;

    @NotBlank(message = "Organization is required.")
    private String organizationPublicId;

    @NotBlank(message = "Role is required.")
    private String rolePublicId;

    @NotBlank(message = "Username is required.")
    @Size(
            max = UserConstants.USERNAME_MAX_LENGTH,
            message = "Username cannot exceed 100 characters."
    )
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email.")
    @Size(
            max = UserConstants.EMAIL_MAX_LENGTH,
            message = "Email cannot exceed 150 characters."
    )
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(
            min = 8,
            max = UserConstants.PASSWORD_MAX_LENGTH,
            message = "Password must be between 8 and 500 characters."
    )
    private String password;

    @NotBlank(message = "First Name is required.")
    @Size(max = UserConstants.NAME_MAX_LENGTH)
    private String firstName;

    @Size(max = UserConstants.NAME_MAX_LENGTH)
    private String middleName;

    @NotBlank(message = "Last Name is required.")
    @Size(max = UserConstants.NAME_MAX_LENGTH)
    private String lastName;

    @Size(max = UserConstants.MOBILE_MAX_LENGTH)
    private String mobileNumber;

    @Size(max = UserConstants.DESIGNATION_MAX_LENGTH)
    private String designation;

    @Size(max = UserConstants.DEPARTMENT_MAX_LENGTH)
    private String department;

    @Size(max = UserConstants.PROFILE_IMAGE_MAX_LENGTH)
    private String profileImage;
}