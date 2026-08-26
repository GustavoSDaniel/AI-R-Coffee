package com.gustavosdaniel.aircoffeeapi.controller;

import com.gustavosdaniel.aircoffeeapi.controller.openApi.ProductOpenApi;
import com.gustavosdaniel.aircoffeeapi.domain.dto.request.ProductRequest;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.ProductResponse;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.ProductSummary;
import com.gustavosdaniel.aircoffeeapi.service.ProductService;
import jakarta.validation.Valid;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductOpenApi {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/{categoryId}")
    public ResponseEntity<ProductResponse> crateProduct(
            @Valid @RequestBody ProductRequest request, @PathVariable UUID categoryId
            ){

        ProductResponse response = productService.createProduct(request, categoryId);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);

    }

    @GetMapping("/all")
    public ResponseEntity<Page<ProductResponse>> allProducts(
            @RequestParam(required = false) String name,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        Page<ProductResponse> responses = productService.allProducts(name, pageable);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ProductResponse>> allProductsActive(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        Page<ProductResponse> responses = productService.allProductsActive(pageable);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductSummary>> allProductsActive(
            @RequestParam(required = false) String name
    ){
        List<ProductSummary> responses = productService.searchProductActive(name);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<ProductSummary>> allProductsInactive(
            @RequestParam(required = false) String name
    ){
        List<ProductSummary> responses = productService.findProductsInactive(name);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{categoryId}/category-product")
    public ResponseEntity<Page<ProductResponse>> productsByCategory(
            @PathVariable UUID categoryId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        Page<ProductResponse> responses = productService
                .productsByCategory(pageable, categoryId);

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateProduct(@PathVariable UUID id){

        productService.activateProduct(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableProduct(@PathVariable UUID id){

        productService.disableProduct(id);

        return ResponseEntity.noContent().build();
    }
}
