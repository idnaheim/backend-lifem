package com.idnaheim.lifem.calendar;

import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumBaseFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private CalendarService calendarService;

    private CalendarEntity sampleEvent;
    private CalendarRequest sampleRequest;

    @BeforeEach
    void setUp() {
        Instant start = Instant.parse("2026-09-10T08:00:00Z");
        Instant end   = Instant.parse("2026-09-10T09:00:00Z");

        sampleEvent = new CalendarEntity();
        sampleEvent.setId(1L);
        sampleEvent.setTitle("Team Standup");
        sampleEvent.setDescription("Daily sync");
        sampleEvent.setStartDate(start);
        sampleEvent.setEndDate(end);
        sampleEvent.setAllDay(false);
        sampleEvent.setCategory(EnumBaseCategory.WORK);
        sampleEvent.setFrequency(EnumBaseFrequency.DAILY);
        sampleEvent.setLocation("Google Meet");
        sampleEvent.setReminderEnabled(true);
        sampleEvent.setReminderMinutesBefore(10);

        sampleRequest = new CalendarRequest(
                "Team Standup",
                "Daily sync",
                start,
                end,
                false,
                EnumBaseCategory.WORK,
                EnumBaseFrequency.DAILY,
                "Google Meet",
                true,
                10
        );
    }

    // -------------------------------------------------------------------------
    // getAllEvents
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllEvents")
    class GetAllEvents {

        @Test
        @DisplayName("returns all events from repository")
        void returnsAll() {
            when(calendarRepository.findAll()).thenReturn(List.of(sampleEvent));

            List<CalendarEntity> result = calendarService.getAllEvents();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Team Standup");
            verify(calendarRepository).findAll();
        }

        @Test
        @DisplayName("returns empty list when no events exist")
        void returnsEmpty() {
            when(calendarRepository.findAll()).thenReturn(List.of());

            assertThat(calendarService.getAllEvents()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getEventById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getEventById")
    class GetEventById {

        @Test
        @DisplayName("returns present Optional when event exists")
        void found() {
            when(calendarRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

            Optional<CalendarEntity> result = calendarService.getEventById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getTitle()).isEqualTo("Team Standup");
        }

        @Test
        @DisplayName("returns empty Optional when event not found")
        void notFound() {
            when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(calendarService.getEventById(99L)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getEventsBetween
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getEventsBetween")
    class GetEventsBetween {

        @Test
        @DisplayName("delegates to repository and returns ordered results")
        void delegatesToRepository() {
            Instant from = Instant.parse("2026-09-01T00:00:00Z");
            Instant to   = Instant.parse("2026-09-30T23:59:59Z");

            when(calendarRepository.findByStartDateBetweenOrderByStartDateAsc(from, to))
                    .thenReturn(List.of(sampleEvent));

            List<CalendarEntity> result = calendarService.getEventsBetween(from, to);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Team Standup");
            verify(calendarRepository).findByStartDateBetweenOrderByStartDateAsc(from, to);
        }

        @Test
        @DisplayName("returns empty list when no events in range")
        void returnsEmptyWhenNoneInRange() {
            Instant from = Instant.parse("2025-01-01T00:00:00Z");
            Instant to   = Instant.parse("2025-01-31T23:59:59Z");

            when(calendarRepository.findByStartDateBetweenOrderByStartDateAsc(from, to))
                    .thenReturn(List.of());

            assertThat(calendarService.getEventsBetween(from, to)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // createEvent
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createEvent")
    class CreateEvent {

        @Test
        @DisplayName("maps request to entity and saves")
        void savesFromRequest() {
            when(calendarRepository.save(any())).thenAnswer(inv -> {
                CalendarEntity e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            CalendarEntity result = calendarService.createEvent(sampleRequest);

            assertThat(result.getTitle()).isEqualTo("Team Standup");
            assertThat(result.getLocation()).isEqualTo("Google Meet");
            assertThat(result.isReminderEnabled()).isTrue();
            assertThat(result.getReminderMinutesBefore()).isEqualTo(10);
            assertThat(result.getCategory()).isEqualTo(EnumBaseCategory.WORK);
            assertThat(result.getFrequency()).isEqualTo(EnumBaseFrequency.DAILY);

            ArgumentCaptor<CalendarEntity> captor = ArgumentCaptor.forClass(CalendarEntity.class);
            verify(calendarRepository).save(captor.capture());
            assertThat(captor.getValue().getTitle()).isEqualTo("Team Standup");
        }
    }

    // -------------------------------------------------------------------------
    // updateEvent
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateEvent")
    class UpdateEvent {

        @Test
        @DisplayName("updates all fields and returns updated entity when found")
        void updatesWhenFound() {
            Instant newStart = Instant.parse("2026-10-01T09:00:00Z");
            Instant newEnd   = Instant.parse("2026-10-01T10:00:00Z");

            CalendarRequest updateRequest = new CalendarRequest(
                    "Updated Meeting",
                    "New description",
                    newStart,
                    newEnd,
                    true,
                    EnumBaseCategory.PERSONAL,
                    EnumBaseFrequency.WEEKLY,
                    "Zoom",
                    false,
                    null
            );

            when(calendarRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
            when(calendarRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Optional<CalendarEntity> result = calendarService.updateEvent(1L, updateRequest);

            assertThat(result).isPresent();
            CalendarEntity updated = result.get();
            assertThat(updated.getTitle()).isEqualTo("Updated Meeting");
            assertThat(updated.getDescription()).isEqualTo("New description");
            assertThat(updated.getStartDate()).isEqualTo(newStart);
            assertThat(updated.getEndDate()).isEqualTo(newEnd);
            assertThat(updated.isAllDay()).isTrue();
            assertThat(updated.getCategory()).isEqualTo(EnumBaseCategory.PERSONAL);
            assertThat(updated.getFrequency()).isEqualTo(EnumBaseFrequency.WEEKLY);
            assertThat(updated.getLocation()).isEqualTo("Zoom");
            assertThat(updated.isReminderEnabled()).isFalse();
            assertThat(updated.getReminderMinutesBefore()).isNull();
        }

        @Test
        @DisplayName("returns empty Optional when event not found")
        void returnsEmptyWhenMissing() {
            when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(calendarService.updateEvent(99L, sampleRequest)).isEmpty();
            verify(calendarRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // deleteEvent
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteEvent")
    class DeleteEvent {

        @Test
        @DisplayName("returns true and deletes when event exists")
        void deletesExisting() {
            when(calendarRepository.existsById(1L)).thenReturn(true);

            assertThat(calendarService.deleteEvent(1L)).isTrue();
            verify(calendarRepository).deleteById(1L);
        }

        @Test
        @DisplayName("returns false when event does not exist")
        void returnsFalseWhenMissing() {
            when(calendarRepository.existsById(99L)).thenReturn(false);

            assertThat(calendarService.deleteEvent(99L)).isFalse();
            verify(calendarRepository, never()).deleteById(anyLong());
        }
    }
}
