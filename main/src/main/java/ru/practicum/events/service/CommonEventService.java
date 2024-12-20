package ru.practicum.events.service;

import ru.practicum.service.HitServiceImpl;
import ru.practicum.dto.HitStatDto;
import ru.practicum.events.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class CommonEventService {

    public static Long getViews(Event event, HitServiceImpl hitService) {
        String uris = "/events/" + event.getId();
        LocalDateTime start = Objects.nonNull(event.getPublishedOn()) ? event.getPublishedOn() : event.getCreatedOn();
        LocalDateTime end = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startFormatted = start.format(formatter);
        String endFormatted = end.format(formatter);

        List<HitStatDto> hitStatDtoList = hitService.getStats(startFormatted, endFormatted, List.of(uris), true);
        return hitStatDtoList.stream().findFirst().map(HitStatDto::getHits).orElse(0L);
    }
}
