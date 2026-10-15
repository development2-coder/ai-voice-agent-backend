package com.infinitio.aivoiceplatform.user.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.user.dto.request.CreateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.request.UpdateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.response.UserResponse;

/**
 * User service.
 *
 * <p>
 * Defines operations for user management.
 * Access control and tenant isolation are enforced by the
 * service implementation.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface UserService {

    /**
     * Create a user.
     *
     * @param request create user request
     * @return created user
     */
    UserResponse create(CreateUserRequest request);

    /**
     * Update a user.
     *
     * @param request update user request
     * @return updated user
     */
    UserResponse update(UpdateUserRequest request);

    /**
     * Get a user by public ID.
     *
     * @param publicId user public ID
     * @return user details
     */
    UserResponse getByPublicId(String publicId);

    /**
     * Get users.
     *
     * <p>
     * The implementation determines whether the request is made
     * by SUPER_ADMIN or an ADMIN and applies the appropriate
     * tenant scope.
     * </p>
     *
     * @param page page number
     * @param size page size
     * @return users
     */
    PageResponse<UserResponse> getAll(
            int page,
            int size
    );

    /**
     * Delete a user.
     *
     * @param publicId user public ID
     */
    void delete(String publicId);

    /**
     * Activate a user.
     *
     * @param publicId user public ID
     */
    void activate(String publicId);

    /**
     * Deactivate a user.
     *
     * @param publicId user public ID
     */
    void deactivate(String publicId);
}