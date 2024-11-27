package ru.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.practicum.dto.HitDto;

import java.util.List;

public interface HitService {
    ResponseEntity<Object> addHit(HitDto hitDto);

    ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique);
}
