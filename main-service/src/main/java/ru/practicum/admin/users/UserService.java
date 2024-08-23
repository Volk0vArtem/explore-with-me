package ru.practicum.admin.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.admin.users.model.User;
import ru.practicum.admin.users.model.UserDto;
import ru.practicum.admin.users.model.UserMapper;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto addUser(UserDto userDto) {
        try {
            User user = userMapper.toUser(userDto);
            userRepository.save(user);
            UserDto savedUser = userMapper.toUserDto(user);
            log.info("Added user: {}", savedUser);
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("User with email " + userDto.getEmail() + " already exists");
        }
    }

    public List<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size) {
        if (from == null || from < 0) {
            throw new BadRequestException("Pagination parameter 'from' must be non-negative");
        }
        if (size == null || size <= 0) {
            throw new BadRequestException("Pagination parameter 'size' must be greater than 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        Page<User> users;

        if (ids != null) {
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            users = userRepository.findAllByIdsPageable(ids, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.getContent().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with ID " + id + " not found"));
        userRepository.delete(user);
        log.info("Deleted user with ID: {}", id);
    }
}
