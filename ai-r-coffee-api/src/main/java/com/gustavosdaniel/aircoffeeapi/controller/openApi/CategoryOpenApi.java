package com.gustavosdaniel.aircoffeeapi.controller.openApi;

import com.gustavosdaniel.aircoffeeapi.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.CategoryResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Categories", description = "Endpoints para gerenciamento de categorias de produtos")
public interface CategoryOpenApi {

    @Operation(
            summary = "Criar uma nova categoria",
            description = "Cria uma nova categoria com os dados fornecidos. Retorna a categoria criada com seu ID e status ativo.",
            method = "POST",
            tags = {"Categories"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoria criada com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryResponse.class),
                            examples = @ExampleObject(
                                    name = "Categoria criada",
                                    value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "name": "Bebidas Quentes",
                        "active": true
                    }
                    """
                            )
                    ),
                    headers = @Header(name = "Location", description = "URL da nova categoria", schema = @Schema(type = "string"))
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
                        "path": "/api/v1/categories"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado - token ausente ou inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - permissões insuficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito - já existe categoria com o mesmo nome",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2024-01-01T12:00:00Z",
                        "status": 409,
                        "error": "Conflict",
                        "message": "Categoria 'Bebidas Quentes' já cadastrada",
                        "path": "/api/v1/categories"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content
            )
    })
    ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    );


    @Operation(
            summary = "Listar todas as categorias (inclui inativas)",
            description = "Retorna todas as categorias cadastradas, opcionalmente filtradas por nome (busca parcial, case-insensitive). Endpoint público.",
            method = "GET",
            tags = {"Categories"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorias retornada com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryResponse.class),
                            examples = @ExampleObject(
                                    name = "Lista de categorias",
                                    value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "name": "Bebidas Quentes",
                            "active": true
                        },
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174001",
                            "name": "Bebidas Geladas",
                            "active": false
                        }
                    ]
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<List<CategoryResponse>> allCategoryByName(
            @Parameter(
                    name = "name",
                    description = "Filtro parcial pelo nome da categoria (opcional)",
                    in = ParameterIn.QUERY,
                    required = false,
                    example = "Bebidas"
            )
            @RequestParam(required = false) String name
    );


    @Operation(
            summary = "Listar categorias ativas",
            description = "Retorna somente categorias com status ativo, opcionalmente filtradas por nome. Endpoint público.",
            method = "GET",
            tags = {"Categories"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorias ativas",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "name": "Bebidas Quentes",
                            "active": true
                        }
                    ]
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<List<CategoryResponse>> allCategoryByNameActive(
            @Parameter(
                    name = "name",
                    description = "Filtro parcial pelo nome da categoria (opcional)",
                    in = ParameterIn.QUERY,
                    required = false,
                    example = "Quentes"
            )
            @RequestParam(required = false) String name
    );


    @Operation(
            summary = "Listar categorias inativas",
            description = "Retorna somente categorias com status inativo, opcionalmente filtradas por nome.",
            method = "GET",
            tags = {"Categories"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorias inativas",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174001",
                            "name": "Bebidas Geladas",
                            "active": false
                        }
                    ]
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<List<CategoryResponse>> allCategoryByNameInactive(
            @Parameter(
                    name = "name",
                    description = "Filtro parcial pelo nome da categoria (opcional)",
                    in = ParameterIn.QUERY,
                    required = false,
                    example = "Geladas"
            )
            @RequestParam(required = false) String name
    );


    @Operation(
            summary = "Ativar uma categoria",
            description = "Define o status da categoria para 'ativo'. Se a categoria não existir, retorna 404.",
            method = "PATCH",
            tags = {"Categories"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoria ativada com sucesso (sem conteúdo no corpo)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido (formato UUID incorreto)",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada para o ID fornecido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2024-01-01T12:00:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "Categoria com ID 123e4567-e89b-12d3-a456-426614174000 não encontrada",
                        "path": "/api/v1/categories/123e4567-e89b-12d3-a456-426614174000/activate"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "Categoria já está ativa", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<Void> activateCategory(
            @Parameter(
                    name = "id",
                    description = "UUID da categoria a ser ativada",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id
    );


    @Operation(
            summary = "Desativar uma categoria",
            description = "Define o status da categoria para 'inativo'. Se a categoria não existir, retorna 404.",
            method = "PATCH",
            tags = {"Categories"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoria desativada com sucesso (sem conteúdo no corpo)"
            ),
            @ApiResponse(responseCode = "400", description = "ID inválido (formato UUID incorreto)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada para o ID fornecido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2024-01-01T12:00:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "Categoria com ID 123e4567-e89b-12d3-a456-426614174000 não encontrada",
                        "path": "/api/v1/categories/123e4567-e89b-12d3-a456-426614174000/disable"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "Categoria já está inativa", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    ResponseEntity<Void> disableCategory(
            @Parameter(
                    name = "id",
                    description = "UUID da categoria a ser desativada",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id
    );
}