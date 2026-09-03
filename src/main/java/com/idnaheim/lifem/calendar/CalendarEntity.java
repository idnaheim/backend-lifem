package com.idnaheim.lifem.calendar;

import com.idnaheim.lifem.enums.EventCategory;
import com.idnaheim.lifem.enums.EventFrequency;
import com.idnaheim.lifem.utilities.AuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "calendar")
public class CalendarEntity extends AuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private String description;

    private Instant startDate;

    private Instant endDate;

    private boolean allDay;

    @Enumerated(EnumType.STRING)
    private EventCategory category;

    @Enumerated(EnumType.STRING)
    private EventFrequency frequency;

    private String location;

    private boolean reminderEnabled;

    private Integer reminderMinutesBefore;

}
