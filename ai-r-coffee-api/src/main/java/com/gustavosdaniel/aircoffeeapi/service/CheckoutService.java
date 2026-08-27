package com.gustavosdaniel.aircoffeeapi.service;

import com.gustavosdaniel.aircoffeeapi.domain.dto.request.CheckoutRequest;
import com.gustavosdaniel.aircoffeeapi.domain.po.Product;
import com.gustavosdaniel.aircoffeeapi.exception.BusinessRuleException;
import com.gustavosdaniel.aircoffeeapi.repository.ProductRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class CheckoutService {

    @Value("${stripe.frontend-url}")
    private String frontendUrl;

    private final ProductRepository productRepository;

    public CheckoutService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String createCheckoutSession(CheckoutRequest request) throws StripeException {

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/checkout/sucesso?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/checkout/cancelado");

        for (CheckoutRequest.CartItem item : request.items()) {

            Product product = getAndValidateProduct(item);
            SessionCreateParams.LineItem lineItem = createStripeLineItem(product, item.quantity());
            paramsBuilder.addLineItem(lineItem);
        }

        Session session = Session.create(paramsBuilder.build());
        return session.getUrl();
    }

    private Product getAndValidateProduct(CheckoutRequest.CartItem item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new BusinessRuleException("Produto não encontrado: " + item.productId()));

        if (!product.isActive()) {
            throw new BusinessRuleException("Produto indisponível: " + product.getName());
        }

        if (product.getQuantity() < item.quantity()) {
            throw new BusinessRuleException("Estoque insuficiente para: " + product.getName());
        }

        return product;
    }

    private SessionCreateParams.LineItem createStripeLineItem(Product product, Integer quantity) {
        long unitAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        return SessionCreateParams.LineItem.builder()
                .setQuantity(quantity.longValue())
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("brl")
                                .setUnitAmount(unitAmount)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(product.getName())
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
