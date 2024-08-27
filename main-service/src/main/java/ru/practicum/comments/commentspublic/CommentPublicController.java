package ru.practicum.comments.commentspublic;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comments.dto.CommentDto;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentPublicController {
    private final CommentPublicService service;

    @GetMapping("/{eventId}")
    public ResponseEntity<List<CommentDto>> getCommentsByEvent(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok().body(service.getCommentsByEvent(eventId));
    }
}
