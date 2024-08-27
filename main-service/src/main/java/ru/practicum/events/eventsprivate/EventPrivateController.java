package ru.practicum.events.eventsprivate;

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
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventRequestStatusUpdateRequest;
import ru.practicum.events.dto.EventRequestStatusUpdateResult;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequest;
import ru.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable("userId") Long userId, @Valid @RequestBody NewEventDto event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventPrivateService.createEvent(event, userId));
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByUserId(@PathVariable Long userId,
                                                                 @RequestParam(defaultValue = "0") Integer from,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok().body(eventPrivateService.getAllEventsByUserId(userId, from, size));
    }

    @GetMapping("{eventId}")
    public ResponseEntity<EventFullDto> getEventByUserIdAndEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        return ResponseEntity.ok().body(eventPrivateService.getEventByUserIdAndEventId(userId, eventId));
    }

    @PatchMapping("{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return ResponseEntity.ok().body(eventPrivateService.updateEvent(eventId, updateEventUserRequest, userId));
    }

    @GetMapping("{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsByEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        return ResponseEntity.ok().body(eventPrivateService.getRequests(userId, eventId));
    }

    @PatchMapping("{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequest(@PathVariable Long userId, @PathVariable Long eventId,
                                                                        @RequestBody EventRequestStatusUpdateRequest request) {
        return ResponseEntity.ok().body(eventPrivateService.updateRequestStatus(userId, eventId, request));
    }
}
