package ru.practicum.compilations.compilationspublic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.model.CompilationEvent;
import ru.practicum.compilations.model.CompilationMapper;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.compilations.repository.CompilationsEventRepository;
import ru.practicum.events.dto.EventMapper;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicService {

    private final CompilationRepository compilationRepository;
    private final CompilationsEventRepository compilationsEventRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        if (from == null || from < 0) {
            throw new BadRequestException("Pagination parameter 'from' must be non-negative");
        }
        if (size == null || size <= 0) {
            throw new BadRequestException("Pagination parameter 'size' must be greater than 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = pinned == null ?
                compilationRepository.findAll(pageable).getContent()
                : compilationRepository.findAllByPinned(pinned, pageable);

        log.info("Retrieved {} compilations with pinned={} from index {} with page size {}",
                compilations.size(), pinned, from, size);

        return compilations.stream()
                .map(this::toCompilationDtoWithEvents)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilation(Long id) {
        Compilation compilation = compilationRepository
                .findById(id).orElseThrow(() -> new NotFoundException("Compilation with id " + id + " not found"));
        CompilationDto compilationDto = toCompilationDtoWithEvents(compilation);
        log.info("Retrieved compilation with id {}", id);
        return compilationDto;
    }

    private CompilationDto toCompilationDtoWithEvents(Compilation compilation) {
        List<CompilationEvent> compilationEvents = compilationsEventRepository.findAllByCompilationId(compilation.getId());
        List<EventShortDto> events = compilationEvents.stream()
                .map(compilationEvent -> eventMapper.toEventShortDto(getEventById(compilationEvent.getEvent().getId())))
                .collect(Collectors.toList());
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
        compilationDto.setEvents(events);
        return compilationDto;
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));
    }
}
