package com.gustavosdaniel.aircoffeeapi.service;

import com.gustavosdaniel.aircoffeeapi.domain.dto.request.ProductRequest;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.ProductResponse;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.ProductSummary;
import com.gustavosdaniel.aircoffeeapi.domain.mapping.ProductMapper;
import com.gustavosdaniel.aircoffeeapi.domain.po.Category;
import com.gustavosdaniel.aircoffeeapi.domain.po.Product;
import com.gustavosdaniel.aircoffeeapi.exception.CategoryNotFoundException;
import com.gustavosdaniel.aircoffeeapi.exception.NameExistException;
import com.gustavosdaniel.aircoffeeapi.exception.ProductNotFoundException;
import com.gustavosdaniel.aircoffeeapi.repository.CategoryRepository;
import com.gustavosdaniel.aircoffeeapi.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, UUID categoryId){

        log.info("Criando um novo Produto");

        existsProduct(request.name());

        Category category = getCategoryOrThrow(categoryId);

        Product newProduct = productMapper.toProduct(request, category);

        Product saveProduct = productRepository.save(newProduct);

        log.info("Produto: {}, criado com sucesso", saveProduct.getId());

        return productMapper.toResponse(saveProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> allProducts(String name, Pageable pageable){

        log.info("Buscando todos os produtos cadastrados");

        String searchTerm = (name == null) ? "" : name;

        Page<Product> products = productRepository.searchByNameAll(searchTerm, pageable);

        log.info("Total de produtos {}", products.getTotalElements());

        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> allProductsActive(Pageable pageable){

        log.info("Buscando todos os produtos ativos");

        Page<Product> products = productRepository.findByActiveTrue(pageable);

        log.info("Total de produtos ativos {}", products.getTotalElements());

        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> productsByCategory(Pageable pageable, UUID categoryId){

        log.info("Buscando produto pela categoria");

        Category category = getCategoryOrThrow(categoryId);

        Page<Product> products = productRepository.findAllByCategory(category, pageable);

        log.info("Total de produtos {} encontrados pela categoria: {}",
                products.getTotalElements(), category.getName());

        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductSummary> searchProductActive(String name){

        String searchTerm = (name == null) ? "" : name;

        log.info("Buscando produtos ativos pelo nome {}", searchTerm);

        List<Product> products = productRepository.searchActiveByName(searchTerm);

        return products.stream().map(productMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSummary> findProductsInactive(String name){

        log.info("Buscando por produtos inativos");

        String searchTerm = (name == null) ? "" : name;

        List<Product> products = productRepository.searchInactiveByName(searchTerm);

        log.info("Total de produtos inativos encontrados {}", products.size());

        return products.stream().map(productMapper::toSummary).toList();

    }

    @Transactional
    public void activateProduct(UUID id){

        log.info("Ativando Produto");

        Product product = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);

        product.activate();

        productRepository.save(product);

        log.info("Produto: {}, ativado com sucesso", product.getName());
    }

    @Transactional
    public void disableProduct(UUID id){

        log.info("Desativando Produto");

        Product product = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);

        product.deactivate();

        productRepository.save(product);

        log.info("Produto: {}, desativado com sucesso", product.getName());
    }

    private Category getCategoryOrThrow(UUID categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    private void existsProduct(String name){

        if (productRepository.existsByNameIgnoreCase(name)) throw new NameExistException();
    }
}
