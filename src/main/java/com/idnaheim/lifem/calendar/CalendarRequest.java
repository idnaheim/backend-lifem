package com.idnaheim.lifem.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.idnaheim.lifem.config.LocalDateToInstantDeserializer;
import com.idnaheim.lifem.enums.EventCategory;
import com.idnaheim.lifem.enums.EventFrequency;

import java.time.Instant;

public record CalendarRequest(
        String title,
        String description,
        @JsonDeserialize(using = LocalDateToInstantDeserializer.class)
        Instant startDate,
        @JsonDeserialize(using = LocalDateToInstantDeserializer.class)
        Instant endDate,
        @JsonProperty("allDay") boolean allDay,
        EventCategory category,
        EventFrequency frequency,
        String location,
        @JsonProperty("reminderEnabled") boolean reminderEnabled,
        Integer reminderMinutesBefore
) {
    public CalendarEntity toEntity() {
        CalendarEntity entity = new CalendarEntity();
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setAllDay(allDay);
        entity.setCategory(category);
        entity.setFrequency(frequency);
        entity.setLocation(location);
        entity.setReminderEnabled(reminderEnabled);
        entity.setReminderMinutesBefore(reminderMinutesBefore);
        return entity;
    }
}
