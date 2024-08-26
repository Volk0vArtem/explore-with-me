package ru.practicum.users.comments;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.admin.users.model.User;
import ru.practicum.users.comments.model.Comment;
import ru.practicum.users.events.model.Event;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> getCommentsByUser(User user);

    List<Comment> getCommentsByEvent(Event event);
}
