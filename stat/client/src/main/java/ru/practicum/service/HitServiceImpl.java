package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.client.HitClient;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.HitStatDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {
    private static final Logger log = LoggerFactory.getLogger(HitServiceImpl.class);
    private final HitClient hitClient;

    @Override
    public void addHit(HitDto hitDto) {
        log.info("Добавление хита: {}", hitDto);
        try {
            hitClient.addHit(hitDto);
        } catch (Exception e) {
            log.error("Ошибка при добавлении хита: {}", hitDto, e);
            throw e;
        }
    }

    @Override
    public List<HitStatDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("Получение статистики для start: {}, end: {}, uris: {}, unique: {}", start, end, uris, unique);
        try {
            return hitClient.getStats(start, end, uris, unique);
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: start: {}, end: {}, uris: {}, unique: {}", start, end, uris, unique, e);
            throw e;
        }
    }
}
