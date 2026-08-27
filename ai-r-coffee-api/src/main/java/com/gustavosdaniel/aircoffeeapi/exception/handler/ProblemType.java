package com.gustavosdaniel.aircoffeeapi.exception.handler;

import java.net.URI;

public enum ProblemType {

    BUSINESS_RULE(

            "urn:ai-r-coffee:regra-de-negocio",
            "Violação de regra de negócio"
    ),

    VALIDATE_ERROR(

            "urn:ai-r-coffee:erro-de-validacao",
            "Validação falhou"
    ),

    NAME_EXIST(
            "urn:ai-r-coffee:nome-existe",
            "Nome já existente"
    ),

    CATEGORY_NOT_FOUND(
            "urn:ai-r-coffee:category-not-found",
            "Categoria não encontrada"
    ),

    PRODUCT_NOT_FOUND(
            "urn:ai-r-coffee:product-not-found",
            "Produto não encontrado"
    ),

    USER_NOT_FOUND(
            "urn:ai-r-coffee:user-not-found",
            "Usuário não encontrado"
    );

    private final URI uri;

    private final String title;

    ProblemType(String uri, String title) {
        this.uri = URI.create(uri);
        this.title = title;
    }

    public URI getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }
}
