package ru.practicum.admin.comments;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.users.comments.model.CommentDto;

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
                                                   @RequestBody CommentDto commentDto) {
        return ResponseEntity.ok().body(service.patchComment(commentId, commentDto));
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        service.deleteComment(commentId);
    }
}
