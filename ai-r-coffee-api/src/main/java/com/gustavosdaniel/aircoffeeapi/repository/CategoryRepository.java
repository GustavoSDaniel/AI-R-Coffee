package com.gustavosdaniel.aircoffeeapi.repository;

import com.gustavosdaniel.aircoffeeapi.domain.po.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameIgnoreCase(String name);

    @Query("""
            SELECT c FROM Category c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%') ) 
            """
    )
    List<Category> searchByName(@Param("name") String name);

    @Query("""
        SELECT c FROM Category c
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) 
        AND c.active = true
        """)
    List<Category> searchActiveByName(@Param("name") String name);

    @Query("""
        SELECT c FROM Category c
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) 
        AND c.active = false
        """)
    List<Category> searchInactiveByName(@Param("name") String name);

}
