package com.idnaheim.lifem.events;

import com.idnaheim.lifem.config.ApiResponse;
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
        return new ResponseEntity<>(
            ApiResponse.success(200, eventService.getAllEvents()),
            HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getEventById(@PathVariable long id) {
        var response = eventService.getEventById(id)
                .map(event -> new ResponseEntity(
                    ApiResponse.success(200, event),
                    HttpStatus.OK
                ))
                .orElse(new ResponseEntity(
                    ApiResponse.notFound(),
                    HttpStatus.NOT_FOUND
                ));
        return response;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createEvent(@RequestBody EventEntity event) {
        return new ResponseEntity<>(
            ApiResponse.created(eventService.createEvent(event)),
            HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateEvent(@PathVariable long id, @RequestBody EventEntity event) {
        var response = eventService.updateEvent(id, event)
                .map(updated -> new ResponseEntity(
                    ApiResponse.success(200, updated),
                    HttpStatus.OK
                ))
                .orElse(new ResponseEntity(
                    ApiResponse.notFound(),
                    HttpStatus.NOT_FOUND
                ));
        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEvent(@PathVariable long id) {
        if (eventService.deleteEvent(id)) {
            return new ResponseEntity<>(
                ApiResponse.success(204, "Event deleted successfully", null),
                HttpStatus.NO_CONTENT
            );
        }
        return new ResponseEntity<>(
            ApiResponse.notFound(),
            HttpStatus.NOT_FOUND
        );
    }

}
