package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.StatsDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query(value = "select new ru.practicum.StatsDto" +
            "(s.app, s.uri, count(s.uri)) from Hit as s " +
            "where s.timestamp between ?1 and ?2 " +
            "and s.uri in ?3 " +
            "group by s.app, s.uri " +
            "order by count(s.uri) desc ")
    List<StatsDto> getWithUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "select new ru.practicum.StatsDto" +
            "(s.app, s.uri, count(distinct s.ip)) from Hit as s " +
            "where s.timestamp between ?1 and ?2 " +
            "and s.uri in ?3 " +
            "group by s.app, s.uri " +
            "order by count(distinct s.ip) desc")
    List<StatsDto> getWithUriUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "select new ru.practicum.StatsDto" +
            "(s.app, s.uri, count(s.uri)) from Hit as s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "order by count(s.uri) desc ")
    List<StatsDto> getWithoutUri(LocalDateTime start, LocalDateTime end);

    @Query(value = "select new ru.practicum.StatsDto" +
            "(s.app, s.uri, count(distinct s.ip)) from Hit as s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "order by count(distinct s.ip) desc")
    List<StatsDto> getWithoutUriUnique(LocalDateTime start, LocalDateTime end);
}
