package com.gustavosdaniel.aircoffeeapi.controller.openApi;

import com.gustavosdaniel.aircoffeeapi.domain.dto.request.CheckoutRequest;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Checkout", description = "Endpoints para integração com gateway de pagamento (Stripe)")
@SecurityRequirement(name = "bearerAuth")
public interface CheckoutOpenApi {

    @Operation(
            summary = "Criar sessão de pagamento",
            description = "Recebe os itens do carrinho, valida as regras de negócio (estoque, disponibilidade) e gera uma URL segura de pagamento hospedada pela Stripe.",
            method = "POST"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sessão criada com sucesso. Retorna a URL de redirecionamento para a Stripe.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "URL do Stripe",
                                    value = """
                                    {
                                        "url": "https://checkout.stripe.com/c/pay/cs_test_a1b2c3d4e5f6g7h8..."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (Produto inativo ou estoque insuficiente)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Estoque insuficiente",
                                    value = """
                                    {
                                        "timestamp": "2026-08-27T12:00:00Z",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "Estoque insuficiente para: Café Expresso",
                                        "path": "/api/v1/checkout/session"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado (Usuário precisa fazer login antes de pagar)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado no banco de dados", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor ou falha na comunicação com a Stripe", content = @Content)
    })
    ResponseEntity<Map<String, String>> createSession(
            @Valid @RequestBody CheckoutRequest request
    ) throws StripeException;
}
