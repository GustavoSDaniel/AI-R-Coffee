package com.gustavosdaniel.aircoffeeapi.domain.po;

import com.gustavosdaniel.aircoffeeapi.domain.enums.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends BaseEntity{

    public User(){}

    public User(String userName, UserRole role) {
        this.userName = userName;
        this.role = role;
    }

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.CONSUMER;

    public String getKeycloakId() {
        return keycloakId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
