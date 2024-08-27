package ru.practicum.events.eventsprivate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventMapper;
import ru.practicum.events.dto.EventRequestStatusUpdateRequest;
import ru.practicum.events.dto.EventRequestStatusUpdateResult;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.StateAction;
import ru.practicum.events.dto.UpdateEventUserRequest;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.State;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.RequestMapper;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.model.RequestStatus;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventPrivateService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventMapper eventMapper;

    public EventFullDto createEvent(NewEventDto newEventDto, Long userId) {
        validateEventDate(newEventDto.getEventDate());
        if (newEventDto.getPaid() == null) {
            newEventDto.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }
        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0);
        }
        Event event = eventMapper.toEvent(newEventDto);
        User user = getUserById(userId);
        event.setInitiator(user);
        Category category = getCategoryById(newEventDto.getCategory());
        event.setCategory(category);
        event.setState(State.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0);
        event.setViews(0);
        log.info("Successfully created new event: {}", event);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(savedEvent);
    }

    public List<EventShortDto> getAllEventsByUserId(Long userId, Integer from, Integer size) {
        getUserById(userId);
        if (from == null || from < 0) {
            throw new BadRequestException("Pagination parameter 'from' must be non-negative");
        }
        if (size == null || size <= 0) {
            throw new BadRequestException("Pagination parameter 'size' must be greater than 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        log.info("Successfully retrieved event by userId: {}", userId);
        return events.stream().map(eventMapper::toEventShortDto).collect(Collectors.toList());
    }

    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        getUserById(userId);
        getEventById(eventId);
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId);
        if (event == null) {
            throw new NotFoundException("Event not found");
        }
        log.info("Successfully retrieved event: {}", event);
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto updateEvent(Long eventId, UpdateEventUserRequest updatedEvent, Long userId) {
        getUserById(userId);
        Event event = getEventById(eventId);
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            if (event.getState().equals(State.PUBLISHED)) {
                throw new DataIntegrityViolationException("Event already published");
            }
            if (updatedEvent.getAnnotation() != null) {
                event.setAnnotation(updatedEvent.getAnnotation());
            }
            if (updatedEvent.getCategory() != null) {
                event.setCategory(getCategoryById(updatedEvent.getCategory()));
            }
            if (updatedEvent.getDescription() != null) {
                event.setDescription(updatedEvent.getDescription());
            }
            if (updatedEvent.getEventDate() != null) {
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                    throw new BadRequestException("Too late do changes");
                }
                if (event.getEventDate().isBefore(LocalDateTime.now())) {
                    throw new BadRequestException("Event is finished");
                }
                LocalDateTime updateEventDate = LocalDateTime.parse(updatedEvent.getEventDate(), FORMATTER);
                if (updateEventDate.isBefore(LocalDateTime.now()) ||
                        updateEventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                    throw new BadRequestException("Cant set event date");
                }
                event.setEventDate(updateEventDate);
            }
            if (updatedEvent.getLocation() != null) {
                event.setLat(updatedEvent.getLocation().getLat());
                event.setLon(updatedEvent.getLocation().getLon());
            }
            if (updatedEvent.getPaid() != null) {
                event.setPaid(updatedEvent.getPaid());
            }
            if (updatedEvent.getParticipantLimit() != null) {
                event.setParticipantLimit(updatedEvent.getParticipantLimit());
            }
            if (updatedEvent.getRequestModeration() != null) {
                event.setRequestModeration(updatedEvent.getRequestModeration());
            }
            if (updatedEvent.getStateAction() != null) {
                if (updatedEvent.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
                    event.setState(State.CANCELED);
                } else {
                    event.setState(State.PENDING);
                }
            }
            if (updatedEvent.getTitle() != null) {
                event.setTitle(updatedEvent.getTitle());
            }
            return eventMapper.toEventFullDto(eventRepository.save(event));
        } else {
            throw new BadRequestException("User is not owner of event " + eventId);
        }
    }

    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        getUserById(userId);
        Event event = getEventById(eventId);
        List<Request> requests = requestRepository.findAllByEvent(event);
        return requests.stream().map(requestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        getUserById(userId);
        Event event = getEventById(eventId);

        List<ParticipationRequestDto> confirmedReqs = new ArrayList<>();
        List<ParticipationRequestDto> canceledReqs = new ArrayList<>();

        for (Long requestId : updateRequest.getRequestIds()) {
            Request request = requestRepository
                    .findById(requestId).orElseThrow(() -> new NotFoundException("Request not found"));
            processRequestStatus(updateRequest.getStatus(), event, request, confirmedReqs, canceledReqs);
        }

        eventRepository.save(event);
        return new EventRequestStatusUpdateResult(confirmedReqs, canceledReqs);
    }

    private void processRequestStatus(String status, Event event, Request request,
                                      List<ParticipationRequestDto> confirmedReqs,
                                      List<ParticipationRequestDto> canceledReqs) {
        if (!request.getStatus().equals(RequestStatus.PENDING)) {
            if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                throw new DataIntegrityViolationException("Request already confirmed");
            } else {
                throw new BadRequestException("Request " + request.getId() + " is not pending");
            }
        } else {
            if (status.equals("CONFIRMED")) {
                handleConfirmedStatus(event, request, confirmedReqs);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                canceledReqs.add(requestMapper.toParticipationRequestDto(request));
                requestRepository.save(request);
                log.info("Successfully rejected request {}", request.getId());
            }
        }
    }

    private void handleConfirmedStatus(Event event, Request request,
                                       List<ParticipationRequestDto> confirmedRequests) {
        if (event.getConfirmedRequests() >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            request.setStatus(RequestStatus.CANCELED);
            confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
            requestRepository.save(request);
            throw new DataIntegrityViolationException("The participant limit is reached");
        }
        request.setStatus(RequestStatus.CONFIRMED);
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
        requestRepository.save(request);
        log.info("Successfully confirmed request {}", request.getId());
    }

    private void validateEventDate(String eventDate) {
        LocalDateTime date = LocalDateTime.parse(eventDate, FORMATTER);
        if (date.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Date must be in future more than 2 hours");
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    private Category getCategoryById(Integer categoryId) {
        return categoryRepository.findById(Long.valueOf(categoryId))
                .orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " not found"));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> {
            log.error("Event with id {} not found", eventId);
            return new NotFoundException("Event with id " + eventId + " not found");
        });
    }
}
