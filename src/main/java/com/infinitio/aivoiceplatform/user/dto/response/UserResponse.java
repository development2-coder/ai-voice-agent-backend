package com.infinitio.aivoiceplatform.user.dto.response;

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
public class UserResponse {

    private String publicId;

    private String tenantPublicId;

    private String organizationPublicId;

    private String rolePublicId;

    private String username;

    private String email;

    private String firstName;

    private String middleName;

    private String lastName;

    private String fullName;

    private String mobileNumber;

    private String designation;

    private String department;

    private String profileImage;

    private Boolean emailVerified;

    private Boolean mobileVerified;

    private Integer isActive;
}