package com.gustavosdaniel.aircoffeeapi.domain.dto.response;

import com.gustavosdaniel.aircoffeeapi.domain.enums.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(

        UUID id,
        String email,
        String userName,
        UserRole role
) {
}
