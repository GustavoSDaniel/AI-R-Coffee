package com.gustavosdaniel.aircoffeeapi.controller;

import com.gustavosdaniel.aircoffeeapi.controller.openApi.CategoryOpenApi;
import com.gustavosdaniel.aircoffeeapi.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.aircoffeeapi.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController implements CategoryOpenApi {

    private final CategoryService categoryService;


    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    ){

        CategoryResponse response = categoryService.createCategory(request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponse>> allCategoryByName(
            @RequestParam(required = false) String name){

        List<CategoryResponse> responseList = categoryService.searchAllByName(name);

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> allCategoryByNameActive(
            @RequestParam(required = false) String name){

        List<CategoryResponse> responseList = categoryService.searchCategoryActive(name);

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<CategoryResponse>> allCategoryByNameInactive(
            @RequestParam(required = false) String name){

        List<CategoryResponse> responseList = categoryService.searchCategoryInactive(name);

        return ResponseEntity.ok(responseList);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCategory(@PathVariable UUID id){

         categoryService.activateCategory(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableCategory(@PathVariable UUID id){

        categoryService.disableCategory(id);

        return ResponseEntity.noContent().build();
    }
}
