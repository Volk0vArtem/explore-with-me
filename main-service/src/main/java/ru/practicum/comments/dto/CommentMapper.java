package ru.practicum.comments.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comments.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toComment(CommentDto commentDto);

    @Mapping(target = "created", source = "comment.created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "eventId", source = "comment.event.id")
    CommentDto toCommentDto(Comment comment);
}
