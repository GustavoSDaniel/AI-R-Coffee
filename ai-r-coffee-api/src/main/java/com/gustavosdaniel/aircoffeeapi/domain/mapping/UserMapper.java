package com.gustavosdaniel.aircoffeeapi.domain.mapping;

import com.gustavosdaniel.aircoffeeapi.domain.dto.response.UserResponse;
import com.gustavosdaniel.aircoffeeapi.domain.po.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user){

        if (user == null) return null;

        return new UserResponse(

                user.getId(),
                user.getEmail(),
                user.getUserName(),
                user.getRole()
        );
    }
}
