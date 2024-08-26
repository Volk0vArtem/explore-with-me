package ru.practicum.users.comments;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.users.comments.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
