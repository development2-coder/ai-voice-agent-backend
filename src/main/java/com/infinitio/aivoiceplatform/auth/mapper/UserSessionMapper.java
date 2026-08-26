package com.infinitio.aivoiceplatform.auth.mapper;

import com.infinitio.aivoiceplatform.auth.dto.response.UserSessionResponse;
import com.infinitio.aivoiceplatform.auth.entity.UserSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSessionMapper {

    UserSessionResponse toResponse(
            UserSession entity
    );
}