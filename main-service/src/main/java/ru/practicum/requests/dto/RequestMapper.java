package ru.practicum.requests.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.requests.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "event.id", target = "event")
    ParticipationRequestDto toParticipationRequestDto(Request request);
}
