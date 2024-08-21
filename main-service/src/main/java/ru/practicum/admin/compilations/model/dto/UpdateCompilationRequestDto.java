package ru.practicum.admin.compilations.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UpdateCompilationRequestDto {

    private List<Long> events;

    private Boolean pinned;

    @Length(min = 1, max = 50)
    private String title;
}
