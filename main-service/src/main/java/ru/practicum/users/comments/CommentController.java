package ru.practicum.users.comments;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.users.comments.model.CommentDto;

import java.util.List;

@RestController
@RequestMapping("/users/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService service;

    @PostMapping("{userId}/{eventId}")
    public ResponseEntity<CommentDto> addComment(@Valid @RequestBody CommentDto commentDto,
                                                 @PathVariable("userId") Long userId,
                                                 @PathVariable("eventId") Long eventId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addComment(userId, eventId, commentDto));
    }

    @PatchMapping("/{userId}/{commentId}")
    public ResponseEntity<CommentDto> patchComment(@Valid @RequestBody CommentDto commentDto,
                                                   @PathVariable("userId") Long userId,
                                                   @PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok().body(service.patchComment(userId, commentId, commentDto));
    }

    @DeleteMapping("/{userId}/{commentId}")
    public void deleteComment(@PathVariable("userId") Long userId,
                              @PathVariable("commentId") Long commentId) {
        service.deleteComment(userId, commentId);
    }

    @GetMapping("/{userId}/{eventId}")
    public ResponseEntity<List<CommentDto>> getCommentsByEvent(@PathVariable("userId") Long userId,
                                                               @PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok().body(service.getCommentsByEvent(userId, eventId));
    }
}