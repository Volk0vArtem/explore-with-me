package ru.practicum.admin.comments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.admin.users.UserRepository;
import ru.practicum.admin.users.model.User;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.comments.CommentRepository;
import ru.practicum.users.comments.model.Comment;
import ru.practicum.users.comments.model.CommentDto;
import ru.practicum.users.comments.model.CommentMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentAdminService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public List<CommentDto> getCommentsByUser(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new NotFoundException("User with id " + userId + " not found."));
        return commentRepository.getCommentsByUser(user).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public CommentDto patchComment(Long commentId, CommentDto commentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id " + commentId + " not found."));
        comment.setText(commentDto.getText());
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }
}
