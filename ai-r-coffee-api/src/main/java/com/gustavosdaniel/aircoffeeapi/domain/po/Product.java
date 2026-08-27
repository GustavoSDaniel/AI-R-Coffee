package com.gustavosdaniel.aircoffeeapi.domain.po;

import com.gustavosdaniel.aircoffeeapi.domain.enums.UnitMeasure;
import com.gustavosdaniel.aircoffeeapi.exception.BusinessRuleException;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product extends BaseEntity{

    public Product(){}

    public Product(String name, String description, Integer quantity, UnitMeasure unitMeasure, BigDecimal price, String imageUrl, Category categories) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.unitMeasure = unitMeasure;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = categories;
    }

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_measure", nullable = false)
    @Enumerated(EnumType.STRING)
    private UnitMeasure unitMeasure;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name ="image_url", nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public void addQuantity(int amount){

        this.quantity += amount;
    }

    public void removeQuantity(int amount){

        if (amount > this.quantity){
            throw new BusinessRuleException("Estoque insuficiente para realizar esta operação");
        }

        this.quantity -= amount;
    }

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

    public Integer getQuantity() {
        return quantity;
    }

    public UnitMeasure getUnitMeasure() {
        return unitMeasure;
    }

    public void setUnitMeasure(UnitMeasure unitMeasure) {
        this.unitMeasure = unitMeasure;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category categories) {
        this.category = categories;
    }
}
