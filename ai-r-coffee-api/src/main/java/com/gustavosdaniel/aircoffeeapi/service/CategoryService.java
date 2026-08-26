package com.gustavosdaniel.aircoffeeapi.service;

import com.gustavosdaniel.aircoffeeapi.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.aircoffeeapi.domain.mapping.CategoryMapper;
import com.gustavosdaniel.aircoffeeapi.domain.po.Category;
import com.gustavosdaniel.aircoffeeapi.exception.CategoryNotFoundException;
import com.gustavosdaniel.aircoffeeapi.exception.NameExistException;
import com.gustavosdaniel.aircoffeeapi.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request){

        existsCategory(request.name());

        Category newCategory = categoryMapper.toCategory(request);

        Category categorySaved = categoryRepository.save(newCategory);

        return categoryMapper.toResponse(categorySaved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategoryActive(String name){

        String searchTerm = (name == null) ? "" : name;

        log.info("Buscando categorias ativas pelo nome: {}", searchTerm);

        List<Category> categories = categoryRepository.searchActiveByName(searchTerm);

        log.info("Total de categorias encontrados: {}", categories.size());

        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategoryInactive(String name){

        String searchTerm = (name == null) ? "" : name;

        log.info("Buscando categorias inativas pelo nome: {}", searchTerm);

        List<Category> categories = categoryRepository.searchInactiveByName(searchTerm);

        log.info("Total de categorias encontrados: {}", categories.size());

        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> searchAllByName(String name){

        String searchTerm = (name == null) ? "" : name;

        log.info("Buscando todas as categorias");

        List<Category> categories = categoryRepository.searchByName(searchTerm);

        log.info("Total de categorias encontrados: {}", categories.size());

        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    @Transactional
    public void activateCategory(UUID id){

        log.info("Iniciando processo para ativar categoria {}", id);

        Category category = categoryRepository
                .findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        category.activate();

        categoryRepository.save(category);

        log.info("Categoria ativada com sucesso");
    }

    @Transactional
    public void disableCategory(UUID id){

        log.info("Iniciando processo para desativar a categoria {}", id);

        Category category = categoryRepository
                .findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        category.deactivate();

        categoryRepository.save(category);

        log.info("Categoria desativada com sucesso");
    }

    private void existsCategory(String name){

        if (categoryRepository.existsByNameIgnoreCase(name)){

            throw new NameExistException();
        }
    }
}
