package com.gustavosdaniel.aircoffeeapi.domain.dto.request;

import com.gustavosdaniel.aircoffeeapi.domain.enums.UnitMeasure;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "O nome do produto não pode estar vazio")
        String name,

        @NotBlank(message = "A descrição do produto não pode estar vazia")
        String description,

        @NotNull(message = "A quantidade é obrigatória")
        @PositiveOrZero(message = "Não é possível adicionar uma quantidade negativa")
        Integer quantity,

        @NotNull(message = "O tipo de unidade é obrigatório")
        UnitMeasure unitMeasure,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "Não é possível adicionar um valor negativo, nem zero")
        @Digits(integer = 7, fraction = 2)
        BigDecimal price,

        @NotBlank(message = "A imagem do produto é obrigatória")
        String imageUrl) {

    public ProductRequest {

        if (name != null) name = name.trim();

        if (description != null) description = description.trim();

        if (imageUrl != null) imageUrl = imageUrl.trim();
    }
}
