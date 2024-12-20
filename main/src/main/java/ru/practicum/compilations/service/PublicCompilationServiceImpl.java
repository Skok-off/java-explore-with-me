package ru.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.model.CompilationMapper;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.exceptions.NotFoundException;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicCompilationServiceImpl implements PublicCompilationService {
    private final CompilationRepository compilationRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        if (size < 1) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(from / size, size);
        if (Objects.isNull(pinned)) pinned = false;
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);
        List<CompilationDto> result = compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .toList();
        log.info("Compilations founded: {}", result);
        return result;
    }

    @Override
    public CompilationDto getCompilationById(int compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id %d is not found.", compId)));
        CompilationDto result = CompilationMapper.toCompilationDto(compilation);
        log.info("Compilation founded (by id {}): {} ", compId, result);
        return result;
    }
}
