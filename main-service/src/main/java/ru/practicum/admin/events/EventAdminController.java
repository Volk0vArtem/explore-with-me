package ru.practicum.admin.events;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.admin.events.model.UpdateEventAdminRequest;
import ru.practicum.users.events.model.dto.EventFullDto;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventAdminService eventAdminService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(@RequestParam(required = false) List<Long> users,
                                                        @RequestParam(required = false) List<String> states,
                                                        @RequestParam(required = false) List<Long> categories,
                                                        @RequestParam(required = false) String rangeStart,
                                                        @RequestParam(required = false) String rangeEnd,
                                                        @RequestParam(defaultValue = "0") Integer from,
                                                        @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok()
                .body(eventAdminService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequest event) {
        return ResponseEntity.ok().body(eventAdminService.updateEvent(eventId, event));
    }
}
