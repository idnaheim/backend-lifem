package com.idnaheim.lifem.calendar;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/calendar/events")
@AllArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @GetMapping
    public ResponseEntity getAllEvents() {
        return new ResponseEntity(calendarEventService.getAllEvents(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEventById(@PathVariable long id) {
        return calendarEventService.getEventById(id)
                .map(event -> new ResponseEntity(event, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/range")
    public ResponseEntity getEventsBetween(@RequestParam Instant start, @RequestParam Instant end) {
        return new ResponseEntity(calendarEventService.getEventsBetween(start, end), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody CalendarEventEntity event) {
        return new ResponseEntity(calendarEventService.createEvent(event), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable long id, @RequestBody CalendarEventEntity event) {
        return calendarEventService.updateEvent(id, event)
                .map(updated -> new ResponseEntity(updated, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteEvent(@PathVariable long id) {
        if (calendarEventService.deleteEvent(id)) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

}
