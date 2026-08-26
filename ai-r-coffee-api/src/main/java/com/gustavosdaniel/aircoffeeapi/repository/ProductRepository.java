package com.gustavosdaniel.aircoffeeapi.repository;

import com.gustavosdaniel.aircoffeeapi.domain.po.Category;
import com.gustavosdaniel.aircoffeeapi.domain.po.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByNameIgnoreCase(String name);

    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%') ) 
            """
    )
    List<Product> searchByName(@Param("name") String name);

    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) 
        AND p.active = true
        """)
    List<Product> searchActiveByName(@Param("name") String name);

    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) 
        AND p.active = false
        """)
    List<Product> searchInactiveByName(@Param("name") String name);

    Page<Product> findAllByCategory(Category category, Pageable pageable);
    
    Page<Product> findAllByActive(Pageable pageable);
    
    Page<Product> findAllBy(Pageable pageable);
}
