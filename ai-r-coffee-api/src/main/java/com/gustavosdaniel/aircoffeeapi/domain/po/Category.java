package com.gustavosdaniel.aircoffeeapi.domain.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity{

    public Category(){}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}