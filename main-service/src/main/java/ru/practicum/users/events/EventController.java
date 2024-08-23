package ru.practicum.users.events;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.users.events.model.dto.EventFullDto;
import ru.practicum.users.events.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.users.events.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.users.events.model.dto.EventShortDto;
import ru.practicum.users.events.model.dto.NewEventDto;
import ru.practicum.users.events.model.dto.UpdateEventUserRequest;
import ru.practicum.users.requests.model.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable("userId") Long userId, @Valid @RequestBody NewEventDto event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(event, userId));
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByUserId(@PathVariable Long userId,
                                                                 @RequestParam(defaultValue = "0") Integer from,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok().body(eventService.getAllEventsByUserId(userId, from, size));
    }

    @GetMapping("{eventId}")
    public ResponseEntity<EventFullDto> getEventByUserIdAndEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        return ResponseEntity.ok().body(eventService.getEventByUserIdAndEventId(userId, eventId));
    }

    @PatchMapping("{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return ResponseEntity.ok().body(eventService.updateEvent(eventId, updateEventUserRequest, userId));
    }

    @GetMapping("{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsByEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        return ResponseEntity.ok().body(eventService.getRequests(userId, eventId));
    }

    @PatchMapping("{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequest(@PathVariable Long userId, @PathVariable Long eventId,
                                                                        @RequestBody EventRequestStatusUpdateRequest request) {
        return ResponseEntity.ok().body(eventService.updateRequestStatus(userId, eventId, request));
    }
}
