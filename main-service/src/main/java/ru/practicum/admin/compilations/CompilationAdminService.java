package ru.practicum.admin.compilations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.admin.compilations.model.Compilation;
import ru.practicum.admin.compilations.model.CompilationEvent;
import ru.practicum.admin.compilations.model.CompilationMapper;
import ru.practicum.admin.compilations.model.dto.CompilationDto;
import ru.practicum.admin.compilations.model.dto.NewCompilationDto;
import ru.practicum.admin.compilations.model.dto.UpdateCompilationRequestDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.events.EventRepository;
import ru.practicum.users.events.model.Event;
import ru.practicum.users.events.model.dto.EventMapper;
import ru.practicum.users.events.model.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminService {

    private final CompilationRepository compilationRepository;
    private final CompilationsEventRepository compilationsEventRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        compilation.setPinned(Boolean.TRUE.equals(newCompilationDto.getPinned()));
        compilationRepository.save(compilation);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            saveCompilationEvents(newCompilationDto.getEvents(), compilation);
        }

        CompilationDto compilationDto = mapToCompilationDtoWithEvents(compilation);
        log.info("Added compilation: {}", compilationDto);
        return compilationDto;
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto updateCompilationRequestDto) {
        Compilation compilation = getCompilationById(compId);

        if (updateCompilationRequestDto.getTitle() != null) {
            compilation.setTitle(updateCompilationRequestDto.getTitle());
        }
        if (updateCompilationRequestDto.getPinned() != null) {
            compilation.setPinned(updateCompilationRequestDto.getPinned());
        }

        if (updateCompilationRequestDto.getEvents() != null) {
            compilationsEventRepository.deleteAllByCompilationId(compId);
            if (!updateCompilationRequestDto.getEvents().isEmpty()) {
                saveCompilationEvents(updateCompilationRequestDto.getEvents(), compilation);
            }
        }

        CompilationDto compilationDto = mapToCompilationDtoWithEvents(compilation);
        log.info("Updated compilation: {}", compilationDto);
        return compilationDto;
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = getCompilationById(compId);
        compilationRepository.delete(compilation);
        compilationsEventRepository.deleteAllByCompilationId(compId);
        log.info("Deleted compilation with id: {}", compId);
    }

    private void saveCompilationEvents(List<Long> eventIds, Compilation compilation) {
        for (Long eventId : eventIds) {
            Event event = getEventById(eventId);
            CompilationEvent compilationEvent = new CompilationEvent();
            compilationEvent.setCompilation(compilation);
            compilationEvent.setEvent(event);
            compilationsEventRepository.save(compilationEvent);
        }
    }

    private CompilationDto mapToCompilationDtoWithEvents(Compilation compilation) {
        List<CompilationEvent> compilationEventList = compilationsEventRepository.findAllByCompilationId(compilation.getId());
        List<EventShortDto> events = new ArrayList<>();
        for (CompilationEvent compilationEvent : compilationEventList) {
            events.add(eventMapper.toEventShortDto(compilationEvent.getEvent()));
        }
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
        compilationDto.setEvents(events);
        return compilationDto;
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found."));
    }

    private Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compId + " not found."));
    }
}
