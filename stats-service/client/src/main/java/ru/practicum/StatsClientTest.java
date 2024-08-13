package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@Slf4j
public class StatsClientTest {
    public static void main(String[] args) {
        SpringApplication.run(StatsClientTest.class, args);

        StatsClient statsClient = new StatsClient("http://localhost:9090");

        HitDto hitDto = new HitDto();
        hitDto.setApp("ewm-main-service");
        hitDto.setIp("192.163.0.1");
        hitDto.setUri("/events/1");
        hitDto.setTimestamp(LocalDateTime.now());

        statsClient.addHit(hitDto);
        List<StatsDto> result = statsClient.getStats(LocalDateTime.of(2024, 8, 1, 10, 0, 0),
                LocalDateTime.of(2025, 8, 1, 10, 0, 1),
                null,
                false);
        log.info("result: {}", result);
    }
}
