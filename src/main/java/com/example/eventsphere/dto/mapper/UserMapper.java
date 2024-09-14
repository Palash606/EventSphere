package com.example.eventsphere.dto.mapper;

import com.example.eventsphere.dto.UserDto;
import com.example.eventsphere.model.User;

public class UserMapper {
    public static UserDto mapToUserDto(User user, UserDto userDto) {
        userDto.setUsername(user.getUsername());
        userDto.setPwd(user.getPwd());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    public static User mapToUser(UserDto userDto, User user) {
        user.setUsername(userDto.getUsername());
        user.setPwd(userDto.getPwd());
        user.setEmail(userDto.getEmail());
        return user;
    }
}
