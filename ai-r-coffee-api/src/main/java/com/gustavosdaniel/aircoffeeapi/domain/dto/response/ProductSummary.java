package com.gustavosdaniel.aircoffeeapi.domain.dto.response;

import java.util.UUID;

public record ProductSummary(

        UUID id,
        String name,
        String imageUrl
) {
}
