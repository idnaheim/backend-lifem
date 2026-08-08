package com.idnaheim.lifem.events;

import java.time.Instant;
import java.time.LocalDateTime;

public record EventResponse(
    long id,
    String title,
    String description,
    Instant startDate,
    Instant endDate,
    String location,
    boolean allDay,
    String category,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate
) {
    public static EventResponse fromEntity(EventEntity entity) {
        return new EventResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getLocation(),
            entity.isAllDay(),
            entity.getCategory(),
            entity.getCreatedBy(),
            entity.getCreatedDate(),
            entity.getModifiedBy(),
            entity.getModifiedDate()
        );
    }
}
