package com.gustavosdaniel.aircoffeeapi.domain.dto.response;

import com.gustavosdaniel.aircoffeeapi.domain.enums.UnitMeasure;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(

        UUID id,
        String name,
        String description,
        Integer quantity,
        UnitMeasure unitMeasure,
        BigDecimal price,
        String imageUrl,
        UUID categoryId
) {
}
