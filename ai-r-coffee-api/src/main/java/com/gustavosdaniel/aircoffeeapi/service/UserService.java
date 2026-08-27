package com.gustavosdaniel.aircoffeeapi.service;

import com.gustavosdaniel.aircoffeeapi.domain.dto.response.UserResponse;
import com.gustavosdaniel.aircoffeeapi.domain.enums.UserRole;
import com.gustavosdaniel.aircoffeeapi.domain.mapping.UserMapper;
import com.gustavosdaniel.aircoffeeapi.domain.po.User;
import com.gustavosdaniel.aircoffeeapi.exception.UserNotFoundException;
import com.gustavosdaniel.aircoffeeapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public User getOrCreateCurrentUser(){

        Jwt jwt = currentJwt();
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String username = jwt.getClaimAsString("preferred_username");
        UserRole roleFromToken = extractRole(jwt);

        return userRepository.findByKeycloakId(keycloakId)
                .map(existUser -> {

                    if (existUser.getRole() != roleFromToken){ existUser.setRole(roleFromToken);

                    }
                    log.info("Usuário {}, logado com sucesso", existUser.getUserName());
                    return existUser;
                })
                .orElseGet(() -> {
                    User newUser = new User(
                            keycloakId, email, username, roleFromToken
                    );

                    log.info("Usuario criado com sucesso");

                    return userRepository.save(newUser);
                });
    }

    @Transactional(readOnly = true)
    public UserResponse findUserById(UUID id){

        log.info("Buscando usuário por id: {}", id);

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        log.info("Usuário: {}, encontrado com sucesso", user.getId());

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findUserByEmail(String email){

        log.info("Buscando usuário por email: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);

        log.info("Usuário com o email: {}, encontrado com sucesso", user.getEmail());

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> allUsersByName(String name, Pageable pageable){

        log.info("Buscando todos os usuários");

        String searchName = (name == null) ? "" : name;

        Page<User> users = userRepository.searchByName(searchName, pageable);

        log.info("Total de {}, usúarios encontrados", users.getTotalElements());

        return users.map(userMapper::toResponse);
    }

    @Transactional
    public void activateUser(UUID id){

        log.info("Ativando usuário");

        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        user.activate();

        userRepository.save(user);

        log.info("Usuário ativado com sucesso");
    }

    @Transactional
    public void disableUser(UUID id){

        log.info("Desativando usuário");

        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        user.deactivate();

        userRepository.save(user);

        log.info("Usuário desativado com sucesso");
    }

    private Jwt currentJwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @SuppressWarnings("unchecked")
    private UserRole extractRole(Jwt jwt){

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");

            if (roles.stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"))) {
                return UserRole.ADMIN;
            }

        }

        return UserRole.CONSUMER;
    }
}
