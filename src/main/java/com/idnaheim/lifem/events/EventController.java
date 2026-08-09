package com.idnaheim.lifem.events;

import com.idnaheim.lifem.utilities.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@AllArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success(eventService.getAllEvents()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getEventById(@PathVariable long id) {
        return eventService.getEventById(id)
                .<ResponseEntity<ApiResponse<?>>>map(event -> ResponseEntity.ok(ApiResponse.success(event)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createEvent(@RequestBody EventEntity event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(eventService.createEvent(event)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateEvent(@PathVariable long id, @RequestBody EventEntity event) {
        return eventService.updateEvent(id, event)
                .<ResponseEntity<ApiResponse<?>>>map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEvent(@PathVariable long id) {
        if (eventService.deleteEvent(id)) {
            return ResponseEntity.ok(ApiResponse.success(204, "Event deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound());
    }

}
