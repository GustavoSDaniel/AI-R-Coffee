package com.gustavosdaniel.aircoffeeapi.controller;

import com.gustavosdaniel.aircoffeeapi.controller.openApi.CheckoutOpenApi;
import com.gustavosdaniel.aircoffeeapi.domain.dto.request.CheckoutRequest;
import com.gustavosdaniel.aircoffeeapi.service.CheckoutService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController implements CheckoutOpenApi {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> createSession(
            @Valid @RequestBody CheckoutRequest request
    ) throws StripeException {

        String url = checkoutService.createCheckoutSession(request);

        return ResponseEntity.ok(Map.of("url", url));
    }
}
