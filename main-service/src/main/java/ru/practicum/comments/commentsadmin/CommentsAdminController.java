package ru.practicum.comments.commentsadmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comments.dto.CommentDto;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentsAdminController {
    private final CommentAdminService service;

    @GetMapping("/{userId}")
    public ResponseEntity<List<CommentDto>> getCommentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok().body(service.getCommentsByUser(userId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> patchComment(@PathVariable Long commentId,
                                                   @RequestBody @Valid CommentDto commentDto) {
        return ResponseEntity.ok().body(service.patchComment(commentId, commentDto));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        service.deleteComment(commentId);
    }
}
