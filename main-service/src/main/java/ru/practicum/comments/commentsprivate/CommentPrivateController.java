package ru.practicum.comments.commentsprivate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comments.dto.CommentDto;

@RestController
@RequestMapping("/users/comments")
@RequiredArgsConstructor
public class CommentPrivateController {
    private final CommentPrivateService service;

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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable("userId") Long userId,
                              @PathVariable("commentId") Long commentId) {
        service.deleteComment(userId, commentId);
    }
}