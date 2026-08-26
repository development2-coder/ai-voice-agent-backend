package com.infinitio.aivoiceplatform.user.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.user.dto.request.CreateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.request.UpdateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.response.UserResponse;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse update(UpdateUserRequest request);

    UserResponse getByPublicId(String publicId);

    PageResponse<UserResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}