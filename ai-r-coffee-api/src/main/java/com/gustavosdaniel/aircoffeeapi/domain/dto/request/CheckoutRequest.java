package com.gustavosdaniel.aircoffeeapi.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record CheckoutRequest(

        @NotEmpty(message = "O carrinho näo pode estar vazio")
        List<CartItem> items
) {
    public record CartItem(

            @NotNull(message = "O produto é obrigatório")
            UUID productId,

            @NotNull(message = "A quantidade é obrigatória")
            @Positive(message = "A quantidade precisa ser maior que zero")
            Integer quantity
    ){}
}
