package com.gustavosdaniel.aircoffeeapi.controller.openApi;

import com.gustavosdaniel.aircoffeeapi.domain.dto.request.ProductRequest;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.ProductResponse;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.ProductSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Endpoints para gerenciamento de produtos")
public interface ProductOpenApi {

    @Operation(
            summary = "Criar um novo produto",
            description = "Cria um novo produto associado a uma categoria existente. Retorna o produto criado com seu ID.",
            method = "POST",
            tags = {"Products"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Produto criado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponse.class),
                            examples = @ExampleObject(
                                    name = "Produto criado",
                                    value = """
                    {
                        "id": "98765432-1234-5678-9012-345678901234",
                        "name": "Café Expresso",
                        "description": "Café curto e intenso",
                        "price": 5.90,
                        "active": true,
                        "categoryId": "123e4567-e89b-12d3-a456-426614174000"
                    }
                    """
                            )
                    ),
                    headers = @Header(name = "Location", description = "URL do novo produto", schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (dados ausentes ou mal formatados)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2024-01-01T12:00:00Z",
                        "status": 400,
                        "error": "Bad Request",
                        "message": "O campo 'name' é obrigatório",
                        "path": "/api/v1/products/123e4567-e89b-12d3-a456-426614174000"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria informada não encontrada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2024-01-01T12:00:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "Categoria com ID 123e4567-e89b-12d3-a456-426614174000 não encontrada",
                        "path": "/api/v1/products/123e4567-e89b-12d3-a456-426614174000"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito - produto com mesmo nome já cadastrado na categoria",
                    content = @Content
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @Parameter(
                    name = "categoryId",
                    description = "UUID da categoria à qual o produto será associado",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID categoryId
    );

    @Operation(
            summary = "Buscar produto por ID",
            description = "Retorna um produto específico pelo seu ID. Endpoint público.",
            method = "GET",
            tags = {"Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto encontrado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponse.class),
                            examples = @ExampleObject(
                                    name = "Produto encontrado",
                                    value = """
                    {
                        "id": "98765432-1234-5678-9012-345678901234",
                        "name": "Café Expresso",
                        "description": "Café curto e intenso",
                        "price": 5.90,
                        "active": true,
                        "categoryId": "123e4567-e89b-12d3-a456-426614174000"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "ID inválido (formato UUID incorreto)", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2024-01-01T12:00:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "Produto com ID 98765432-1234-5678-9012-345678901234 não encontrado",
                        "path": "/api/v1/products/98765432-1234-5678-9012-345678901234"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<ProductResponse> findProductById(
            @Parameter(
                    name = "id",
                    description = "UUID do produto a ser buscado",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "98765432-1234-5678-9012-345678901234"
            )
            @PathVariable UUID id
    );

    @Operation(
            summary = "Listar todos os produtos (inclui inativos)",
            description = "Retorna uma página de produtos, opcionalmente filtrada por nome (busca parcial).",
            method = "GET",
            tags = {"Products"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de produtos retornada com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponse.class),
                            examples = @ExampleObject(
                                    name = "Página de produtos",
                                    value = """
                    {
                        "content": [
                            {
                                "id": "98765432-1234-5678-9012-345678901234",
                                "name": "Café Expresso",
                                "description": "Café curto e intenso",
                                "price": 5.90,
                                "active": true,
                                "categoryId": "123e4567-e89b-12d3-a456-426614174000"
                            }
                        ],
                        "pageable": {
                            "sort": {"sorted": true, "unsorted": false, "empty": false},
                            "offset": 0,
                            "pageNumber": 0,
                            "pageSize": 20,
                            "paged": true,
                            "unpaged": false
                        },
                        "totalElements": 1,
                        "totalPages": 1,
                        "last": true,
                        "size": 20,
                        "number": 0,
                        "sort": {"sorted": true, "unsorted": false, "empty": false},
                        "first": true,
                        "numberOfElements": 1,
                        "empty": false
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<Page<ProductResponse>> allProducts(
            @Parameter(
                    name = "name",
                    description = "Filtro parcial pelo nome do produto (opcional)",
                    in = ParameterIn.QUERY,
                    required = false,
                    example = "Café"
            )
            @RequestParam(required = false) String name,
            @Parameter(description = "Parâmetros de paginação e ordenação")
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    );

    @Operation(
            summary = "Listar produtos ativos",
            description = "Retorna uma página de produtos com status ativo, ordenados por nome. Endpoint público.",
            method = "GET",
            tags = {"Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de produtos ativos",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<Page<ProductResponse>> allProductsActive(
            @Parameter(description = "Parâmetros de paginação e ordenação")
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    );

    @Operation(
            summary = "Buscar produtos ativos por nome (resumo)",
            description = "Retorna uma lista de resumos de produtos ativos, opcionalmente filtrada por nome. Endpoint público.",
            method = "GET",
            tags = {"Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de resumos de produtos ativos",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductSummary.class),
                            examples = @ExampleObject(
                                    value = """
                    [
                        {
                            "id": "98765432-1234-5678-9012-345678901234",
                            "name": "Café Expresso",
                            "price": 5.90
                        }
                    ]
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<List<ProductSummary>> allProductsActiveSummary(
            @Parameter(
                    name = "name",
                    description = "Filtro parcial pelo nome do produto (opcional)",
                    in = ParameterIn.QUERY,
                    required = false,
                    example = "Café"
            )
            @RequestParam(required = false) String name
    );

    @Operation(
            summary = "Listar produtos inativos (resumo)",
            description = "Retorna uma lista de resumos de produtos inativos, opcionalmente filtrada por nome.",
            method = "GET",
            tags = {"Products"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de resumos de produtos inativos",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductSummary.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<List<ProductSummary>> allProductsInactive(
            @Parameter(
                    name = "name",
                    description = "Filtro parcial pelo nome do produto (opcional)",
                    in = ParameterIn.QUERY,
                    required = false,
                    example = "Café"
            )
            @RequestParam(required = false) String name
    );

    @Operation(
            summary = "Listar produtos por categoria",
            description = "Retorna uma página de produtos pertencentes a uma categoria específica. Endpoint público.",
            method = "GET",
            tags = {"Products"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de produtos da categoria",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada",
                    content = @Content
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<Page<ProductResponse>> productsByCategory(
            @Parameter(
                    name = "categoryId",
                    description = "UUID da categoria",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID categoryId,
            @Parameter(description = "Parâmetros de paginação e ordenação")
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    );

    @Operation(
            summary = "Ativar um produto",
            description = "Define o status do produto para 'ativo'.",
            method = "PATCH",
            tags = {"Products"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto ativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2024-01-01T12:00:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "Produto com ID 98765432-1234-5678-9012-345678901234 não encontrado",
                        "path": "/api/v1/products/98765432-1234-5678-9012-345678901234/activate"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "Produto já está ativo", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<Void> activateProduct(
            @Parameter(
                    name = "id",
                    description = "UUID do produto a ser ativado",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "98765432-1234-5678-9012-345678901234"
            )
            @PathVariable UUID id
    );

    @Operation(
            summary = "Desativar um produto",
            description = "Define o status do produto para 'inativo'.",
            method = "PATCH",
            tags = {"Products"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto desativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content
            ),
            @ApiResponse(responseCode = "409", description = "Produto já está inativo", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<Void> disableProduct(
            @Parameter(
                    name = "id",
                    description = "UUID do produto a ser desativado",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "98765432-1234-5678-9012-345678901234"
            )
            @PathVariable UUID id
    );
}