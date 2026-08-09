package com.idnaheim.lifem.calendar;

import com.idnaheim.lifem.enums.EventCategory;
import com.idnaheim.lifem.enums.EventFrequency;
import java.time.Instant;
import java.time.LocalDateTime;

public record CalendarEventResponse(
    long id,
    String title,
    String description,
    Instant startDate,
    Instant endDate,
    boolean allDay,
    EventCategory category,
    EventFrequency frequency,
    String location,
    boolean reminderEnabled,
    Integer reminderMinutesBefore,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate
) {
    public static CalendarEventResponse fromEntity(CalendarEventEntity entity) {
        return new CalendarEventResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.isAllDay(),
            entity.getCategory(),
            entity.getFrequency(),
            entity.getLocation(),
            entity.isReminderEnabled(),
            entity.getReminderMinutesBefore(),
            entity.getCreatedBy(),
            entity.getCreatedDate(),
            entity.getModifiedBy(),
            entity.getModifiedDate()
        );
    }
}
