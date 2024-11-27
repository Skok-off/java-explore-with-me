package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.HitStatDto;
import ru.practicum.service.HitService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class HitController {
    private final HitService hitService;

    @GetMapping("/stats")
    public ResponseEntity<List<HitStatDto>> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {
        List<HitStatDto> stats = hitService.getStats(start, end, uris, unique);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/hit")
    public ResponseEntity<Void> addHit(@RequestBody HitDto hitDto) {
        hitService.addHit(hitDto);
        return ResponseEntity.ok().build();
    }
}
