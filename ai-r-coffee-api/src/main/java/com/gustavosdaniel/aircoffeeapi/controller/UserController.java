package com.gustavosdaniel.aircoffeeapi.controller;

import com.gustavosdaniel.aircoffeeapi.controller.openApi.UserOpenApi;
import com.gustavosdaniel.aircoffeeapi.domain.dto.response.UserResponse;
import com.gustavosdaniel.aircoffeeapi.domain.mapping.UserMapper;
import com.gustavosdaniel.aircoffeeapi.domain.po.User;
import com.gustavosdaniel.aircoffeeapi.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserOpenApi {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(){

        User user = userService.getOrCreateCurrentUser();

        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findUserById(@PathVariable UUID id){

        UserResponse response = userService.findUserById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponse> findUserByEmail(@RequestParam String email){

        UserResponse response = userService.findUserByEmail(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<Page<UserResponse>> allUsers(
            @RequestParam(required = false) String name,
            @ParameterObject
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.ASC)
            Pageable pageable
    ){

        Page<UserResponse> responses = userService.allUsersByName(name, pageable);

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable UUID id){

        userService.activateUser(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id){

        userService.disableUser(id);

        return ResponseEntity.noContent().build();
    }

}
