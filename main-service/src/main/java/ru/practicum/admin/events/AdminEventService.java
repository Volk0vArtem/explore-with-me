package ru.practicum.admin.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.admin.categories.CategoryRepository;
import ru.practicum.admin.categories.model.Category;
import ru.practicum.admin.events.model.UpdateEventAdminRequest;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.events.EventRepository;
import ru.practicum.users.events.model.Event;
import ru.practicum.users.events.model.State;
import ru.practicum.users.events.model.dto.EventFullDto;
import ru.practicum.users.events.model.dto.EventMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEventService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEvent) {
        Event event = getEventById(eventId);

        if (updateEvent.getAnnotation() != null) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getDescription() != null) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateEvent.getEventDate(), formatter);
            if (eventDate.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Event date is before current date");
            }
            event.setEventDate(eventDate);
        }
        if (updateEvent.getTitle() != null) {
            event.setTitle(updateEvent.getTitle());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getLocation() != null) {
            event.setLat(updateEvent.getLocation().getLat());
            event.setLon(updateEvent.getLocation().getLon());
        }
        if (updateEvent.getCategory() != null) {
            Category category = getCategoryById(updateEvent.getCategory());
            event.setCategory(category);
        }
        if (updateEvent.getStateAction() != null) {
            if (updateEvent.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT) && event.getState().equals(State.PUBLISHED)) {
                throw new DataIntegrityViolationException("Event is already published");
            }
            if (updateEvent.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT) && event.getState().equals(State.CANCELED)) {
                throw new DataIntegrityViolationException("Event is canceled");
            }
            if (updateEvent.getStateAction().equals(StateActionAdmin.REJECT_EVENT) && event.getState().equals(State.PUBLISHED)) {
                throw new DataIntegrityViolationException("Event is already published");
            }
            if (updateEvent.getStateAction().equals(StateActionAdmin.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            } else {
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
        }
        eventRepository.save(event);
        log.info("Event updated by admin successful");
        return eventMapper.toEventFullDto(event);
    }

    public List<EventFullDto> getEvents(List<Long> users,
                                        List<String> states,
                                        List<Long> categories,
                                        String rangeStart,
                                        String rangeEnd,
                                        Integer from,
                                        Integer size) {
        int page = from != null ? from / size : 0;
        int pageSize = size != null ? size : 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null;
        LocalDateTime endTime = rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null;

        List<Event> eventsList;
        if (startTime == null && endTime == null) {
            if (users == null && states == null && categories == null) {
                eventsList = eventRepository.findAll(pageable).getContent();
            } else if (users != null && states == null && categories == null) {
                eventsList = eventRepository.getEventsWithUsers(users, now, pageable);
            } else if (users == null && states != null && categories == null) {
                List<State> stateList = parseStates(states);
                eventsList = eventRepository.getEventsWithState(stateList, now, pageable);
            } else if (users == null && states == null && categories != null) {
                eventsList = eventRepository.getEventsWithCategory(categories, now, pageable);
            } else if (users != null && states != null && categories == null) {
                List<State> stateList = parseStates(states);
                eventsList = eventRepository.getEventsWithUsersAndStates(users, stateList, now, pageable);
            } else if (users != null && states == null && categories != null) {
                eventsList = eventRepository.getEventsWithUsersAndCategories(users, categories, now, pageable);
            } else if (users == null && states != null && categories != null) {
                List<State> stateList = parseStates(states);
                eventsList = eventRepository.getEventsWithStateAndCategories(stateList, categories, now, pageable);
            } else {
                List<State> stateList = parseStates(states);
                eventsList = eventRepository.getEventsWithUsersAndStatesAndCategories(users, stateList, categories, now, pageable);
            }
        } else {
            if (users == null && states == null && categories == null) {
                eventsList = eventRepository.getEventsWithTimes(startTime, endTime, pageable);
            } else if (users != null && states == null && categories == null) {
                eventsList = eventRepository.getEventsWithUsersAndTimes(users, startTime, endTime, pageable);
            } else if (users == null && states != null && categories == null) {
                List<State> stateList = parseStates(states);
                eventsList = eventRepository.getEventsWithStateAndTimes(stateList, startTime, endTime, pageable);
            } else if (users == null && states == null && categories != null) {
                eventsList = eventRepository.getEventsWithCategoryAndTimes(categories, startTime, endTime, pageable);
            } else if (users != null && states != null && categories == null) {
                List<State> stateList = parseStates(states);
                eventsList = eventRepository.getEventsWithUsersAndStatesAndTimes(users, stateList, startTime, endTime, pageable);
            } else if (users != null && states == null && categories != null) {
                eventsList = eventRepository.getEventsWithUsersAndCategoriesAndTimes(users, categories, startTime, endTime, pageable);
            } else if (users == null && states != null && categories != null) {
                List<State> stateList = parseStates(states);
                eventsList = eventRepository.getEventsWithStateAndCategoriesAndTimes(stateList, categories, startTime, endTime, pageable);
            } else {
                List<State> stateList = parseStates(states);
                eventsList = eventRepository.getEventsWithUsersAndStatesAndCategoriesAndTimes(users, stateList, categories, startTime, endTime, pageable);
            }
        }

        return eventsList.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));
    }

    private Category getCategoryById(Integer categoryId) {
        return categoryRepository.findById(Long.valueOf(categoryId))
                .orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " not found"));
    }

    private List<State> parseStates(List<String> states) {
        return states.stream()
                .map(State::valueOf)
                .collect(Collectors.toList());
    }
}
