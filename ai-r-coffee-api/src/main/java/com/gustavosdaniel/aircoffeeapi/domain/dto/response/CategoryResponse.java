package com.gustavosdaniel.aircoffeeapi.domain.dto.response;

import java.util.UUID;

public record CategoryResponse(

        UUID id,
        String name,
        String description
) {
}
