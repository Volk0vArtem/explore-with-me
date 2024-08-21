package ru.practicum.users.events.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.admin.categories.model.CategoryDto;
import ru.practicum.admin.users.model.UserShortDto;
import ru.practicum.users.events.model.Location;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EventFullDto {

    private String annotation;

    private CategoryDto category;

    private Integer confirmedRequests;

    private String createdOn;

    private String description;

    private String eventDate;

    private Long id;

    private UserShortDto initiator;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private String publishedOn;

    private Boolean requestModeration;

    private String state;

    private String title;

    private Integer views;
}
