package com.idnaheim.lifem.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public List<CalendarEntity> getAllEvents() {
        return calendarRepository.findAll();
    }

    public Optional<CalendarEntity> getEventById(long id) {
        return calendarRepository.findById(id);
    }

    public List<CalendarEntity> getEventsBetween(Instant start, Instant end) {
        return calendarRepository.findByStartDateBetweenOrderByStartDateAsc(start, end);
    }

    public CalendarEntity createEvent(CalendarEntity event) {
        return calendarRepository.save(event);
    }

    public Optional<CalendarEntity> updateEvent(long id, CalendarEntity updatedEvent) {
        return calendarRepository.findById(id).map(existing -> {
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
            return calendarRepository.save(existing);
        });
    }

    public boolean deleteEvent(long id) {
        if (calendarRepository.existsById(id)) {
            calendarRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
