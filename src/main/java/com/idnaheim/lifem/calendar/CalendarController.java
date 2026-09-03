package com.idnaheim.lifem.calendar;

import com.idnaheim.lifem.utilities.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/calendar/events")
@AllArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success(calendarService.getAllEvents()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getEventById(@PathVariable long id) {
        return calendarService.getEventById(id)
                .<ResponseEntity<ApiResponse<?>>>map(event -> ResponseEntity.ok(ApiResponse.success(event)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<?>> getEventsBetween(@RequestParam Instant start, @RequestParam Instant end) {
        return ResponseEntity.ok(ApiResponse.success(calendarService.getEventsBetween(start, end)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createEvent(@RequestBody CalendarEntity event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(calendarService.createEvent(event)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateEvent(@PathVariable long id, @RequestBody CalendarEntity event) {
        return calendarService.updateEvent(id, event)
                .<ResponseEntity<ApiResponse<?>>>map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEvent(@PathVariable long id) {
        if (calendarService.deleteEvent(id)) {
            return ResponseEntity.ok(ApiResponse.success(204, "Event deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound());
    }

}
