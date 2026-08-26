package com.gustavosdaniel.aircoffeeapi.domain.po;

import com.gustavosdaniel.aircoffeeapi.exception.BusinessRuleException;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity {

    protected BaseEntity() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreated(){
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdated(){
        this.updatedAt = OffsetDateTime.now();
    }

    public void deactivate(){
        if (!this.active) {

            throw new BusinessRuleException(
                    "Não é possivel realizar essa ação pois ele ja encontra desativado");
        }

        this.active = false;
    }

    public void activate(){
        if (this.active){

            throw new BusinessRuleException(
                    "Não é possivel realizar essa ação pois ele ja encontra ativada");
        }
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
