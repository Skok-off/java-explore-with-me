package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.service.HitServiceImpl;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;
import ru.practicum.events.locations.model.LocationMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventMapper;
import ru.practicum.events.model.enums.AdminEventStateAction;
import ru.practicum.events.model.enums.EventState;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final HitServiceImpl hitService;

    @Override
    public List<EventFullDto> getEvents(List<Integer> users, List<EventState> states, List<Integer> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        log.info("Start checks");
        rangeStart = (Objects.nonNull(rangeStart)) ? rangeStart : LocalDateTime.of(1990, 1, 1, 0, 0);
        rangeEnd = (Objects.nonNull(rangeEnd)) ? rangeEnd : LocalDateTime.of(2200, 1, 1, 0, 0);

        if (Objects.nonNull(categories)) {
            categories.forEach(categoryId -> {
                if (!categoryRepository.existsById(categoryId)) {
                    throw new BadRequestException(String.format("Category with id %d is not found.", categoryId));
                }
            });
        }
        log.info("Check categories completed");

        if (size < 1) {
            size = 10;
        }
        PageRequest pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllForAdmin(users, states, categories, rangeStart, rangeEnd, pageable);

        List<EventFullDto> result = events.stream()
                .map(this::convertToEventFullDtoWithViews)
                .toList();
        log.info("Get events: {}", result);
        return result;
    }

    @Override
    public EventFullDto updateEvent(int eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Start update event");
        Event oldEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d is not found.", eventId)));

        handleStateUpdate(updateEventAdminRequest.getStateAction(), oldEvent);
        updateEventFields(updateEventAdminRequest, oldEvent);

        EventFullDto result = EventMapper.toEventFullDto(eventRepository.save(oldEvent));
        log.info("Updated event: {}", result);
        return result;
    }

    private void handleStateUpdate(AdminEventStateAction newStateAction, Event oldEvent) {
        log.info("Start update state");
        if (Objects.isNull(newStateAction)) {
            return;
        }

        if (newStateAction.equals(AdminEventStateAction.PUBLISH_EVENT)) {
            if (!oldEvent.getState().equals(EventState.PENDING)) {
                throw new ConflictException("The event can be published only if it is in a PENDING state.");
            }
            oldEvent.setState(EventState.PUBLISHED);
            oldEvent.setPublishedOn(LocalDateTime.now());
        } else if (newStateAction.equals(AdminEventStateAction.REJECT_EVENT)) {
            if (oldEvent.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException("The event can be canceled only if it has not yet been PUBLISHED.");
            }
            oldEvent.setState(EventState.CANCELED);
            log.info("Updated event state: {}", oldEvent);
        }
    }

    private void updateEventFields(UpdateEventAdminRequest updateRequest, Event event) {
        log.info("Start update event fields");
        if (Objects.nonNull(updateRequest.getEventDate())) {
            updateEventDate(updateRequest.getEventDate(), event);
        }

        Optional.ofNullable(updateRequest.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateRequest.getCategory()).ifPresent(categoryId -> {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id %d is not found.", categoryId)));
            event.setCategory(category);
        });
        Optional.ofNullable(updateRequest.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updateRequest.getLocation()).ifPresent(location -> event.setLocation(LocationMapper.toLocation(location)));
        Optional.ofNullable(updateRequest.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateRequest.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateRequest.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateRequest.getTitle()).ifPresent(event::setTitle);
        log.info("End update event fields");
    }

    private void updateEventDate(LocalDateTime newEventDate, Event event) {
        log.info("Start update event date");
        if (newEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("The updated event time must be at least one hour from the current time.");
        }
        event.setEventDate(newEventDate);
        log.info("Updated event date: {}", event);
    }

    private EventFullDto convertToEventFullDtoWithViews(Event event) {
        Long views = CommonEventService.getViews(event, hitService);
        event.setViews(views);
        return EventMapper.toEventFullDto(event);
    }
}
