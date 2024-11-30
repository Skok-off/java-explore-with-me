package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.model.CommentMapper;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.enums.EventState;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateCommentServiceImpl implements PrivateCommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentDto createComment(int userId, int eventId, CommentDto commentDto) {
        checkId(userId, userRepository);
        checkId(eventId, eventRepository);
        Event event = eventRepository.getEventById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие не опубликовано");
        }

        commentDto.setDate(LocalDateTime.now());
        User user = userRepository.getUserById(userId);
        Comment comment = CommentMapper.toComment(commentDto, user, event);

        CommentDto result = CommentMapper.toCommentDto(commentRepository.save(comment));
        log.info("Создан комментарий {}", comment);
        return result;
    }

    @Override
    public CommentDto updateComment(int eventId, int commentId, int userId, CommentDto commentDto) {
        checkId(commentId, commentRepository);
        checkId(userId, userRepository);
        checkId(eventId, eventRepository);
        Comment commentToUpdate = commentRepository.findById(commentId);
        if (commentToUpdate.getAuthor().getId() != userId) {
            throw new ConflictException("Этот пользователь с Id:" + userId + " не может обновить комментарий");
        }

        commentToUpdate.setText(commentDto.getText());
        commentToUpdate.setCommentDate(LocalDateTime.now());
        commentToUpdate.setEdited(true);

        CommentDto result = CommentMapper.toCommentDto(commentRepository.save(commentToUpdate));
        log.info("Обновлен комментарий {}", result);
        return result;
    }

    private void checkId(int id, JpaRepository<?, Integer> repository) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Не найден id " + id + " из: " + repository);
        }
    }
}

