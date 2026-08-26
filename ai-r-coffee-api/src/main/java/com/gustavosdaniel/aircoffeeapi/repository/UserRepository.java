package com.gustavosdaniel.aircoffeeapi.repository;

import com.gustavosdaniel.aircoffeeapi.domain.po.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByKeycloakId(String keycloakId);

    @Query("SELECT * FROM users WHERE user_name ILIKE CONCAT('%', :name, '%')")
    Page<User> searchByName(String name, Pageable pageable);

    Page<User> findAllBy(Pageable pageable);
}
