package ru.practicum.comments.commentsprivate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public CommentDto addComment(Long userId, Long eventId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found."));
        Comment comment = commentMapper.toComment(commentDto);
        comment.setCreated(LocalDateTime.now());
        comment.setEvent(event);
        comment.setUser(user);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    public CommentDto patchComment(Long userId, Long commentId, CommentDto commentDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found."));
        Comment comment = getCommentById(commentId, userId);
        comment.setText(commentDto.getText());
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    public void deleteComment(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found."));
        commentRepository.deleteById(getCommentById(commentId, userId).getId());
    }

    private Comment getCommentById(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id " + commentId + " not found."));
        if (!Objects.equals(userId, comment.getUser().getId())) {
            throw new BadRequestException("The user is not the author of the comment");
        }
        return comment;
    }
}
