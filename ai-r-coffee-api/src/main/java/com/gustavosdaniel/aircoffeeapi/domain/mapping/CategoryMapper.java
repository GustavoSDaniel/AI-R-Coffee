package com.gustavosdaniel.aircoffeeapi.domain.mapping;

import com.gustavosdaniel.aircoffeeapi.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.aircoffeeapi.domain.po.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toCategory(CategoryRequest request){

        return new Category(
                request.name(),
                request.description()
        );
    }

    public CategoryResponse toResponse(Category category){

        if (category == null) return null;

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

}
