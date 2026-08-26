package com.gustavosdaniel.aircoffeeapi.domain.mapping;

import com.gustavosdaniel.aircoffeeapi.domain.dto.request.ProductRequest;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.ProductResponse;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.ProductSummary;
import com.gustavosdaniel.aircoffeeapi.domain.po.Category;
import com.gustavosdaniel.aircoffeeapi.domain.po.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toProduct(ProductRequest request, Category category){

        return new Product(

                request.name(),
                request.description(),
                request.quantity(),
                request.unitMeasure(),
                request.price(),
                request.imageUrl(),
                category
        );
    }

    public ProductResponse toResponse(Product product){

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getQuantity(),
                product.getUnitMeasure(),
                product.getPrice(),
                product.getImageUrl(),
                product.getCategory().getId()
        );
    }

    public ProductSummary toSummary(Product product) {

        return new ProductSummary(
                product.getId(),
                product.getName(),
                product.getImageUrl()
        );
    }
}
