package com.idnaheim.lifem.events;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(EventResponse::fromEntity)
                .toList();
    }

    public Optional<EventResponse> getEventById(long id) {
        return eventRepository.findById(id)
                .map(EventResponse::fromEntity);
    }

    public EventResponse createEvent(EventEntity event) {
        return EventResponse.fromEntity(eventRepository.save(event));
    }

    public Optional<EventResponse> updateEvent(long id, EventEntity updatedEvent) {
        return eventRepository.findById(id).map(existing -> {
            existing.setTitle(updatedEvent.getTitle());
            existing.setDescription(updatedEvent.getDescription());
            existing.setStartDate(updatedEvent.getStartDate());
            existing.setEndDate(updatedEvent.getEndDate());
            existing.setLocation(updatedEvent.getLocation());
            existing.setAllDay(updatedEvent.isAllDay());
            existing.setCategory(updatedEvent.getCategory());
            return EventResponse.fromEntity(eventRepository.save(existing));
        });
    }

    public boolean deleteEvent(long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
