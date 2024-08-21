package ru.practicum.admin.users.model;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserDto userDto);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    UserShortDto toUserShortDto(UserDto userDto);
}
