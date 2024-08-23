package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;
    private final HitMapper mapper;

    @Override
    public HitDto hit(HitDto hitDto) {
        return mapper.toHitDto(repository.save(mapper.tiHit(hitDto)));
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Неверные параметры start и end");
        }
        if (unique) {
            if (uris != null) {
                return repository.getWithUriUnique(start, end, uris);
            } else {
                return repository.getWithoutUriUnique(start, end);
            }
        } else {
            if (uris != null) {
                return repository.getWithUri(start, end, uris);
            } else {
                return repository.getWithoutUri(start, end);
            }
        }
    }
}
