package com.idnaheim.lifem.calendar;

import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumBaseFrequency;

import java.time.Instant;
import java.time.LocalDateTime;

public record CalendarResponse(
    long id,
    String title,
    String description,
    Instant startDate,
    Instant endDate,
    boolean allDay,
    EnumBaseCategory category,
    EnumBaseFrequency frequency,
    String location,
    boolean reminderEnabled,
    Integer reminderMinutesBefore,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate
) {
    public static CalendarResponse fromEntity(CalendarEntity entity) {
        return new CalendarResponse(
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
