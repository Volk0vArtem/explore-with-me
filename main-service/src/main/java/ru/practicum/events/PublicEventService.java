package ru.practicum.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatsClient;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.events.EventRepository;
import ru.practicum.users.events.ViewsRepository;
import ru.practicum.users.events.model.Event;
import ru.practicum.users.events.model.State;
import ru.practicum.users.events.model.Views;
import ru.practicum.users.events.model.dto.EventFullDto;
import ru.practicum.users.events.model.dto.EventMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicEventService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository repository;
    private final StatsClient client;
    private final ViewsRepository viewsRepository;
    private final EventMapper eventMapper;

    public List<EventFullDto> getEvents(String text,
                                        List<Long> categories,
                                        boolean paid,
                                        String rangeStart,
                                        String rangeEnd,
                                        boolean onlyAvailable,
                                        String sort,
                                        Integer from,
                                        Integer size,
                                        String ip,
                                        String requestUri) {
        if (sort == null) {
            sort = "VIEWS";
        }

        if (rangeEnd != null && LocalDateTime.parse(rangeEnd, FORMATTER).isBefore(LocalDateTime.parse(rangeStart, FORMATTER))) {
            throw new BadRequestException("End time must be after start time");
        }
        List<Event> eventList;

        if (from == null || from < 0) {
            throw new BadRequestException("Pagination parameter 'from' must be non-negative");
        }
        if (size == null || size <= 0) {
            throw new BadRequestException("Pagination parameter 'size' must be greater than 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);

        if (rangeEnd == null) {
            LocalDateTime now = LocalDateTime.now();
            eventList = fetchEventsWithoutRange(text, categories, paid, onlyAvailable, sort, pageable, now);
        } else {
            LocalDateTime start = LocalDateTime.parse(rangeStart, FORMATTER);
            LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
            eventList = fetchEventsWithRange(text, categories, paid, onlyAvailable, sort, pageable, start, end);
        }

        List<EventFullDto> eventFullDtoList = new ArrayList<>();
        for (Event event : eventList) {
            EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
            eventFullDtoList.add(eventFullDto);
        }

        addHit(ip, requestUri);
        log.info("Get all events with params successful");
        return eventFullDtoList;
    }

    public EventFullDto getEvent(Long id, String ip, String uri) {
        Event event = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id " + id + " not found"));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Event " + id + " is not published");
        }
        if (viewsRepository.findAllByEventIdAndIp(id, ip).isEmpty()) {
            event.setViews(event.getViews() + 1);
            repository.save(event);
            Views views = new Views();
            views.setIp(ip);
            views.setEvent(event);
            viewsRepository.save(views);
        }

        addHit(ip, uri);
        log.info("Get event {} successful", event.getId());
        return eventMapper.toEventFullDto(event);
    }

    private List<Event> fetchEventsWithoutRange(String text,
                                                List<Long> categories,
                                                boolean paid,
                                                boolean onlyAvailable,
                                                String sort,
                                                Pageable pageable,
                                                LocalDateTime now) {
        if (text == null) {
            return fetchEventsWithoutTextWithoutRange(categories, paid, onlyAvailable, sort, pageable, now);
        } else {
            return fetchEventsWithTextWithoutRange(categories, paid, onlyAvailable, sort, pageable, text, now);
        }
    }

    private List<Event> fetchEventsWithRange(String text,
                                             List<Long> categories,
                                             boolean paid,
                                             boolean onlyAvailable,
                                             String sort,
                                             Pageable pageable,
                                             LocalDateTime start,
                                             LocalDateTime end) {
        if (text == null) {
            return fetchEventsWithoutTextWithRange(categories, paid, onlyAvailable, sort, pageable, start, end);
        } else {
            return fetchEventsWithTextWithRange(categories, paid, onlyAvailable, sort, pageable, text, start, end);
        }
    }

    private List<Event> fetchEventsWithoutTextWithoutRange(List<Long> categories,
                                                           boolean paid,
                                                           boolean onlyAvailable,
                                                           String sort,
                                                           Pageable pageable,
                                                           LocalDateTime now) {
        if (categories == null) {
            return fetchEventsWithoutCategoriesWithoutTextWithoutRange(paid, onlyAvailable, sort, pageable, now);
        } else {
            return fetchEventsWithCategoriesWithoutTextWithoutRange(paid, onlyAvailable, sort, pageable, categories, now);
        }
    }

    private List<Event> fetchEventsWithTextWithoutRange(List<Long> categories,
                                                        boolean paid,
                                                        boolean onlyAvailable,
                                                        String sort,
                                                        Pageable pageable,
                                                        String text,
                                                        LocalDateTime now) {
        if (categories == null) {
            return fetchEventsWithoutCategoriesWithTextWithoutRange(paid, onlyAvailable, sort, pageable, text, now);
        } else {
            return fetchEventsWithCategoriesWithTextWithoutRange(paid, onlyAvailable, sort, pageable, text, categories, now);
        }
    }

    private List<Event> fetchEventsWithoutCategoriesWithoutTextWithoutRange(boolean paid,
                                                                            boolean onlyAvailable,
                                                                            String sort,
                                                                            Pageable pageable,
                                                                            LocalDateTime now) {
        if (paid) {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsPaidAndAvailable(now, pageable)
                        : repository.getEventsPaidAndAvailableByDate(now, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsPaid(now, pageable)
                        : repository.getEventsPaidByDate(now, pageable);
            }
        } else {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsAvailable(now, pageable)
                        : repository.getEventsAvailableByDate(now, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsAll(now, pageable)
                        : repository.getEventsAllByDate(now, pageable);
            }
        }
    }

    private List<Event> fetchEventsWithCategoriesWithoutTextWithoutRange(boolean paid,
                                                                         boolean onlyAvailable,
                                                                         String sort,
                                                                         Pageable pageable,
                                                                         List<Long> categories,
                                                                         LocalDateTime now) {
        if (paid) {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsWitchCatAndPaidAndAvailable(categories, now, pageable)
                        : repository.getEventsWitchCatAndPaidAndAvailableByDate(categories, now, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsPaidAndCat(categories, now, pageable)
                        : repository.getEventsPaidAndCatByDate(categories, now, pageable);
            }
        } else {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsAvailableAndCat(categories, now, pageable)
                        : repository.getEventsAvailableAndCatByDate(categories, now, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsAllAndCat(categories, now, pageable)
                        : repository.getEventsAllAndCatByDate(categories, now, pageable);
            }
        }
    }

    private List<Event> fetchEventsWithoutCategoriesWithTextWithoutRange(boolean paid,
                                                                         boolean onlyAvailable,
                                                                         String sort,
                                                                         Pageable pageable,
                                                                         String text,
                                                                         LocalDateTime now) {
        if (paid) {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndAvailableAndPaid(text, now, pageable)
                        : repository.getEventsTextAndAvailableAndPaidByDate(text, now, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndPaid(text, now, pageable)
                        : repository.getEventsTextAndPaidByDate(text, now, pageable);
            }
        } else {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndAvailable(text, now, pageable)
                        : repository.getEventsTextAndAvailableByDate(text, now, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsText(text, now, pageable)
                        : repository.getEventsTextByDate(text, now, pageable);
            }
        }
    }

    private List<Event> fetchEventsWithCategoriesWithTextWithoutRange(boolean paid,
                                                                      boolean onlyAvailable,
                                                                      String sort,
                                                                      Pageable pageable,
                                                                      String text,
                                                                      List<Long> categories,
                                                                      LocalDateTime now) {
        if (paid) {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndCategoriesAndAvailableAndPaid(text.toLowerCase(), categories, now, pageable)
                        : repository.getEventsTextAndCategoriesAndAvailableAndPaidByDate(text.toLowerCase(), categories, now, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndCategoriesAndPaid(text.toLowerCase(), categories, now, pageable)
                        : repository.getEventsTextAndCategoriesAndPaidByDate(text.toLowerCase(), categories, now, pageable);
            }
        } else {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndCategoriesAndAvailable(text.toLowerCase(), categories, now, pageable)
                        : repository.getEventsTextAndCategoriesAndAvailableByDate(text.toLowerCase(), categories, now, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndCategories(text.toLowerCase(), categories, now, pageable)
                        : repository.getEventsTextAndCategoriesByDate(text.toLowerCase(), categories, now, pageable);
            }
        }
    }

    private List<Event> fetchEventsWithoutTextWithRange(List<Long> categories,
                                                        boolean paid,
                                                        boolean onlyAvailable,
                                                        String sort,
                                                        Pageable pageable,
                                                        LocalDateTime start,
                                                        LocalDateTime end) {
        if (categories == null) {
            return fetchEventsWithoutCategoriesWithoutTextWithRange(paid, onlyAvailable, sort, pageable, start, end);
        } else {
            return fetchEventsWithCategoriesWithoutTextWithRange(paid, onlyAvailable, sort, pageable, categories, start, end);
        }
    }

    private List<Event> fetchEventsWithTextWithRange(List<Long> categories,
                                                     boolean paid,
                                                     boolean onlyAvailable,
                                                     String sort,
                                                     Pageable pageable,
                                                     String text,
                                                     LocalDateTime start,
                                                     LocalDateTime end) {
        if (categories == null) {
            return fetchEventsWithoutCategoriesWithTextWithRange(paid, onlyAvailable, sort, pageable, text, start, end);
        } else {
            return fetchEventsWithCategoriesWithTextWithRange(paid, onlyAvailable, sort, pageable, text, categories, start, end);
        }
    }

    private List<Event> fetchEventsWithoutCategoriesWithoutTextWithRange(boolean paid,
                                                                         boolean onlyAvailable,
                                                                         String sort,
                                                                         Pageable pageable,
                                                                         LocalDateTime start,
                                                                         LocalDateTime end) {
        if (paid) {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsPaidAndAvailable(start, end, pageable)
                        : repository.getEventsPaidAndAvailableByDate(start, end, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsPaid(start, end, pageable)
                        : repository.getEventsPaidByDate(start, end, pageable);
            }
        } else {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsAvailable(start, end, pageable)
                        : repository.getEventsAvailableByDate(start, end, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsAll(start, end, pageable)
                        : repository.getEventsAllByDate(start, end, pageable);
            }
        }
    }

    private List<Event> fetchEventsWithCategoriesWithoutTextWithRange(boolean paid,
                                                                      boolean onlyAvailable,
                                                                      String sort,
                                                                      Pageable pageable,
                                                                      List<Long> categories,
                                                                      LocalDateTime start,
                                                                      LocalDateTime end) {
        if (paid) {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsWitchCatAndPaidAndAvailable(categories, start, end, pageable)
                        : repository.getEventsWitchCatAndPaidAndAvailableByDate(categories, start, end, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsPaidAndCat(categories, start, end, pageable)
                        : repository.getEventsPaidAndCatByDate(categories, start, end, pageable);
            }
        } else {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsAvailableAndCat(categories, start, end, pageable)
                        : repository.getEventsAvailableAndCatByDate(categories, start, end, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsAllAndCat(categories, start, end, pageable)
                        : repository.getEventsAllAndCatByDate(categories, start, end, pageable);
            }
        }
    }

    private List<Event> fetchEventsWithoutCategoriesWithTextWithRange(boolean paid,
                                                                      boolean onlyAvailable,
                                                                      String sort,
                                                                      Pageable pageable,
                                                                      String text,
                                                                      LocalDateTime start,
                                                                      LocalDateTime end) {
        if (paid) {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndAvailableAndPaid(text.toLowerCase(), start, end, pageable)
                        : repository.getEventsTextAndAvailableAndPaidByDate(text.toLowerCase(), start, end, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndPaid(text.toLowerCase(), start, end, pageable)
                        : repository.getEventsTextAndPaidByDate(text.toLowerCase(), start, end, pageable);
            }
        } else {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndAvailable(text.toLowerCase(), start, end, pageable)
                        : repository.getEventsTextAndAvailableByDate(text.toLowerCase(), start, end, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsText(text.toLowerCase(), start, end, pageable)
                        : repository.getEventsTextByDate(text.toLowerCase(), start, end, pageable);
            }
        }
    }

    private List<Event> fetchEventsWithCategoriesWithTextWithRange(boolean paid,
                                                                   boolean onlyAvailable,
                                                                   String sort,
                                                                   Pageable pageable,
                                                                   String text,
                                                                   List<Long> categories,
                                                                   LocalDateTime start,
                                                                   LocalDateTime end) {
        if (paid) {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndCategoriesAndAvailableAndPaid(text.toLowerCase(), categories, start, end, pageable)
                        : repository.getEventsTextAndCategoriesAndAvailableAndPaidByDate(text.toLowerCase(), categories, start, end, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndCategoriesAndPaid(text.toLowerCase(), categories, start, end, pageable)
                        : repository.getEventsTextAndCategoriesAndPaidByDate(text.toLowerCase(), categories, start, end, pageable);
            }
        } else {
            if (onlyAvailable) {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndCategoriesAndAvailable(text.toLowerCase(), categories, start, end, pageable)
                        : repository.getEventsTextAndCategoriesAndAvailableByDate(text.toLowerCase(), categories, start, end, pageable);
            } else {
                return sort.equals("VIEWS")
                        ? repository.getEventsTextAndCategories(text.toLowerCase(), categories, start, end, pageable)
                        : repository.getEventsTextAndCategoriesByDate(text.toLowerCase(), categories, start, end, pageable);
            }
        }
    }

    private void addHit(String ip, String uri) {
        HitDto hit = new HitDto("ewm-main", uri, ip, LocalDateTime.now());
        client.addHit(hit);
    }
}
