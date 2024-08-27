package ru.practicum.events.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.categories.dto.CategoryMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Location;
import ru.practicum.users.dto.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface EventMapper {

    @Mapping(target = "eventDate", source = "event.eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "createdOn", source = "event.createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "eventDate", source = "event.eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", source = "event.publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "location", expression = "java(buildLocation(event.getLat(), event.getLon()))")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "eventDate", source = "newEventDto.eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "lat", source = "newEventDto.location.lat")
    @Mapping(target = "lon", source = "newEventDto.location.lon")
    @Mapping(target = "category", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    default Location buildLocation(Float lat, Float lon) {
        return new Location(lat, lon);
    }
}

