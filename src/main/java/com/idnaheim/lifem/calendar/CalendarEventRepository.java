package com.idnaheim.lifem.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, Long> {

    List<CalendarEventEntity> findByStartDateBetween(Instant start, Instant end);

    List<CalendarEventEntity> findByStartDateBetweenOrderByStartDateAsc(Instant start, Instant end);

}
