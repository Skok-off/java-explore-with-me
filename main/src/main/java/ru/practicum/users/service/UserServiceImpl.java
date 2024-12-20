package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.NotUniqueException;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.model.User;
import ru.practicum.users.model.UserMapper;
import ru.practicum.users.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        if (userRepository.existsUserByEmail(newUserRequest.getEmail())) {
            throw new NotUniqueException("Email must be unique");
        }
        User userToSave = userRepository.save(UserMapper.toUser(newUserRequest));
        log.info("User Saved {}", userToSave);
        return UserMapper.toUserDto(userToSave);
    }

    @Override
    public List<UserDto> getUsersByIds(List<Integer> ids, int from, int size) {
        List<User> userList;
        if (size < 1) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(from / size, size);
        if (Objects.nonNull(ids) && !ids.isEmpty()) {
            userList = userRepository.findAllById(ids);

        } else {
            userList = userRepository.findAll(pageable).getContent();

        }
        log.info("Found users {} (by ids {}, from {}, size {})", userList, ids, from, size);
        return userList.stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public void deleteUserById(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("UserId " + userId + " not found");
        }
        log.info("Delete User by Id {}", userId);
        userRepository.deleteById(userId);
    }
}
