package com.idnaheim.lifem.calendar;

import com.idnaheim.lifem.utilities.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/calendar/events")
@AllArgsConstructor
@Tag(name = "Calendar", description = "Calendar event scheduling and management")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "List all calendar events")
    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    @GetMapping
    public ResponseEntity<CustomResponse<List<CalendarEntity>>> getAllEvents() {
        return ResponseEntity.ok(CustomResponse.success(calendarService.getAllEvents()));
    }

    @Operation(summary = "Get calendar event by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event found"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<CalendarEntity>> getEventById(
            @Parameter(description = "Event ID") @PathVariable long id) {
        return calendarService.getEventById(id)
                .<ResponseEntity<CustomResponse<CalendarEntity>>>map(event -> ResponseEntity.ok(CustomResponse.success(event)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Get events in a date range",
               description = "Returns all calendar events that fall between the given start and end timestamps (ISO-8601).")
    @ApiResponse(responseCode = "200", description = "Events in range retrieved")
    @GetMapping("/range")
    public ResponseEntity<CustomResponse<List<CalendarEntity>>> getEventsBetween(
            @Parameter(description = "Range start (ISO-8601 instant)") @RequestParam Instant start,
            @Parameter(description = "Range end (ISO-8601 instant)") @RequestParam Instant end) {
        return ResponseEntity.ok(CustomResponse.success(calendarService.getEventsBetween(start, end)));
    }

    @Operation(summary = "Create a new calendar event")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<CalendarEntity>> createEvent(@RequestBody CalendarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(calendarService.createEvent(request)));
    }

    @Operation(summary = "Update a calendar event")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event updated"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<CalendarEntity>> updateEvent(
            @Parameter(description = "Event ID") @PathVariable long id,
            @RequestBody CalendarRequest request) {
        return calendarService.updateEvent(id, request)
                .<ResponseEntity<CustomResponse<CalendarEntity>>>map(updated -> ResponseEntity.ok(CustomResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Delete a calendar event")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event deleted"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteEvent(
            @Parameter(description = "Event ID") @PathVariable long id) {
        if (calendarService.deleteEvent(id)) {
            return ResponseEntity.ok(CustomResponse.success(204, "Event deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound());
    }

}
