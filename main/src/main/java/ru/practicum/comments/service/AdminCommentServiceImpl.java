package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.exceptions.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {
    private final CommentRepository commentRepository;

    @Override
    public void deleteComment(int id) {
        if (!commentRepository.existsById(id)) {
            throw new NotFoundException("Комментарий с id " + id + " не существует");
        }
        commentRepository.deleteById(id);
        log.info("Удален комментарий с id {}", id);
    }
}
