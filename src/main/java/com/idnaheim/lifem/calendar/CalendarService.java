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

    public CalendarEntity createEvent(CalendarRequest request) {
        return calendarRepository.save(request.toEntity());
    }

    public Optional<CalendarEntity> updateEvent(long id, CalendarRequest request) {
        return calendarRepository.findById(id).map(existing -> {
            existing.setTitle(request.title());
            existing.setDescription(request.description());
            existing.setStartDate(request.startDate());
            existing.setEndDate(request.endDate());
            existing.setAllDay(request.allDay());
            existing.setCategory(request.category());
            existing.setFrequency(request.frequency());
            existing.setLocation(request.location());
            existing.setReminderEnabled(request.reminderEnabled());
            existing.setReminderMinutesBefore(request.reminderMinutesBefore());
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
