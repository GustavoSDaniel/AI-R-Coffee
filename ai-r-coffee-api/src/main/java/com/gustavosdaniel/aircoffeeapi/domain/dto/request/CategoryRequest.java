package com.gustavosdaniel.aircoffeeapi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(

        @NotBlank(message = "O nome da categoria não pode estar vazio")
        String name,

        @NotBlank(message = "A descrição da categoria não pode estar vazia")
        String description) {

    public CategoryRequest {

        if (name != null) name = name.trim();

        if (description != null) description = description.trim();
    }
}
