package ru.practicum.users.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.admin.users.UserRepository;
import ru.practicum.admin.users.model.User;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.events.EventRepository;
import ru.practicum.users.events.model.Event;
import ru.practicum.users.events.model.State;
import ru.practicum.users.requests.model.ParticipationRequestDto;
import ru.practicum.users.requests.model.Request;
import ru.practicum.users.requests.model.RequestMapper;
import ru.practicum.users.requests.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new DataIntegrityViolationException("Initiator cant send request to his own event");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new DataIntegrityViolationException("Event is not published");
        }
        if (event.getParticipantLimit() != 0) {
            if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                throw new DataIntegrityViolationException("Participant limit exceeded");
            }
        }
        if (requestRepository.findByRequesterAndEvent(userId, eventId).isPresent()) {
            throw new DataIntegrityViolationException("Request already exists");
        }
        Request request = new Request();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setUser(user);
            request.setEvent(event);
            request.setStatus(RequestStatus.CONFIRMED);
            request.setCreated(LocalDateTime.now());
            requestRepository.save(request);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        } else {
            request.setUser(user);
            request.setEvent(event);
            request.setStatus(RequestStatus.PENDING);
            request.setCreated(LocalDateTime.now());
            requestRepository.save(request);
        }
        log.info("Request created");
        return requestMapper.toParticipationRequestDto(request);
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        getUserById(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " not found"));
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            Event event = getEventById(request.getEvent().getId());
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }
        request.setStatus(RequestStatus.CANCELED);
        log.info("Request cancelled successful");
        return requestMapper.toParticipationRequestDto(request);
    }

    public List<ParticipationRequestDto> getRequests(Long userId) {
        User user = getUserById(userId);
        List<Request> requests = requestRepository.findAllByUser(user);
        log.info("Get requests from user {}", userId);
        return requests.stream().map(requestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }
}
