package com.idnaheim.lifem.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {

    List<CalendarEntity> findByStartDateBetween(Instant start, Instant end);

    List<CalendarEntity> findByStartDateBetweenOrderByStartDateAsc(Instant start, Instant end);

}
