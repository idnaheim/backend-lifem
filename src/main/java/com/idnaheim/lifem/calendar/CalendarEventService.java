package com.idnaheim.lifem.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    public List<CalendarEventEntity> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    public Optional<CalendarEventEntity> getEventById(long id) {
        return calendarEventRepository.findById(id);
    }

    public List<CalendarEventEntity> getEventsBetween(Instant start, Instant end) {
        return calendarEventRepository.findByStartDateBetweenOrderByStartDateAsc(start, end);
    }

    public CalendarEventEntity createEvent(CalendarEventEntity event) {
        return calendarEventRepository.save(event);
    }

    public Optional<CalendarEventEntity> updateEvent(long id, CalendarEventEntity updatedEvent) {
        return calendarEventRepository.findById(id).map(existing -> {
            existing.setTitle(updatedEvent.getTitle());
            existing.setDescription(updatedEvent.getDescription());
            existing.setStartDate(updatedEvent.getStartDate());
            existing.setEndDate(updatedEvent.getEndDate());
            existing.setAllDay(updatedEvent.isAllDay());
            existing.setCategory(updatedEvent.getCategory());
            existing.setFrequency(updatedEvent.getFrequency());
            existing.setLocation(updatedEvent.getLocation());
            existing.setReminderEnabled(updatedEvent.isReminderEnabled());
            existing.setReminderMinutesBefore(updatedEvent.getReminderMinutesBefore());
            return calendarEventRepository.save(existing);
        });
    }

    public boolean deleteEvent(long id) {
        if (calendarEventRepository.existsById(id)) {
            calendarEventRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
