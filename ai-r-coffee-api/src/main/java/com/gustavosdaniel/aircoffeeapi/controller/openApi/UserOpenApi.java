package com.gustavosdaniel.aircoffeeapi.controller.openApi;

import com.gustavosdaniel.aircoffeeapi.domain.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Users", description = "Endpoints para gerenciamento de usuários e perfis")
@SecurityRequirement(name = "bearerAuth")
public interface UserOpenApi {

    @Operation(
            summary = "Buscar perfil do usuário logado",
            description = "Retorna os dados do usuário atualmente autenticado via token JWT. Cria o usuário no banco local (JIT Provisioning) no primeiro acesso.",
            method = "GET"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil retornado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Usuário Logado",
                                    value = """
                                    {
                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                        "email": "gustavosdaniel@hotmail.com",
                                        "userName": "gustavosdaniel",
                                        "role": "ADMIN"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado ou token expirado", content = @Content)
    })
    ResponseEntity<UserResponse> me();

    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os detalhes de um usuário específico. Exclusivo para administradores.",
            method = "GET"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer ROLE_ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    ResponseEntity<UserResponse> findUserById(
            @Parameter(description = "UUID do usuário a ser buscado", in = ParameterIn.PATH, required = true)
            @PathVariable UUID id
    );

    @Operation(
            summary = "Buscar usuário por E-mail",
            description = "Retorna os detalhes de um usuário com base no endereço de e-mail. Exclusivo para administradores.",
            method = "GET"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer ROLE_ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    ResponseEntity<UserResponse> findUserByEmail(
            @Parameter(description = "Endereço de e-mail do usuário", in = ParameterIn.QUERY, required = true, example = "cliente@loja.com")
            @RequestParam String email
    );

    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma página de usuários, opcionalmente filtrada por nome. Exclusivo para administradores.",
            method = "GET"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de usuários retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer ROLE_ADMIN)", content = @Content)
    })
    ResponseEntity<Page<UserResponse>> allUsers(
            @Parameter(description = "Filtro parcial pelo nome do usuário", in = ParameterIn.QUERY)
            @RequestParam(required = false) String name,
            @ParameterObject
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.ASC)
            Pageable pageable
    );

    @Operation(
            summary = "Ativar conta de usuário",
            description = "Reativa a conta de um usuário inativo. Exclusivo para administradores.",
            method = "PATCH"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário ativado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer ROLE_ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    ResponseEntity<Void> activateUser(
            @Parameter(description = "UUID do usuário", in = ParameterIn.PATH, required = true)
            @PathVariable UUID id
    );

    @Operation(
            summary = "Desativar conta de usuário",
            description = "Inativa temporariamente a conta de um usuário (Soft Delete). Exclusivo para administradores.",
            method = "PATCH"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer ROLE_ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    ResponseEntity<Void> disableUser(
            @Parameter(description = "UUID do usuário", in = ParameterIn.PATH, required = true)
            @PathVariable UUID id
    );
}