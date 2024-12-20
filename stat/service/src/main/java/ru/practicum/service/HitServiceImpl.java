package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.HitStatDto;
import ru.practicum.exceptions.BadParamException;
import ru.practicum.exceptions.DateTimeException;
import ru.practicum.model.Hit;
import ru.practicum.model.HitMapper;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {
    private static final Logger log = LoggerFactory.getLogger(HitServiceImpl.class);
    private final HitRepository hitRepository;

    @Override
    public void addHit(HitDto hitDto) {
        Hit hit = HitMapper.toHit(hitDto);
        hitRepository.save(hit);
        log.info("Added hit: {}", hit);
    }

    @Override
    public List<HitStatDto> getStats(LocalDateTime start, LocalDateTime end, Set<String> uris, Boolean unique) {
        log.info("Start to getStats({}, {}, {})", uris, start, end);
        if (Objects.isNull(start)) {
            throw new BadParamException("Не указано начало.");
        }
        if (Objects.isNull(end)) {
            throw new BadParamException("Не указан конец.");
        }
        if (end.isBefore(start)) {
            throw new DateTimeException("Начало позже конца. Ошибка");
        }
        List<HitStatDto> result;
        if (unique) {
            if (Objects.isNull(uris) || uris.isEmpty()) {
                result = hitRepository.findAllUniqueHitsWhenUriIsEmpty(start, end);
            } else {
                result = hitRepository.findAllUniqueHitsWhenUriIsNotEmpty(start, end, uris);
            }
        } else {
            if (Objects.isNull(uris) || uris.isEmpty()) {
                result = hitRepository.findAllHitsWhenUriIsEmpty(start, end);
            } else {
                result = hitRepository.findAllHitsWhenStarEndUris(start, end, uris);
            }
        }
        return result;
    }
}