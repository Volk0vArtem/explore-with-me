package ru.practicum.comments.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import ru.practicum.users.dto.UserShortDto;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CommentDto {
    @NotBlank
    @Length(min = 10, max = 280)
    private String text;
    private Long eventId;
    private UserShortDto user;
    private String created;
}
